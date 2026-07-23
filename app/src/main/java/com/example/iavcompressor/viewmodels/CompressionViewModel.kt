package com.example.iavcompressor.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iavcompressor.helper.BatchItem
import com.example.iavcompressor.helper.CompressionQuality
import com.example.iavcompressor.helper.CompressionUiState
import com.example.iavcompressor.helper.Image
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.util.Collections

class CompressionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CompressionUiState>(CompressionUiState.Loading)
    val uiState : StateFlow<CompressionUiState> = _uiState.asStateFlow()

    private var selectedRawImages = listOf<Image>()

    /**
     * Stores initial image list picked from HomeScreen
     */
    fun setSelectedImages(uris: List<Image>) {
        selectedRawImages = uris
    }
    private var isProcessingStarted = false



    fun startCompressionProcess(
        context: Context,
        images: List<Image> = selectedRawImages,
        quality: CompressionQuality
    ) {
        Log.d("CompressorVM", "startCompressionProcess called. Image count: ${images.size}")

        if (images.isEmpty()) {
            Log.e("CompressorVM", "Image list is empty!")
            _uiState.value = CompressionUiState.Error("No images selected.")
            return
        }

        if (isProcessingStarted) {
            Log.w("CompressorVM", "Compression already in progress. Skipping duplicate call.")
            return
        }

        isProcessingStarted = true

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = CompressionUiState.Loading

            val semaphore = Semaphore(permits = 2)
            val compressedImages = Collections.synchronizedList(mutableListOf<BatchItem>())

            try {
                coroutineScope {
                    val jobs = images.map { image ->
                        async {
                            semaphore.withPermit {
                                var tempInputFile: File? = null
                                try {
                                    Log.d("CompressorVM", "Processing image: ${image.uri}")
                                    val inputUri = image.uri
                                    val originalName = queryFileName(context, inputUri)
                                    tempInputFile = copyUriToCache(context, inputUri)

                                    val originalDetails = Image(
                                        uri = inputUri,
                                        name = originalName,
                                        sizeDisplay = formatFileSize(tempInputFile.length())
                                    )

                                    val compressedFile = Compressor.compress(context, tempInputFile) {
                                        resolution(1920, 1080)
                                        quality(80)
                                        format(android.graphics.Bitmap.CompressFormat.JPEG)
                                    }

                                    val compressedDetails = Image(
                                        uri = Uri.fromFile(compressedFile),
                                        name = "Compressed_$originalName",
                                        sizeDisplay = formatFileSize(compressedFile.length()),
                                        file = compressedFile
                                    )

                                    compressedImages.add(BatchItem(originalDetails, compressedDetails))
                                    Log.d("CompressorVM", "Successfully compressed: $originalName")

                                } catch (e: Exception) {
                                    Log.e("CompressorVM", "Error compressing single image", e)
                                } finally {
                                    tempInputFile?.delete()
                                }
                            }
                        }
                    }
                    jobs.awaitAll()
                }

                Log.d("CompressorVM", "Batch finished. Compressed total: ${compressedImages.size}")

                if (compressedImages.isNotEmpty()) {
                    _uiState.value = CompressionUiState.Success(compressedImages)
                } else {
                    _uiState.value = CompressionUiState.Error("Failed to compress selected images.")
                }

            } catch (e: Exception) {
                Log.e("CompressorVM", "Fatal batch exception", e)
                _uiState.value = CompressionUiState.Error("Compression failed: ${e.localizedMessage}")
            } finally {
                isProcessingStarted = false
            }
        }
    }


    fun saveToGallery(context : Context , compressedFile : File , fileName : String , onComplete : (Boolean) -> Unit ){

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME , fileName)
                    put(MediaStore.Images.Media.MIME_TYPE , "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH , "Pictures/CompressedImages")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , contentValues)

                uri?.let {
                    uri?.let { targetUri ->
                        resolver.openOutputStream(targetUri)?.use { outputStream ->
                            compressedFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        launch(Dispatchers.Main) { onComplete(true) }
                    } ?: launch(Dispatchers.Main) { onComplete(false) }

                }


            }catch (e : Exception){
                launch(Dispatchers.Main) { onComplete(false) }
            }

        }

    }

    private fun copyUriToCache(context: Context, inputUri: Uri): File {
        // Unique temp file name prevents any disk path collision
        val tempFile = File.createTempFile("raw_input_", "_${System.nanoTime()}.jpg", context.cacheDir)

        context.contentResolver.openInputStream(inputUri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    @SuppressLint("Range")
    private fun queryFileName(context: Context, inputUri: Uri) : String{
        var name = "image.jpg"

        context.contentResolver.query(inputUri,null,null,null,null)?.use {
            cursor ->
            var index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if(index == -1 && cursor.moveToFirst()){
                name = cursor.getString(index)
            }
        }
        return name
    }


    fun resetState() {
        isProcessingStarted = false
        _uiState.value = CompressionUiState.Loading
    }


    @SuppressLint("DefaultLocale")
    private fun formatFileSize(sizeBytes: Long): String {
        val sizeInMb = sizeBytes.toDouble() / (1024 * 1024)
        return if (sizeInMb >= 1.0) {
            String.format("%.2f MB", sizeInMb)
        } else {
            val sizeInKb = sizeBytes.toDouble() / 1024
            String.format("%.1f KB", sizeInKb)
        }
    }
}
package com.example.iavcompressor.viewmodels

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iavcompressor.helper.CompressionUiState
import com.example.iavcompressor.helper.Image
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CompressionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CompressionUiState>(CompressionUiState.Loading)
    val uiState : StateFlow<CompressionUiState> = _uiState.asStateFlow()

    private var isProcessingStarted = false

    fun startCompressionProcess(context : Context , inputUri : Uri){
        if(isProcessingStarted)return

        isProcessingStarted = true

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = CompressionUiState.Loading
            try {
                val originalName = queryFileName(context, inputUri)
                val tempInputFile = copyUriToCache(context, inputUri)

                val originalDetails = Image(
                    uri = inputUri,
                    name = originalName,
                    sizeDisplay = formatFileSize(  tempInputFile.length())
                )

                val compressedFile = Compressor.compress(context , tempInputFile){
                    resolution(1920,1080)
                    quality(80)
                    format(android.graphics.Bitmap.CompressFormat.JPEG)
                }

                val compressedDetails = Image(
                    uri = Uri.fromFile(compressedFile),
                    name = "Compressed_$originalName",
                    sizeDisplay = formatFileSize(compressedFile.length()),
                    file = compressedFile
                )

                _uiState.value = CompressionUiState.Success(
                    original = originalDetails,
                    compressed = compressedDetails
                )

                tempInputFile.delete()
            }catch (e : Exception){
            _uiState.value = CompressionUiState.Error("Compression failed")
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

    private fun copyUriToCache(context: Context , inputUri: Uri) : File{
        val tempFile = File(context.cacheDir , "temp_input_\${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(inputUri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
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
package com.example.iavcompressor.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.iavcompressor.helper.BatchItem
import com.example.iavcompressor.helper.CompressionUiState
import com.example.iavcompressor.helper.Image
import com.example.iavcompressor.viewmodels.CompressionViewModel
import com.example.iavcompressor.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressionResultScreen(
    onNavigateBack: () -> Unit,
    viewModel: CompressionViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compression Results") },
                navigationIcon = {
//                    TextButton(onClick = onNavigateBack) {
//                        Text("Back")
//                    }
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_ios_new_24px),
                        contentDescription = "Go Back",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(4.dp)
                            .clickable {
                                onNavigateBack()
                            }

                    )
                    
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(targetState = uiState, label = "StateTransition") { state ->
                when (state) {
                    is CompressionUiState.Loading -> {
                        LoadingView()
                    }
                    is CompressionUiState.Success -> {
                        // Using state.result matching your CompressionUiState data class
                        BatchResultCarouselView(
                            batchItems = state.result,
                            onSaveSingle = { item ->
                                item.compressedImage.file?.let { file ->
                                    viewModel.saveToGallery(context, file, item.compressedImage.name) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Saved ${item.compressedImage.name}!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onSaveAll = {
                                var savedCount = 0
                                state.result.forEach { item ->
                                    item.compressedImage.file?.let { file ->
                                        viewModel.saveToGallery(context, file, item.compressedImage.name) { success ->
                                            if (success) savedCount++
                                            if (savedCount == state.result.size) {
                                                Toast.makeText(context, "All ${state.result.size} images saved!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                    is CompressionUiState.Error -> {
                        ErrorView(message = state.message, onRetry = onNavigateBack)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Compressing your images...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BatchResultCarouselView(
    batchItems: List<BatchItem>,
    onSaveSingle: (BatchItem) -> Unit,
    onSaveAll: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { batchItems.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Batch count header
        Text(
            text = "Image ${pagerState.currentPage + 1} of ${batchItems.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Carousel HorizontalPager showing compressed files
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            pageSpacing = 16.dp
        ) { page ->
            val item = batchItems[page]

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.compressedImage.file ?: item.compressedImage.uri)
                            .memoryCacheKey("compressed_${item.compressedImage.file?.absolutePath}_${item.compressedImage.file?.lastModified()}_$page")
                            .diskCacheKey("compressed_disk_${item.compressedImage.file?.absolutePath}_${item.compressedImage.file?.lastModified()}_$page")
                            .crossfade(true)
                            .build(),
                        contentDescription = item.compressedImage.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Dots Indicator
        if (batchItems.size > 1) {
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(batchItems.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Metadata details using originalImage & compressedImage fields
        val activeItem = batchItems[pagerState.currentPage]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetadataCard(
                title = "Original",
                image = activeItem.originalImage,
                modifier = Modifier.weight(1f)
            )
            MetadataCard(
                title = "Compressed",
                image = activeItem.compressedImage,
                modifier = Modifier.weight(1f),
                highlight = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Save buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onSaveSingle(activeItem) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Save Current")
            }

            if (batchItems.size > 1) {
                Button(
                    onClick = onSaveAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Save All (${batchItems.size})")
                }
            }
        }
    }
}

@Composable
private fun MetadataCard(
    title: String,
    image: Image,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = image.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = image.sizeDisplay,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Error: $message", color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Go Back")
        }
    }
}
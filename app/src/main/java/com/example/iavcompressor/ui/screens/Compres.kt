package com.example.iavcompressor.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iavcompressor.helper.CompressionUiState
import com.example.iavcompressor.helper.Image
import com.example.iavcompressor.viewmodels.CompressionViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressionResultScreen(
    imageUri: Uri,
    onNavigateBack: () -> Unit,
    viewModel: CompressionViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Kickoff compression logic as soon as the screen enters the composition tree
    LaunchedEffect(imageUri) {
        viewModel.startCompressionProcess(context, imageUri)
    }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Compression Result") })
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
                        // Displayed when the image state object is null
                        LoadingView()
                    }
                    is CompressionUiState.Success -> {
                        // Displayed when the compressed image object is populated
                        CompressedResultView(
                            original = state.original,
                            compressed = state.compressed,
                            onSave = {
                                state.compressed.file?.let { file ->
                                    viewModel.saveToGallery(context, file, state.compressed.name) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Saved to Pictures/CompressedImages!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
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
            text = "Compressing your image...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompressedResultView(
    original: Image,
    compressed: Image,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display the newly compressed image using Coil
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(compressed.file ?: compressed.uri) // Pass the File directly
                .memoryCacheKey("${compressed.uri}_${compressed.file?.lastModified()}") // Unique key prevents cache collision
                .crossfade(true)
                .build(),
            contentDescription = "Compressed Image Preview",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )

        // Side-by-side metadata comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetadataCard(
                title = "Original",
                name = original.name,
                size = original.sizeDisplay,
                modifier = Modifier.weight(1f)
            )
            MetadataCard(
                title = "Compressed",
                name = compressed.name,
                size = compressed.sizeDisplay,
                modifier = Modifier.weight(1f),
                highlight = true
            )
        }

        // Action Button to Save File to Storage
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Save to Storage")
        }
    }
}



@Composable
private fun MetadataCard(
    title: String,
    name: String,
    size: String,
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
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = size, style = MaterialTheme.typography.titleMedium)
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
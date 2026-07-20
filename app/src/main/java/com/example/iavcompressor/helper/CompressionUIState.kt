package com.example.iavcompressor.helper

sealed interface CompressionUiState {
    // When the image object is null (compression in progress)
    object Loading : CompressionUiState

    // When compression finishes and the image state is ready
    data class Success(
        val original: Image,
        val compressed: Image
    ) : CompressionUiState

    data class Error(val message: String) : CompressionUiState
}
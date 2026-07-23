package com.example.iavcompressor.helper


import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class CompressionQuality(
    val label: String,
    val targetQuality: Int,
    val maxResolutionWidth: Int,
    val maxResolutionHeight: Int
) {
    LOW(
        label = "Low (Max Savings)",
        targetQuality = 50,
        maxResolutionWidth = 1280,
        maxResolutionHeight = 720
    ),
    MEDIUM(
        label = "Medium (Balanced)",
        targetQuality = 75,
        maxResolutionWidth = 1920,
        maxResolutionHeight = 1080
    ),
    HIGH(
        label = "High (Best Quality)",
        targetQuality = 90,
        maxResolutionWidth = 2560,
        maxResolutionHeight = 1440
    )
}
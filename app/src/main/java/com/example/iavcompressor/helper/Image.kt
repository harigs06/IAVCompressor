package com.example.iavcompressor.helper

import android.net.Uri
import java.io.File

data class Image(
    val uri: Uri,
    val name: String,
    val sizeDisplay: String,
    val file: File? = null
)
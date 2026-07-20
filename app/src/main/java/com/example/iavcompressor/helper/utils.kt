package com.example.iavcompressor.helper

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun extractMetadata(context: Context, uri: Uri): Pair<String, String> {
    var name = "Unknown"
    var sizeBytes = 0L

    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

        if (cursor.moveToFirst()) {
            if (nameIndex != -1) name = cursor.getString(nameIndex)
            if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
        }
    }

    // Mathematical formula to transform raw bytes into human-scannable Megabytes
    val sizeInMb = sizeBytes.toDouble() / (1024 * 1024)
    val displaySize = String.format("%.2f MB", sizeInMb)

    return Pair(name, displaySize)
}
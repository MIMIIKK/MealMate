package com.kanung.mealmate.data.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class ImageManager(private val context: Context) {
    fun saveImage(imageFile: File): String {
        val filename = "recipe_${UUID.randomUUID()}.jpg"
        val destinationFile = File(context.filesDir, "images").apply {
            if (!exists()) {
                mkdirs()
            }
        }

        val outputFile = File(destinationFile, filename)
        imageFile.copyTo(outputFile, overwrite = true)

        return outputFile.absolutePath
    }

    fun getImageUri(path: String): Uri {
        return Uri.fromFile(File(path))
    }

    fun deleteImage(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
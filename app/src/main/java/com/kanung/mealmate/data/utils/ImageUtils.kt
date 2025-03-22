package com.kanung.mealmate.data.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Converts a Uri to a File object.
     *
     * @param context The context of the application.
     * @param uri The Uri of the image to be converted.
     * @return The File object representing the image, or null if the conversion fails.
     */
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                val file = File(context.cacheDir, "temp_image.jpg")
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4 * 1024) // 4KB buffer
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                file
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
package com.example.selfiesegmentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.core.graphics.scale
import java.io.File
import java.io.FileOutputStream

class ImageLoader(private val context: Context) {

    // Get directory for storing images
    private val imageDir: File by lazy {
        File(context.filesDir, "images").apply {
            if (!exists()) mkdirs()
        }
    }

    fun justLoad(fileName: String): Bitmap? {
        return try {
            val file = File(imageDir, fileName)
            if (file.exists()) {
                // Decode the original bitmap
                val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)

                // Scale the bitmap down to 200x200 and return it
                Bitmap.createScaledBitmap(originalBitmap, 200, 200, true)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    // Load from internal storage
    fun loadImage(imageView: ImageView, fileName: String): Bitmap? {
        try {
            val file = File(imageDir, fileName)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
                return bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // Save bitmap to internal storage
    fun saveBitmap(bitmap: Bitmap, fileName: String): String? {
        return try {
            val file = File(imageDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Load from assets
    fun loadFromAssets(imageView: ImageView, assetFileName: String) {
        try {
            context.assets.open(assetFileName).use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Delete image from storage
    fun deleteImage(fileName: String): Boolean {
        val file = File(imageDir, fileName)
        return if (file.exists()) {
            file.delete()
        } else false
    }

    // Check if image exists
    fun imageExists(fileName: String): Boolean {
        return File(imageDir, fileName).exists()
    }

    // Get file path
    fun getImagePath(fileName: String): String {
        return File(imageDir, fileName).absolutePath
    }

    // List all saved images
    fun listSavedImages(): List<String> {
        return imageDir.listFiles()?.map { it.name } ?: emptyList()
    }

    // Clear all saved images
    fun clearAll() {
        imageDir.listFiles()?.forEach { it.delete() }
    }
}
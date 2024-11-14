package com.example.selfiesegmentation

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.scale
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageLoader(private val context: Context) {
    // Get external cache directory for storing images
    private val imageDir: File? by lazy {
        context.externalCacheDir?.let { cacheDir ->
            File(cacheDir, "images").apply {
                if (!exists()) mkdirs()
            }
        }
    }
    fun justLoad(fileName: String): Bitmap? {
        return try {
            // Check if imageDir exists
            if (imageDir == null) {
                Log.e("ImageLoader", "External cache directory not available")
                return null
            }

            val file = File(imageDir, fileName)

            if (!file.exists()) {
                Log.e("ImageLoader", "File doesn't exist at path: ${file.absolutePath}")
                return null
            }

            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (originalBitmap == null) {
                Log.e("ImageLoader", "Failed to decode bitmap")
                return null
            }

            // Resize to exact dimensions
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 200, 200, true)

            // Save the resized version
            saveBitmapAsPNG(resizedBitmap, fileName)


            Log.d("ImageLoader", "Image loaded and resized successfully at: ${file.absolutePath}")
            resizedBitmap
        } catch (e: Exception) {
            Log.e("ImageLoader", "Error loading image", e)
            null
        }
    }

    fun saveBitmapAsPNG(bitmap: Bitmap, fileName: String): String? {
        return try {
            if (imageDir == null) {
                Log.e("ImageLoader", "External cache directory not available")
                return null
            }


            val file = File(imageDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
               // out.flush()
            }
            Log.d("ImageLoader", "Saved PNG successfully at: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ImageLoader", "Error saving PNG", e)
            null
        }
    }

    fun loader(assetManager: AssetManager, fileName: String) : Bitmap?{
        try {
            val inputStream = assetManager.open(fileName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            return null
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
        return imageDir?.listFiles()?.map { it.name } ?: emptyList()
    }

    // Clear all saved images
    fun clearAll() {
        imageDir?.listFiles()?.forEach { it.delete() }
    }
}
package com.example.selfiesegmentation

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

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

            // Save the original version (if needed)
            saveBitmapAsPNG(originalBitmap, fileName)

            Log.d("ImageLoader", "Image loaded successfully at: ${file.absolutePath}")
            originalBitmap
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
            }
            Log.d("ImageLoader", "Saved PNG successfully at: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ImageLoader", "Error saving PNG", e)
            null
        }
    }

    fun loader(assetManager: AssetManager, fileName: String): Bitmap? {
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

    @RequiresApi(Build.VERSION_CODES.Q) // Required for Android 10 and above
    fun saveImageToGallery(bitmap: Bitmap, context: Context, imageName: String) {
        // Prepare the content values for the image
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$imageName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/YourAppName"
            )  // The folder where the image will be saved
        }

        // Insert the image into the MediaStore
        val resolver: ContentResolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            try {
                // Open an output stream to write the bitmap to the selected uri
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
                // Notify the gallery to update (it may not be necessary but it helps)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }
}
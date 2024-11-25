package com.example.selfiesegmentation

import android.app.Application
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.io.IOException
import java.nio.ByteBuffer


class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val imagelo: ImageLoader = ImageLoader(application)
    private val _stickermap = MutableLiveData<Bitmap>()
    val stickermap: LiveData<Bitmap> get() = _stickermap

    private val k = MutableLiveData<Bitmap>()
    val kmap: LiveData<Bitmap> get() = k

    private val _centered = MutableLiveData<Bitmap>()
    val centered : LiveData<Bitmap> get() = _centered

    var bs : Pair<Bitmap,Bitmap> ?= null
    private val _sepiaBitmap = MutableLiveData<Bitmap>()
    val sepiaBitmap: LiveData<Bitmap> get() = _sepiaBitmap

    private val _bnwBitmap = MutableLiveData<Bitmap>()
    val bnwBitmap: LiveData<Bitmap> get() = _bnwBitmap

    private val _bitmap = MutableLiveData<Bitmap?>().apply { value = null }
    val bitmap: LiveData<Bitmap?> get() = _bitmap

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    private val _maskBitmap = MutableLiveData<Bitmap>()
    val maskBitmap: LiveData<Bitmap> get() = _maskBitmap

    private val _backgrounds = MutableLiveData<Bitmap>()
    val backgrounds: LiveData<Bitmap> get() = _backgrounds

    lateinit var boxs : Rect

    private var objectDetectionCompletes = false
    fun selfie_segmentation(bitmap: Bitmap) {
        val startime = System.currentTimeMillis()
        if (objectDetectionCompletes) {
            val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .enableRawSizeMask()
                .build()

            val segmenter = Segmentation.getClient(options)

            val inputImage = InputImage.fromBitmap(bitmap, 0)

            segmenter.process(inputImage)
                .addOnSuccessListener { segmentationMask ->
                    val maskBuffer = segmentationMask.buffer
                    val maskWidth = segmentationMask.width
                    val maskHeight = segmentationMask.height
                    val maskBitmap = convertByteBufferToBitmap(maskBuffer, maskWidth, maskHeight)

                    val resultBitmap = applySegmentationMask(bitmap, maskBitmap, Color.WHITE)
                    bs = resultBitmap
                    _maskBitmap.value = maskBitmap
                    _bitmap.value = resultBitmap.first
                    _stickermap.value = resultBitmap.second
                    _centered.value = resultBitmap.first
                    _statusMessage.value = "Segmentation successful!"
                    //updateBitmap(bs!!.first)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    _statusMessage.value = "Segmentation failed: ${e.message}"
                }
        } else {
            _statusMessage.value = "ObjectDetectionFailed"
        }
        val endtime = System.currentTimeMillis()
        println("the time taken to remove the background is ${endtime - startime}")
    }

    fun updateBitmap(newBitmap: Bitmap?) {
        _bitmap.value = newBitmap
        Log.d("SharedViewModel", "Bitmap updated: $newBitmap")
    }


    // Static, constant list that doesn’t change across sessions
     var staticImageList = listOf(
        "damn.jpg", "emoji.png", "glass.png", "image 3.png", "image 2.png",
        "image 6.png", "image 1340.png", "image 627.png", "image 304.png",
        "image 1348.png", "image 1772.png"
    ).toMutableList()
    // Function that generates the final list with the dynamic path
    fun getImageListWithDynamicPath(imagelo: ImageLoader): MutableList<String> {
        val list = staticImageList.toMutableList()
            list.add(0,imagelo.getImagePath("stick"))
        return list
    }

     private fun applySegmentationMask(original: Bitmap, mask: Bitmap, color: Int): Pair<Bitmap, Bitmap> {
        val scaledMask = Bitmap.createScaledBitmap(mask, original.width, original.height, false)

        val width = original.width
        val height = original.height
        val resultPixels = IntArray(width * height)
        val stickerPixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val maskPixel = scaledMask.getPixel(x, y)

                // Mask alpha (grayscale value) is used to determine foreground
                val maskAlpha = Color.alpha(maskPixel)

                if (maskAlpha > 128) { // Foreground
                    val originalPixel = original.getPixel(x, y)

                    // Set the pixel with maximum opacity
                    resultPixels[index] = Color.argb(255, Color.red(originalPixel), Color.green(originalPixel), Color.blue(originalPixel))
                    stickerPixels[index] = resultPixels[index]
                } else { // Background
                    resultPixels[index] = color // Use provided background color
                    stickerPixels[index] = Color.TRANSPARENT
                }
            }
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val sticker = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
        sticker.setPixels(stickerPixels, 0, width, 0, 0, width, height)

        return Pair(resultBitmap, sticker)
    }


    fun cropBitmap(originalBitmap: Bitmap, left: Int, top: Int, width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(originalBitmap, left, top, width, height)
    }


    fun scaleDownBitmap(original: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val aspectRatio = original.width.toFloat() / original.height
        val scaledWidth: Int
        val scaledHeight: Int

        if (original.width > original.height) {
            scaledWidth = maxWidth
            scaledHeight = (maxWidth / aspectRatio).toInt()
        } else {
            scaledWidth = (maxHeight * aspectRatio).toInt()
            scaledHeight = maxHeight
        }
        return Bitmap.createScaledBitmap(original, scaledWidth, scaledHeight, true)
    }


    private fun convertByteBufferToBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap {
        buffer.rewind() // Reset buffer to the beginning
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val foregroundConfidence = buffer.float // Get the confidence value (float)
                if (foregroundConfidence > 0.5) {
                    // Foreground (person) - White
                    bitmap.setPixel(x, y, Color.WHITE)
                } else {
                    // Background - Transparent
                    bitmap.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }

        return bitmap
    }
    fun applyBlackAndWhiteFilter(bitmap: Bitmap): Bitmap {
        val startime = System.currentTimeMillis()
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                // Get the pixel color
                val pixel = bitmap.getPixel(i, j)
                // Extract the red, green, and blue components
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // Compute the grayscale value
                val gray = (red * 0.3 + green * 0.59 + blue * 0.11).toInt().coerceIn(0, 255)

                // Set the pixel to the grayscale color
                newBitmap.setPixel(i, j, Color.rgb(gray, gray, gray))
            }
        }
        _bnwBitmap.value = newBitmap
        val endtime = System.currentTimeMillis()
        println("the time taken to put the filter is ${endtime - startime}")
        return newBitmap

    }
    fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val startime = System.currentTimeMillis()
        val bmp = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // Apply sepia transformation
                val tr = (0.393 * red + 0.769 * green + 0.189 * blue).toInt().coerceIn(0, 255)
                val tg = (0.349 * red + 0.686 * green + 0.168 * blue).toInt().coerceIn(0, 255)
                val tb = (0.272 * red + 0.534 * green + 0.131 * blue).toInt().coerceIn(0, 255)

                val newPixel = Color.rgb(tr, tg, tb)
                bmp.setPixel(x, y, newPixel)
            }
        }
        _sepiaBitmap.value = bmp
        val endtime = System.currentTimeMillis()
        println("the time taken to put the filter is ${endtime - startime}")
        return bmp
    }

    private fun areBoxesOverlapping(box1: Rect, box2: Rect): Boolean {
        val overlapLeft = maxOf(box1.left, box2.left)
        val overlapTop = maxOf(box1.top, box2.top)
        val overlapRight = minOf(box1.right, box2.right)
        val overlapBottom = minOf(box1.bottom, box2.bottom)

        return overlapLeft < overlapRight && overlapTop < overlapBottom
    }

    fun processImageObjectDetection(bitmap: Bitmap) {
        val startTime = System.currentTimeMillis()
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        val objectDetector = ObjectDetection.getClient(options)
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        objectDetector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                if (detectedObjects.isEmpty()) {
                    _statusMessage.value = "ObjectDetection Failed"
                    return@addOnSuccessListener
                }

                objectDetectionCompletes = true

                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = android.graphics.Canvas(mutableBitmap)
                val paint = Paint().apply {
                    color = Color.RED
                    strokeWidth = 8f
                    style = Paint.Style.STROKE
                }

                val detectedBoxes = mutableListOf<Rect>()
                var largestBox: Rect? = null
                var maxArea = 0

                for (obj in detectedObjects) {
                    val box = obj.boundingBox

                    // Check for overlapping or duplicates
                    if (detectedBoxes.none { existingBox -> areBoxesOverlapping(existingBox, box) }) {
                        detectedBoxes.add(box)
                        canvas.drawRect(box, paint)

                        // Calculate the area of the current box
                        val area = box.width() * box.height()
                        if (area > maxArea) {
                            maxArea = area
                            largestBox = box
                        }

                        Log.d(
                            "ObjectDetection",
                            "Bounding Box: left=${box.left}, top=${box.top}, right=${box.right}, bottom=${box.bottom}"
                        )
                    }
                }

                // Store the largest box
                if (largestBox != null) {
                    boxs = largestBox
                    Log.d(
                        "ObjectDetection",
                        "Largest Box: left=${boxs.left}, top=${boxs.top}, right=${boxs.right}, bottom=${boxs.bottom}"
                    )
                }

             //   _bitmap.value = mutableBitmap
                _statusMessage.value = "ObjectDetection Found"
            }
            .addOnFailureListener { e ->
                Log.d("TAG", "ObjectDetection Failed")
                e.printStackTrace()
                _statusMessage.value = "ObjectDetection has Failed"
            }

        val endTime = System.currentTimeMillis()
        println("The time taken to detect the object is ${endTime - startTime} ms")
    }


    fun background(bitmap: Bitmap, mask: Bitmap, color: Int) {
        var startime = System.currentTimeMillis()
        val bitmapstore = applySegmentationMask(bitmap, mask, color)
        // Update kmap with the first part of the result
        k.value = bitmapstore.first

        _statusMessage.value = "Segmentation successful!"
        var endtime = System.currentTimeMillis()
        println("The time taken to add color is ${endtime - startime}")
    }

    fun calculateavgforeground(original: Bitmap, mask: Bitmap): Int {
        var red = 0
        var green = 0
        var blue = 0
        var count = 0
        val scaledMask = Bitmap.createScaledBitmap(mask,original.width,original.height,false)
//        val width = minOf(original.width, mask.width)
//        val height = minOf(original.height, mask.height)

        for (x in 0 until scaledMask.width) {
            for (y in 0 until scaledMask.height) {
                val maskpixel = scaledMask.getPixel(x, y)
                if (Color.alpha(maskpixel) > 128) {
                    val pixel = original.getPixel(x, y)
                    red += Color.red(pixel)
                    green += Color.green(pixel)
                    blue += Color.blue(pixel)
                    count++
                }
            }
        }
        val avgRed = if (count > 0) red / count else 0
        val avgGreen = if (count > 0) green / count else 0
        val avgBlue = if (count > 0) blue / count else 0

        val reds = 255 - avgRed
        val greens = 255 - avgGreen
        val blues = 255 - avgBlue

         var k = Color.rgb(reds, greens, blues)
        return k;
    }
  fun setBackground(assetManager: AssetManager,assetPath : String) {

      val inputStream = assetManager.open(assetPath)
      val bitmap = BitmapFactory.decodeStream(inputStream)
      inputStream.close()
     if(bitmap!=null){
         _backgrounds.value = bitmap
         _statusMessage.value = "background image is added"
     }
      else{
          _statusMessage.value = "bacground not loaded"
     }
}
    fun applyNewBackground(foreground : Bitmap,Mask : Bitmap){
        val start = System.currentTimeMillis()
        val back = _backgrounds.value?:return
        val resultBitmap = applyImageSegmentationMask(foreground,Mask,back)
        _bitmap.value = resultBitmap
        _statusMessage.value = "Backgorund image added"
        val endtime = System.currentTimeMillis()

        println("the time taken to add the background image is ${endtime - start}")
    }

    private fun applyImageSegmentationMask(original: Bitmap, mask: Bitmap, back: Bitmap): Bitmap {
        // Scale the mask and background image to match the original size
        val scaledMask = Bitmap.createScaledBitmap(mask, original.width, original.height, false)
        val scaledBackground = Bitmap.createScaledBitmap(back, original.width, original.height, false)

        // Create arrays to store pixel data
        val resultPixels = IntArray(original.width * original.height)
        val stickerPixels = IntArray(original.width * original.height)

        // Loop through each pixel and process it
        for (x in 0 until original.width) {
            for (y in 0 until original.height) {
                val maskPixel = scaledMask.getPixel(x, y)

                val index = y * original.width + x  // Calculate index for result arrays

                if (Color.alpha(maskPixel) > 128) { // Foreground (alpha > 128)
                    val originalPixel = original.getPixel(x, y)
                    // Set the foreground pixel with full opacity (alpha = 255)
                    resultPixels[index] = Color.argb(255, Color.red(originalPixel), Color.green(originalPixel), Color.blue(originalPixel))
                    stickerPixels[index] = resultPixels[index]
                } else { // Background (alpha <= 128)
                    resultPixels[index] = scaledBackground.getPixel(x, y)
                    stickerPixels[index] = Color.TRANSPARENT
                }
            }
        }

        // Create result bitmap and set pixels
        val resultBitmap = Bitmap.createBitmap(original.width, original.height, original.config)
        resultBitmap.setPixels(resultPixels, 0, original.width, 0, 0, original.width, original.height)

        // Create sticker bitmap and set pixels for background
        val sticker = Bitmap.createBitmap(original.width, original.height, original.config)
        sticker.setPixels(stickerPixels, 0, original.width, 0, 0, original.width, original.height)

        return resultBitmap  // Return the final image with the applied mask
    }


    }



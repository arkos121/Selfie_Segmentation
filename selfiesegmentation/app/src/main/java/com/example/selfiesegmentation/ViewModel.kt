package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.nio.ByteBuffer

class MainViewModel : ViewModel() {

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap> get() = _bitmap

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage


    private val _maskBitmap = MutableLiveData<Bitmap>()
    val maskBitmap: LiveData<Bitmap> get() = _maskBitmap

private var objectDetectionCompletes = false

    fun selfie_segmentation(bitmap: Bitmap) {
        if(objectDetectionCompletes){
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

                val resultBitmap = applySegmentationMask(bitmap,maskBitmap,Color.WHITE)
                _maskBitmap.value = maskBitmap
                _bitmap.value = resultBitmap
                _statusMessage.value = "Segmentation successful!"
                            }
            .addOnFailureListener{ e ->
                e.printStackTrace()
                _statusMessage.value = "Segmentation failed: ${e.message}"
            }
            }
        else{
            _statusMessage.value = "ObjectDetectionFailed"
        }
    }


        private fun applySegmentationMask(original: Bitmap, mask: Bitmap, color: Int):Bitmap {
            val scaledMask = Bitmap.createScaledBitmap(mask, original.width, original.height, false)
            val resultBitmap = Bitmap.createBitmap(original.width, original.height, original.config)
            val canvas = android.graphics.Canvas(resultBitmap)
            val paint = Paint()


            for (x in 0 until original.width) {
                for (y in 0 until original.height) {
                    val maskPixel = scaledMask.getPixel(x, y)
                    if (Color.alpha(maskPixel) > 128) {
                        paint.color = original.getPixel(x, y)
                    }
                    else {
                        paint.color = color
                    }
                        canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
                    }
                }

                return resultBitmap
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

fun processImageObjectDetection(bitmap: Bitmap) {
    val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    val objectdetector = ObjectDetection.getClient(options)

    val inputImage = InputImage.fromBitmap(bitmap, 0)

    objectdetector.process(inputImage).addOnSuccessListener { detectedObjects ->

        objectDetectionCompletes = true
        val mutableBitMap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(mutableBitMap)
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }

        for (obj in detectedObjects) {
            val box = obj.boundingBox
            val left = box.left
            val right = box.right
            val bottom = box.bottom
            val up = box.top

            canvas.drawRect(box, paint)
            Log.d(
                "ObjectDetection",
                "Bounding Box: left=$left, up=$up, right=$right, bottom=$bottom"
            )

            _bitmap.value = mutableBitMap
            _statusMessage.value = "ObjectDetectionFound"
        }
    }
        .addOnFailureListener { e ->
            e.printStackTrace()
         _statusMessage.value = "ObjectDetectionFailed"

        }
}
    fun background(resultBitmap: Bitmap,mask: Bitmap,color: Int) {

        val bitmapstore = applySegmentationMask(resultBitmap, mask, color)
        _bitmap.value = bitmapstore
        _statusMessage.value = "Segmentation successful!"
    }


}
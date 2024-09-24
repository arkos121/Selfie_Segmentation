package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.io.IOException
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private var loadedBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Store the loaded bitmap here

        setContentView(R.layout.layout)
        val readFileButton: Button = findViewById(R.id.button1)
        val imageView: ImageView = findViewById(R.id.imageview)
        val processImageButton: Button = findViewById(R.id.button2)
        val selfieSegmentation: Button = findViewById(R.id.button3)
        val textView : TextView = findViewById(R.id.textView)

        readFileButton.setOnClickListener {
            try {
                val assetManager = this.assets
                val inputStream = assetManager.open("photo1.jpg")
                val bitmap = BitmapFactory.decodeStream(inputStream)

                inputStream.close()
                imageView.setImageBitmap(bitmap)
                loadedBitmap = bitmap

                textView.text = ""

            }

            catch (e: IOException){
                e.printStackTrace()
                Toast.makeText(this,"Failed to load image0",Toast.LENGTH_LONG).show()
                textView.text = "Failed to load the image"
            }            }

        processImageButton.setOnClickListener{
            if(loadedBitmap!=null){
                processImageObjectDetection(loadedBitmap!!,textView,imageView)
            }else{
                textView.text = "First load the image"
            }
        }
        selfieSegmentation.setOnClickListener{
            if(loadedBitmap!=null){
                selfie_segmentation(loadedBitmap!!, textView, imageView)
            }
            else{
                textView.text = "Image Missing!"
            }
        }
        }

private fun selfie_segmentation(bitmap: Bitmap, textView: TextView, imageView: ImageView){
    val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
        .enableRawSizeMask()
        .build()

    val segmenter = Segmentation.getClient(options)

    val inputImage = InputImage.fromBitmap(bitmap, 0)
    fun applySegmentationMask(original: Bitmap, mask: Bitmap):Bitmap {
        val scaledMask = Bitmap.createScaledBitmap(mask, original.width, original.height, false)
        val resultBitmap = Bitmap.createBitmap(original.width, original.height, original.config)
        val canvas = android.graphics.Canvas(resultBitmap)
        val paint = Paint()

        for(x in 0 until original.width){
            for(y in 0 until  original.height){
                val maskPixel = scaledMask.getPixel(x,y)
                if (Color.alpha(maskPixel)>128) {
                    paint.color = original.getPixel(x, y)
                }
                else{
                    paint.color = Color.TRANSPARENT
                }
                canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
            }

        }
        return resultBitmap
    }
    fun convertByteBufferToBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap {
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


    segmenter.process(inputImage)
        .addOnSuccessListener { segmentationMask ->
            val maskBuffer = segmentationMask.buffer
            val maskWidth = segmentationMask.width
            val maskHeight = segmentationMask.height
            val maskBitmap = convertByteBufferToBitmap(maskBuffer, maskWidth, maskHeight)

            val resultBitmap = applySegmentationMask(bitmap, maskBitmap)
            imageView.setImageBitmap(resultBitmap)
        }
        .addOnFailureListener{ e ->
            e.printStackTrace()
        }



}
private fun processImageObjectDetection(bitmap: Bitmap,textView: TextView,imageView: ImageView){
    val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    val objectdetector = ObjectDetection.getClient(options)

    val inputImage = InputImage.fromBitmap(bitmap,0)

    objectdetector.process(inputImage).addOnSuccessListener { detectedObjects->

        val mutableBitMap = bitmap.copy(Bitmap.Config.ARGB_8888,true)
        val canvas = android.graphics.Canvas(mutableBitMap)
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }

        if(detectedObjects.isEmpty()){
            Toast.makeText(this,"No object detected",Toast.LENGTH_SHORT).show()
              textView.text ="No object detected"}
    else{
        for(obj in detectedObjects){
            val box = obj.boundingBox
            val left = box.left
            val right = box.right
            val bottom = box.bottom
            val up = box.top
            val labels = obj.labels

            canvas.drawRect(box,paint)
            Toast.makeText(this,"ObjectDetection:Bounding Box: left=$left, up=$up, right=$right, bottom=$bottom",Toast.LENGTH_SHORT).show()
            Log.d("ObjectDetection", "Bounding Box: left=$left, up=$up, right=$right, bottom=$bottom")
            textView.append("Object at rect($left, $up, $right, $bottom)\n")
            if(labels.isNotEmpty()){
                val label = labels[0].text
                Log.d("ObjectDetection", "Detected label: $label")
                Toast.makeText(this,"detected label: $label",Toast.LENGTH_SHORT).show()
                textView.append("Label : label\n")
            }
        }
            imageView.setImageBitmap(mutableBitMap)
    }

}
        .addOnFailureListener{ e->
            e.printStackTrace()
            Toast.makeText(this,"ObjectDetectionFailed", Toast.LENGTH_SHORT).show()
        }
}
    }
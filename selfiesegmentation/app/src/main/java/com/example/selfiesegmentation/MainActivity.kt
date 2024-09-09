package com.example.selfiesegmentation

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.selfiesegmentation.ui.theme.SelfieSegmentationTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var loadedBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Store the loaded bitmap here

        setContentView(R.layout.layout)
        val readFileButton: Button = findViewById(R.id.button1)
        val imageView: ImageView = findViewById(R.id.imageview)
        val processImageButton: Button = findViewById(R.id.button2)
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
                processImageObjectDetection(loadedBitmap!!,textView)
            }else{
                textView.text = "First load the image"
            }
        }
        }


private fun processImageObjectDetection(bitmap: Bitmap,textView: TextView){
    val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    val objectdetector = ObjectDetection.getClient(options)

    val inputImage = InputImage.fromBitmap(bitmap,0)

    objectdetector.process(inputImage).addOnSuccessListener { detectedObjects->
        if(detectedObjects.isEmpty()){
            Toast.makeText(this,"No object detected",Toast.LENGTH_SHORT).show()
              textView.text ="No object detected"}
    else{
        for(obj in detectedObjects){
            val box = obj.boundingBox
            val trackid = obj.trackingId
            val labels = obj.labels

            Toast.makeText(this,"Object detected at : $box",Toast.LENGTH_SHORT).show()
            textView.append("Object at ${obj.boundingBox}\n")
            if(labels.isNotEmpty()){
                val label = labels[0].text
                Toast.makeText(this,"detected label: $label",Toast.LENGTH_SHORT).show()
                textView.append("Label : label\n")
            }
        }
    }

}
        .addOnFailureListener{ e->
            e.printStackTrace()
            Toast.makeText(this,"ObjectDetectionFailed", Toast.LENGTH_SHORT).show()
        }
}
    }
package com.example.selfiesegmentation

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout)
        val readFileButton: Button = findViewById(R.id.button1)
        val imageView: ImageView = findViewById(R.id.imageview)

        readFileButton.setOnClickListener {
            try {
                val assetManager = this.assets
                val inputStream = assetManager.open("photo1.jpg")
                val bitmap = BitmapFactory.decodeStream(inputStream)

                inputStream.close()
                imageView.setImageBitmap(bitmap)
            }
            catch (e: IOException){
                e.printStackTrace()
            }            }
        }
    }

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SelfieSegmentationTheme {
        Greeting("Android")
    }
}
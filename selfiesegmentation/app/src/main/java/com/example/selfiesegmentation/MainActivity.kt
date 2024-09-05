package com.example.selfiesegmentation

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.selfiesegmentation.ui.theme.SelfieSegmentationTheme
import java.io.InputStream

class MainActivity : ComponentActivity() {
  //  override fun onCreate(savedInstanceState: Bundle?) {
        //super.onCreate(savedInstanceState)

        //setContentView(R.layout.layout)
        lateinit var imageView: ImageView
        lateinit var button: Button
        private val pickImage = 100
        private var imageUri: Uri? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.layout)
            title = "KotlinApp"
            imageView = findViewById(R.id.button)
            button = findViewById(R.id.button1)
            button.setOnClickListener {
                val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(gallery, pickImage)
            }
        }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == RESULT_OK && requestCode == pickImage) {
                imageUri = data?.data
                imageView.setImageURI(imageUri)
            }
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
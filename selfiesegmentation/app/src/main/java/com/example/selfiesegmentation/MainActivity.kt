package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.selfiesegmentation.databinding.LayoutBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var loadedBitmap : Bitmap
    private val viewModel : MainViewModel by viewModels()
private lateinit var binding: LayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        // Store the loaded bitmap here

        binding.button1.setOnClickListener {
            loader("photo1.jpg")
        }

        binding.button2.setOnClickListener {
            loadedBitmap.let {
                viewModel.processImageObjectDetection(it)
            }
        }
            binding.button3.setOnClickListener {
                loadedBitmap.let {
                    viewModel.selfie_segmentation(it)
                }
            }

        binding.blue.setOnClickListener {
            loadedBitmap?.let{
                viewModel.background(it,viewModel.maskBitmap.value!!,Color.BLUE)
                showToast("Blue BackgroundAdded")
            }
        }

        binding.purple.setOnClickListener {
            loadedBitmap?.let {
                viewModel.background(it,viewModel.maskBitmap.value!!,Color.MAGENTA)
                showToast("Purple BackgroundAdded")
            }
        }

        binding.red.setOnClickListener {
            loadedBitmap.let {
                viewModel?.background(it,viewModel.maskBitmap.value!!,Color.RED)
                showToast("Red BackgroundAdded")
            }
        }

        binding.yellow.setOnClickListener {
            loadedBitmap.let {
                viewModel.background(it,viewModel.maskBitmap.value!!,Color.YELLOW)
                showToast("Yellow BackgroundAdded")
            }
        }

        viewModel.bitmap.observe(this, Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
        viewModel.statusMessage.observe(this,Observer{message -> binding.textView.text = message})
        viewModel.maskBitmap.observe(this,Observer{bitmap -> binding.imageview.setImageBitmap(bitmap)})
        }

    private fun loader(fileName : String){
val assetManager = this.assets
        try{
            val inputStream = assetManager.open(fileName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            binding.imageview.setImageBitmap(bitmap)
                loadedBitmap = bitmap

            binding.textView.text = ""
        }
        catch (e: IOException){
            e.printStackTrace()
            showToast("failed to load image")
        }
    }

private fun showToast(message: String) = Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
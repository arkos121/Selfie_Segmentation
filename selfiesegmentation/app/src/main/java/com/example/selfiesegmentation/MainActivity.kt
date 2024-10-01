package com.example.selfiesegmentation

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.selfiesegmentation.databinding.LayoutBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var loadedBitmap : Bitmap
    private lateinit var imageView: ImageView
    private val viewModel : MainViewModel by viewModels()
private lateinit var binding: LayoutBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        // Store the loaded bitmap here
//        val spinner:Spinner = findViewById(R.id.dropdown)
//        val filters = arrayOf("Original", "B&W" , "Sepia")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinner.adapter = adapter
//
//
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                val selectedFilter = filters[position]
//                when (selectedFilter) {
//                    "Original" -> imageView.setImageBitmap(loadedBitmap)
//                    "B&W" -> viewModel.applyBlackAndWhiteFilter(loadedBitmap)
//                    "Sepia" -> viewModel.applySepiaFilter(loadedBitmap)
//                }
//                imageView.setImageBitmap(loadedBitmap)
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Do nothing when no selection is made
//            }
//        }

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
                viewModel.background(it,viewModel.maskBitmap.value!!,viewModel.calculateavgforeground(it,viewModel.maskBitmap.value!!))
                showToast("Contrasting color")
            }
        }
        binding.image.setOnClickListener {
          loadedBitmap.let{
              viewModel.setBackground(assets,"damn.jpg")
              viewModel.applyNewBackground(loadedBitmap,viewModel.maskBitmap.value!!)
          }

        }
        binding.bnw.setOnClickListener {
            loadedBitmap.let {
                viewModel.applyBlackAndWhiteFilter(it)

            }
        }
        binding.sepia.setOnClickListener {
            loadedBitmap.let {
                viewModel.applySepiaFilter(it)

            }
        }
        viewModel.bitmap.observe(this, Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
        viewModel.statusMessage.observe(this,Observer{message -> binding.textView.text = message})
        viewModel.maskBitmap.observe(this,Observer{bitmap -> binding.imageview.setImageBitmap(bitmap)})
        viewModel.backgrounds.observe(this,Observer{bitmap -> binding.imageview.setImageBitmap(bitmap)})
        viewModel.bnwBitmap.observe(this, {bitmap -> binding.imageview.setImageBitmap(bitmap)})
        viewModel.sepiaBitmap.observe(this, {bitmap -> binding.imageview.setImageBitmap(bitmap)})


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
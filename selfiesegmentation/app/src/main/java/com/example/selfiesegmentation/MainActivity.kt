package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.selfiesegmentation.databinding.LayoutBinding
import kotlinx.coroutines.withTimeout
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var loadedBitmap : Bitmap
    private lateinit var storedimg : Bitmap
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
            viewModel.bitmap.value.let {
                viewModel.applyBlackAndWhiteFilter(it!!)

            }
        }
        binding.sepia.setOnClickListener {
            viewModel.bitmap.value.let {
                viewModel.applySepiaFilter(it!!)

            }
        }
        viewModel.bitmap.observe(this, Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
        viewModel.statusMessage.observe(this,Observer{message -> binding.textView.text = message})
        viewModel.maskBitmap.observe(this,Observer{bitmap -> binding.imageview.setImageBitmap(bitmap)})
        viewModel.backgrounds.observe(this,Observer{bitmap -> binding.imageview.setImageBitmap(bitmap)})
        viewModel.bnwBitmap.observe(this, { bitmap -> binding.imageview.setImageBitmap(bitmap) })
        viewModel.sepiaBitmap.observe(this, { bitmap -> binding.imageview.setImageBitmap(bitmap) })


    }



    private fun loader(fileName : String){
        val startTime = System.currentTimeMillis()
val assetManager = this.assets
        try{
            val inputStream = assetManager.open(fileName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            binding.imageview.setImageBitmap(bitmap)
            loadedBitmap = bitmap
            storedimg = bitmap

            binding.textView.text = ""
        }
        catch (e: IOException){
            e.printStackTrace()
            showToast("failed to load image")
        }
        val endtime = System.currentTimeMillis()
        val total = endtime - startTime
        println("the time taken for uploading the image is $total")
    }

private fun showToast(message: String) = Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
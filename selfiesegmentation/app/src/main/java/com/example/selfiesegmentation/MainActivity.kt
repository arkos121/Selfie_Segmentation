package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.emptyLongSet
import androidx.lifecycle.Observer
import com.example.selfiesegmentation.databinding.LayoutBinding
import kotlinx.coroutines.withTimeout
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var loadedBitmap : Bitmap ?=null
    private lateinit var storedimg : Bitmap
    private val viewModel : MainViewModel by viewModels()
    private lateinit var binding: LayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        // Store the loaded bitmap here
        val filters = arrayOf("Default" , "Original", "B&W" , "Sepia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dropdown.adapter = adapter
        binding.dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedFilter = filters[position]
                when (selectedFilter) {
                    "Original" -> if(::storedimg.isInitialized){
                        viewModel.bitmap.value = storedimg
                        binding.imageview.setImageBitmap(storedimg)
                    }
                    else{
                        showToast("No image loaded")
                    }
                    "B&W" ->   viewModel.bitmap.value.let {
                        viewModel.applyBlackAndWhiteFilter(it!!) }
                    "Sepia" ->   viewModel.bitmap.value.let {
                        viewModel.applySepiaFilter(it!!)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding.button1.setOnClickListener {
            loader("photo1.jpg")
        }
        binding.button2.setOnClickListener {
            if(loadedBitmap!=null)
            loadedBitmap?.let {
                viewModel.processImageObjectDetection(it)
            }
            else
                showToast("No image loaded")
        }
            binding.button3.setOnClickListener {
                if (loadedBitmap!=null)
                loadedBitmap?.let {
                    viewModel.selfie_segmentation(it)
                }
                else
                    showToast("No image loaded")
            }
        binding.blue.setOnClickListener {
            loadedBitmap.let{
                if(it!=null && viewModel.maskBitmap.value!=null){
                viewModel.background(it,viewModel.maskBitmap.value!!,Color.BLUE)
                showToast("Blue BackgroundAdded")}
                else
                showToast("Mask is missing!")
            }
        }
        binding.purple.setOnClickListener {
            loadedBitmap.let {
                if(it!=null && viewModel.maskBitmap.value!=null){
                viewModel.background(it,viewModel.maskBitmap.value!!,Color.MAGENTA)
                showToast("Purple BackgroundAdded")}
                    else
                        showToast("Mask is missing!")
            }
        }

        binding.red.setOnClickListener {
            loadedBitmap.let {
                if(it!=null && viewModel.maskBitmap.value!=null){
                viewModel?.background(it,viewModel.maskBitmap.value!!,Color.RED)
                showToast("Red BackgroundAdded")}
                else
                    showToast("Mask is missing!")
            }
        }
        binding.yellow.setOnClickListener {
            loadedBitmap?.let {
                if(viewModel.maskBitmap.value!=null){
                viewModel.background(it,viewModel.maskBitmap.value!!,viewModel.calculateavgforeground(it,viewModel.maskBitmap.value!!))
                showToast("Contrasting color")}
                else
                    showToast("Mask is missing!")
            }
        }
        binding.image.setOnClickListener {
          loadedBitmap?.let{
              if(viewModel.maskBitmap.value!=null){
              viewModel.setBackground(assets,"damn.jpg")
              viewModel.applyNewBackground(loadedBitmap!!,viewModel.maskBitmap.value!!)
                  showToast("Image bg added")}
              else{
                  showToast("Mask is missing")
              }
          }
        }
        viewModel.bitmap.observe(this, Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
        viewModel.statusMessage.observe(this,Observer{message -> binding.textView.text = message})
        viewModel.maskBitmap.observe(this,Observer{bitmap -> binding.imageview.setImageBitmap(bitmap)})
        viewModel.backgrounds.observe(this,Observer{bitmap -> binding.imageview.setImageBitmap(bitmap)})
        viewModel.bnwBitmap.observe(this) { bitmap -> binding.imageview.setImageBitmap(bitmap) }
        viewModel.sepiaBitmap.observe(this) { bitmap -> binding.imageview.setImageBitmap(bitmap) }
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
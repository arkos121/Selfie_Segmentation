package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Color.BLUE
import android.graphics.Color.blue
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.emptyLongSet
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfiesegmentation.databinding.LayoutBinding
import kotlinx.coroutines.withTimeout
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var imageCounter = 0
    private var r: Bitmap? = null
    private var loadedBitmap: Bitmap? = null
    private lateinit var storedimg: Bitmap
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: LayoutBinding
    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        // Store the loaded bitmap here
        val filters = arrayOf("Default", "Original", "B&W", "Sepia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dropdown.adapter = adapter
        binding.dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedFilter = filters[position]
                when (selectedFilter) {
                    "Original" -> if (::storedimg.isInitialized) {
                        viewModel.bitmap.value = storedimg
                        binding.imageview.setImageBitmap(storedimg)
                    } else {
                        showToast("No image loaded")
                    }

                    "B&W" -> viewModel.bitmap.value.let {
                        viewModel.applyBlackAndWhiteFilter(it!!)
                    }

                    "Sepia" -> viewModel.bitmap.value.let {
                        viewModel.applySepiaFilter(it!!)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = MyAdapter(listOf("damn.jpg","emoji.png","glass.png"),this)


        binding.button1.setOnClickListener {
            val z = viewModel.loader(assets, "photo1.jpg")
            loadedBitmap = z
            storedimg = z!!
            binding.imageview.setImageBitmap(z)

        }

        binding.emoji.setOnClickListener {
            if(recyclerView.visibility==View.GONE){
                recyclerView.visibility=View.VISIBLE
            }else{
                recyclerView.visibility=View.GONE
            }
            fun addNewZoomableView(bitmap: Bitmap) {
                    var newImageView = ZoomableImageView(this, null).apply {
                        id = View.generateViewId()
                        tag = "image_${++imageCounter}"
                        }
                val lp=ConstraintLayout.LayoutParams(
                    binding.imageview.width,binding.imageview.height
                ).apply {
                    topToTop=binding.imageview.top
                    bottomToBottom=binding.imageview.bottom
                    startToStart=binding.imageview.left
                    endToEnd=binding.imageview.right
                }
                newImageView.layoutParams=lp
                newImageView.setImageBitmap(bitmap)
                binding.imageContainer.addView(newImageView )
                }
                r=viewModel.loader(assets,"glass.png")
                addNewZoomableView(r!!)
            }
            binding.button2.setOnClickListener {
                if (loadedBitmap != null)
                    loadedBitmap?.let {
                        viewModel.processImageObjectDetection(it)
                    }
                else
                showToast("No image loaded")
            }
            binding.button3.setOnClickListener {
                if (loadedBitmap != null)
                    loadedBitmap?.let {
                        viewModel.selfie_segmentation(it)
                    }
                else
                    showToast("No image loaded")
            }
            binding.blue.setOnClickListener {
                loadedBitmap.let {
                    if (it != null && viewModel.maskBitmap.value != null) {
                        viewModel.background(it, viewModel.maskBitmap.value!!, BLUE)
                        showToast("Blue BackgroundAdded")
                    } else
                        showToast("Mask is missing!")
                }
            }
            binding.purple.setOnClickListener {
                loadedBitmap.let {
                    if (it != null && viewModel.maskBitmap.value != null) {
                        viewModel.background(it, viewModel.maskBitmap.value!!, Color.MAGENTA)
                        showToast("Purple BackgroundAdded")
                    } else
                        showToast("Mask is missing!")
                }
            }

            binding.red.setOnClickListener {
                loadedBitmap.let {
                    if (it != null && viewModel.maskBitmap.value != null) {
                        viewModel?.background(it, viewModel.maskBitmap.value!!, Color.RED)
                        showToast("Red BackgroundAdded")
                    } else
                        showToast("Mask is missing!")
                }
            }
            binding.yellow.setOnClickListener {
                loadedBitmap?.let {
                    if (viewModel.maskBitmap.value != null) {
                        viewModel.background(
                            it,
                            viewModel.maskBitmap.value!!,
                            viewModel.calculateavgforeground(it, viewModel.maskBitmap.value!!)
                        )
                        showToast("Contrasting color")
                    } else
                        showToast("Mask is missing!")
                }
            }
            binding.image.setOnClickListener {
                loadedBitmap?.let {
                    if (viewModel.maskBitmap.value != null) {
                        viewModel.setBackground(assets, "damn.jpg")
                        viewModel.applyNewBackground(loadedBitmap!!, viewModel.maskBitmap.value!!)
                        showToast("Image bg added")
                    } else {
                        showToast("Mask is missing")
                    }
                }
            }
            viewModel.bitmap.observe(
                this,
                Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
            viewModel.statusMessage.observe(
                this,
                Observer { message -> binding.textView.text = message })
            viewModel.maskBitmap.observe(
                this,
                Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
            viewModel.backgrounds.observe(
                this,
                Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
            viewModel.bnwBitmap.observe(this) { bitmap -> binding.imageview.setImageBitmap(bitmap) }
            viewModel.sepiaBitmap.observe(this) { bitmap -> binding.imageview.setImageBitmap(bitmap) }
        }

}



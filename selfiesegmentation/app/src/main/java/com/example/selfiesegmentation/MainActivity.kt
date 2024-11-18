package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.BLUE
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfiesegmentation.databinding.LayoutBinding

class MainActivity : AppCompatActivity() {
    private var imageCounter = 0
    private var l = ""
    private var loadedBitmap: Bitmap? = null
    private lateinit var storedimg: Bitmap
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: LayoutBinding
    private val imagelo : ImageLoader = ImageLoader(this)
    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
    private fun copyImageLocation(imageloc : String) : String{
        Log.d("ImageLocation", "Clicked on image: $imageloc")
        l = imageloc
        return l
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
        val imageList = viewModel.staticImageList
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter(
           imageList, this, ::copyImageLocation
        )
        binding.button1.setOnClickListener {
            val z = imagelo.loader(assets,"photo1.jpg")
            loadedBitmap = z
            storedimg = z!!
            binding.imageview.setImageBitmap(z)

        }

        binding.emoji.setOnClickListener {
            if (recyclerView.visibility == View.GONE) {
                recyclerView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.GONE
            }


            fun addNewZoomableView(bitmap: Bitmap) {
                val newImageView = ZoomableImageView(this, null).apply {
                    id = View.generateViewId()
                    tag = "image_${++imageCounter}"
                    isClickable = true
                    isLongClickable = true
                    Log.d("ZoomableView", "Creating new view with tag: $tag")
                }

                val lp = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                ).apply {
                    topToTop = binding.imageContainer.id
                    bottomToBottom = binding.imageContainer.id
                    startToStart = binding.imageContainer.id
                    endToEnd = binding.imageContainer.id
                }

                newImageView.layoutParams = lp
                newImageView.setImageBitmap(bitmap)

                Log.d("ZoomableView", "Adding new ImageView to container")
                binding.imageContainer.addView(newImageView)
                binding.imageContainer.requestLayout()
            }

            if (l!="") {
              if (l.startsWith("/storage/") || l.startsWith("file://")) {
                    // Pass file path directly to `loader`
                  println("it is coming here")
                  l = l.substringAfterLast("/")
                  val b = imagelo.justLoad(l)
                println("on create $b")
                  if (b != null) {
                      println("is b there")
                      addNewZoomableView(b)
                      println("Bitmap loaded and added to ZoomableView")
                  } else {
                      println("Bitmap is null, failed to load image from $l")
                  }
              } else {
                    // Pass asset path directly to `loader`
                    var b = imagelo.loader(assets, l)
                  addNewZoomableView(b!!)
                }
                // Only add a new view if the bitmap was successfully loaded
//                bitmap?.let {
//                    addNewZoomableView(it)
//                }
                l =""
            }
        }

//        the view is added above the code
        var flag = false;
            binding.button2.setOnClickListener {
                if (loadedBitmap != null)
                    loadedBitmap?.let {
                        viewModel.processImageObjectDetection(it)
                        if(!flag){
                        MyAdapter(
                            imageList,
                            this,
                            ::copyImageLocation
                        ).updateData(viewModel.getImageListWithDynamicPath(imagelo))
                    }
                        flag = true
                    }
                else
                    showToast("No image loaded")
            }
        binding.button3.setOnClickListener {
            loadedBitmap?.let {
                viewModel.selfie_segmentation(it) // Trigger segmentation
            }


// Observe stickermap to save and retrieve when ready
            viewModel.stickermap.observe(this) { segmentedBitmap ->
                segmentedBitmap?.let {
                    imagelo.saveBitmapAsPNG(it, "hey") // Save the bitmap when it's available
                    println(imagelo.getImagePath("hey"))
                }
            }
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
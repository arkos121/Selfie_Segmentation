package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.BLUE
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfiesegmentation.databinding.ActivityBackgEmojiBinding
import com.example.selfiesegmentation.databinding.LayoutBinding
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api

class backg_emoji : AppCompatActivity() {
    private var loadedBitmap: Bitmap? = null
    private var l = ""
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var binding: ActivityBackgEmojiBinding
    private val imagelo: ImageLoader = ImageLoader(this)
    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun copyImageLocation(imageloc: String): String {
        Log.d("ImageLocation", "Clicked on image: $imageloc")
        l = imageloc
        return l
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBackgEmojiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedViewModel.centered.observe(this) { bitmap ->
            if (bitmap != null) {
                binding.imags.setImageBitmap(bitmap)
            } else
            {
                println("the null case")
            }
        }
        val imageList = sharedViewModel.staticImageList
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter(imageList, this, ::copyImageLocation)
        binding.emoi.setOnClickListener {
            if (recyclerView.visibility == View.GONE) {
                recyclerView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.GONE
            }


            var imageCounter = 0
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

            if (l != "") {
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
                    val b = imagelo.loader(assets, l)
                    addNewZoomableView(b!!)
                }
                // Only add a new view if the bitmap was successfully loaded
//                bitmap?.let {
//                    addNewZoomableView(it)
//                }
                l = ""
            }
        }
        binding.bgs.setOnClickListener {
            val options = arrayOf("Color", "Image")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Background")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        binding.group3.visibility = View.VISIBLE
                    }

                    1 -> {
                        binding.image.visibility = View.VISIBLE
                    }
                }
            }
                .show()
        }
        binding.button6.setOnClickListener {
            loadedBitmap.let {
                if (it != null && sharedViewModel.maskBitmap.value != null) {
                    sharedViewModel.background(it, sharedViewModel.maskBitmap.value!!, BLUE)
                    showToast("Blue BackgroundAdded")
                } else
                    showToast("Mask is missing!")
            }
        }
        binding.button7.setOnClickListener{

            loadedBitmap.let {
                if (it != null && sharedViewModel.maskBitmap.value != null) {
                    sharedViewModel.background(it, sharedViewModel.maskBitmap.value!!, Color.MAGENTA)
                    showToast("Purple BackgroundAdded")
                } else
                    showToast("Mask is missing!")
            }
        }
        binding.button8.setOnClickListener{
            loadedBitmap.let {
                if (it != null && sharedViewModel.maskBitmap.value != null) {
                    sharedViewModel.background(it, sharedViewModel.maskBitmap.value!!, Color.RED)
                    showToast("Red BackgroundAdded")
                } else
                    showToast("Mask is missing!")
            }
        }
        binding.button9.setOnClickListener{
            loadedBitmap?.let {
                if (sharedViewModel.maskBitmap.value != null) {
                    sharedViewModel.background(
                        it,
                        sharedViewModel.maskBitmap.value!!,
                        sharedViewModel.calculateavgforeground(it, sharedViewModel.maskBitmap.value!!)
                    )
                    showToast("Contrasting color")
                } else
                    showToast("Mask is missing!")
            }
        }
        binding.image.setOnClickListener{
            loadedBitmap?.let {
                if (sharedViewModel.maskBitmap.value != null) {
                    sharedViewModel.setBackground(assets, "damn.jpg")
                    sharedViewModel.applyNewBackground(loadedBitmap!!, sharedViewModel.maskBitmap.value!!)
                    showToast("Image bg added")
                } else {
                    showToast("Mask is missing")
                }
            }
        }
    }
}

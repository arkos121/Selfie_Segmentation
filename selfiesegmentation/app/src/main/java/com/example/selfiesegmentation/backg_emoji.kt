package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfiesegmentation.databinding.ActivityBackgEmojiBinding
import androidx.appcompat.app.AlertDialog
import androidx.core.view.drawToBitmap

class backg_emoji : AppCompatActivity() {

    private lateinit var binding: ActivityBackgEmojiBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private val imagelo: ImageLoader = ImageLoader(this)
    private var selectedBackgroundColor: Int = Color.WHITE
    private var imageCounter = 0
    private var l : String= ""

    fun copyImageLocation(s: String) : String {
        l = s
       return l
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackgEmojiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCanvas()
        setupRecyclerView()
        setupBackgroundOptions()
        setupSave()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupSave() {
        binding.savefinal.setOnClickListener{
            imagelo.saveBitmapAsPNG(binding.canvas.drawToBitmap(), "final-image-${System.currentTimeMillis() / 100}")
            imagelo.saveImageToGallery(binding.canvas.drawToBitmap(), this, "final-${System.currentTimeMillis()}")
        }
    }


    private fun setupCanvas() {
        // Initialize canvas with white background
        binding.canvas.setBackgroundColor(selectedBackgroundColor)
    }

    private fun setupRecyclerView() {
        val imageList = sharedViewModel.staticImageList
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
      //copyImageLocation(imagelo.getImagePath("stick"))

        val lis = sharedViewModel.getImageListWithDynamicPath(imagelo)
        recyclerView.adapter = MyAdapter(lis, this, ::copyImageLocation)

        binding.toggleStickersButton.setOnClickListener {

            binding.recyclerView.visibility =
                if (binding.recyclerView.visibility == View.GONE) View.VISIBLE else View.GONE

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
                    topToTop = binding.canvas.id
                    bottomToBottom = binding.canvas.id
                    startToStart = binding.canvas.id
                    endToEnd = binding.canvas.id
                }

                newImageView.layoutParams = lp
                newImageView.setImageBitmap(bitmap)

                binding.canvas.addView(newImageView)
                binding.stickerOverlay.requestLayout()
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
                    l = ""
                }
            }
        }


var fl = false
    //var imageList = sharedViewModel.staticImageList
    private fun setupBackgroundOptions() {
        binding.changeBackgroundButton.setOnClickListener {
if(!fl) {
    MyAdapter(
        sharedViewModel.getImageListWithDynamicPath(imagelo),
        this,
        ::copyImageLocation
    )
    val options = arrayOf("Color", "Image")
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Select Background")
    builder.setItems(options) { _, which ->
        when (which) {
            0 -> showColorPicker()
            1 -> selectImageBackg()
        }
    }.show()
}
            fl = true
        }
    }



    private fun selectImageBackg() {
        // Simulate picking an image (modify with real implementation if needed)
        val selectedImage = imagelo.loader(assets, "damn.jpg")
        if (selectedImage != null) {
            binding.canvas.background = null // Clear color background
            binding.stickerOverlay.setImageBitmap(selectedImage)
        } else {
            showToast("Failed to load background image")
        }
    }

    private fun showColorPicker() {
        val colors = arrayOf("White", "Blue", "Red", "Green", "Yellow")
        val colorval = arrayOf(Color.WHITE, Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW)

        AlertDialog.Builder(this)
            .setTitle("Pick a color")
            .setItems(colors) { _, which ->
                selectedBackgroundColor = colorval[which]
                binding.canvas.setBackgroundColor(selectedBackgroundColor)
            }.show()


    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    }



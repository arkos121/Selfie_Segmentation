package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfiesegmentation.databinding.ActivityBackgEmojiBinding
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.drawToBitmap

class backg_emoji : AppCompatActivity() {

    private lateinit var binding: ActivityBackgEmojiBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private val imagelo: ImageLoader = ImageLoader(this)
    private var selectedBackgroundColor: Int = Color.WHITE
    private var imageCounter = 0
    private var l : String= ""
    private var its : Bitmap ?=null
    private var c = 0

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
        setUpFilter()
    }
    private fun setUpFilter() {
        binding.FILTERS.setOnClickListener {
            // Filter options
            val filterOptions = arrayOf("Original", "Grayscale", "Sepia", "Blur", "Vignette", "High Contrast", "Bright", "Warm")
            // Capture the current state of the entire canvas and overlay
            val capturedBitmap = binding.canvas.getBitmapFromView()
            // Show filter selection dialog
            AlertDialog.Builder(this)
                .setTitle("Choose Filter")
                .setItems(filterOptions) { _, which ->
                    val filteredBitmap = when (which) {
                        0 -> its // Original
                        1 -> Filters.applyGrayscale(capturedBitmap) // Grayscale
                        2 -> Filters.applySepia(capturedBitmap) // Sepia
                        3 -> Filters.applyBlur(capturedBitmap, 10f) // Blur
                        4 -> Filters.applyVignette(capturedBitmap) // Vignette
                        5 -> Filters.applyContrast(capturedBitmap, 0.5f) // High Contrast
                        6 -> Filters.applyBrightness(capturedBitmap, 30f) // Bright
                        7 -> Filters.applyTint(capturedBitmap, 0xFFFF9800.toInt(), 0.2f) // Warm
                        else -> capturedBitmap
                    }
                    binding.stickerOverlay.setImageBitmap(filteredBitmap)// Apply filtered image
                    removeAllDynamicImageViews()
                    its = filteredBitmap
                    Toast.makeText(this, "${filterOptions[which]} filter applied!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
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
                c++
                its = binding.canvas.drawToBitmap()
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

    fun removeAllDynamicImageViews() {
        // Collect views to remove
        val viewsToRemove = binding.canvas.children
            .filter { it is ImageView && it.tag?.toString()?.startsWith("image_") == true }
            .toList()

        // Remove collected views
        viewsToRemove.forEach { view ->
            binding.canvas.removeView(view)
            Log.d("RemoveView", "Removed view with tag: ${view.tag}")
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
            its = binding.canvas.drawToBitmap()
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
                its = binding.canvas.drawToBitmap()
            }.show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    fun View.getBitmapFromView(): Bitmap {
        val bitmap = Bitmap.createBitmap(this.measuredWidth, this.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap}
    }



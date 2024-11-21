package com.example.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfiesegmentation.databinding.ActivityBackgEmojiBinding
import androidx.appcompat.app.AlertDialog
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

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
                    // Only add a new view if the bitmap was successfully loaded
//                bitmap?.let {
//                    addNewZoomableView(it)
//                }
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

//        Log.d("backg_emoji", "ViewModel initialized")

//        // Observe the bitmap LiveData
//        sharedViewModel.bitmap.observe(this, Observer { bitmap ->
//            Log.d("backg_emoji", "Observer triggered, Bitmap: $bitmap")
//            if (bitmap != null) {
//                binding.imags.setImageBitmap(bissuming you have an ImageView to show the bitmap
//            } else {
////                Log.d("backg_emoji", "Bitmap is null in backg_emoji activity")
////            }
////        })
//        val bits = MainActivity.BitmapHolder.bitmap
////        sharedViewModel.bitmap.observe(this, Observer { bis->
////            binding.imags.setImageBitmap(bis)
////        })
//        val ori = MainActivity.InitialHolder.bitmap
//        binding.imags.setImageBitmap(bits)
//
//        val imageList = sharedViewModel.staticImageList
//        val recyclerView = binding.recyclerView
//        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        recyclerView.layoutManager = layoutManager
//        recyclerView.adapter = MyAdapter(imageList, this, ::copyImageLocation)
//        binding.emoi.setOnClickListener {
//            if (recyclerView.visibility == View.GONE) {
//                recyclerView.visibility = View.VISIBLE
//            } else {
//                recyclerView.visibility = View.GONE
//            }
//
//
////            var imageCounter = 0
//            fun addNewZoomableView(bitmap: Bitmap) {
//                val newImageView = ZoomableImageView(this, null).apply {
//                    id = View.generateViewId()
//
//                    tag = "image_${++imageCounter}"
//                    isClickable = true
//                    isLongClickable = true
//                    Log.d("ZoomableView", "Creating new view with tag: $tag")
//                }
//
//                val lp = ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
//                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
//                ).apply {
//                    topToTop = binding.imageContainer.id
//                    bottomToBottom = binding.imageContainer.id
//                    startToStart = binding.imageContainer.id
//                    endToEnd = binding.imageContainer.id
//                }
//
//                newImageView.layoutParams = lp
//                newImageView.setImageBitmap(bitmap)
//
//                Log.d("ZoomableView", "Adding new ImageView to container")
//                binding.imageContainer.addView(newImageView)
//                binding.imageContainer.requestLayout()
//            }
//
//            if (l != "") {
//                if (l.startsWith("/storage/") || l.startsWith("file://")) {
//                    // Pass file path directly to `loader`
//                    println("it is coming here")
//                    l = l.substringAfterLast("/")
//                    val b = imagelo.justLoad(l)
//                    println("on create $b")
//                    if (b != null) {
//                        println("is b there")
//                        addNewZoomableView(b)
//                        println("Bitmap loaded and added to ZoomableView")
//                    } else {
//                        println("Bitmap is null, failed to load image from $l")
//                    }
//                } else {
//                    // Pass asset path directly to `loader`
//                    val b = imagelo.loader(assets, l)
//                    addNewZoomableView(b!!)
//                }
//                // Only add a new view if the bitmap was successfully loaded
////                bitmap?.let {
////                    addNewZoomableView(it)
////                }
//                l = ""
//            }
//        }
//        binding.bgs.setOnClickListener {
//            val options = arrayOf("Color", "Image")
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("Select Background")
//            builder.setItems(options) { _, which ->
//                when (which) {
//                    0 -> {
//                        binding.group3.visibility = View.VISIBLE
//                    }
//
//                    1 -> {
//                        binding.image.visibility = View.VISIBLE
//                    }
//                }
//            }
//                .show()
//        }
//        binding.button6.setOnClickListener {
//
//                val scaledMask = Bitmap.createScaledBitmap(bits!!, ori!!.width, ori.height, false)
//
//                val width = ori.width
//                val height = ori.height
//                val resultPixels = IntArray(width * height)
//               // val stickerPixels = IntArray(width * height)
//
//                for (y in 0 until height) {
//                    for (x in 0 until width) {
//                        val index = y * width + x
//                        val maskPixel = scaledMask.getPixel(x, y)
//
//                        // Mask alpha (grayscale value) is used to determine foreground
//                        val maskAlpha = Color.alpha(maskPixel)
//
//                        if (maskAlpha > 128) { // Foreground
//                            val originalPixel = ori.getPixel(x, y)
//
//                            // Set the pixel with maximum opacity
//                            resultPixels[index] = Color.argb(255, Color.red(originalPixel), Color.green(originalPixel), Color.blue(originalPixel))
//                          //  stickerPixels[index] = resultPixels[index]
//                        } else { // Background
//                            resultPixels[index] = BLUE // Use provided background color
//                           // stickerPixels[index] = Color.TRANSPARENT
//                        }
//                    }
//                }
//
//                val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//               // val sticker = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//                resultBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
//               // sticker.setPixels(stickerPixels, 0, width, 0, 0, width, height)
//
//                binding.imags.setImageBitmap(resultBitmap)
//            }
//        }
////        binding.button7.setOnClickListener {
////            MainActivity.InitialHolder.bitmap.let {
////                if (it != null && bits != null) {
////                    sharedViewModel.background(it, bits, Color.MAGENTA)
////                    showToast("Purple Background Added")
////                } else
////                    showToast("Mask is missing!")
////            }
////        }
////        binding.button8.setOnClickListener {
////            MainActivity.InitialHolder.bitmap.let {
////                if (it != null && bits != null) {
////                    sharedViewModel.background(it, bits, Color.RED)
////                    showToast("Red Background Added")
////                } else
////                    showToast("Mask is missing!")
////            }
////        }
////        binding.button9.setOnClickListener {
////            MainActivity.InitialHolder.bitmap?.let {
////                if (bits != null) {
////                    sharedViewModel.background(
////                        it,
////                        bits,
////                        sharedViewModel.calculateavgforeground(it, bits)
////                    )
////                    showToast("Contrasting color")
////                } else
////                    showToast("Mask is missing!")
////            }
////        }
////        binding.image.setOnClickListener {
////            MainActivity.InitialHolder?.let {
////                if (bits != null) {
////                    sharedViewModel.setBackground(assets, "damn.jpg")
////                    sharedViewModel.applyNewBackground(MainActivity.InitialHolder.bitmap!!, bits)
////                    showToast("Image background added")
////                } else {
////                    showToast("Mask is missing!")
////                }
////            }
////        }
////
////// Ensure LiveData kmap contains the outcome of the function where applicable
////         sharedViewModel.kmap.observe(this) { result ->
////            binding.imags.setImageBitmap(result)
////        }
////    }
//}

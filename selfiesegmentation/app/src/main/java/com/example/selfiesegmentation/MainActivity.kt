package com.example.selfiesegmentation

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfiesegmentation.databinding.LayoutBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private var imageCounter = 0
    private companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoUri: Uri
    private var l = ""
    private var loadedBitmap: Bitmap? = null
    private lateinit var storedimg: Bitmap
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: LayoutBinding
    private val imagelo: ImageLoader = ImageLoader(this)
    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun copyImageLocation(imageloc: String): String {
        Log.d("ImageLocation", "Clicked on image: $imageloc")
        l = imageloc
        return l
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        initializeLaunchers()
        // Store the loaded bitmap here
        val filters = arrayOf("Default", "Original", "B&W", "Sepia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //    binding.dropdown.adapter = adapter
//        binding.dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                val selectedFilter = filters[position]
//                when (selectedFilter) {
//                    "Original" -> if (::storedimg.isInitialized) {
//                        viewModel.bitmap.value = storedimg
//                        binding.imageview.setImageBitmap(storedimg)
//                    } else {
//                        showToast("No image loaded")
//                    }
//
//                    "B&W" -> viewModel.bitmap.value.let {
//                        viewModel.applyBlackAndWhiteFilter(it!!)
//                    }
//
//                    "Sepia" -> viewModel.bitmap.value.let {
//                        viewModel.applySepiaFilter(it!!)
//                    }
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//            }
//        }


        val imageList = viewModel.staticImageList
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter(imageList, this, ::copyImageLocation)

//            val z = imagelo.loader(assets,"photo1.jpg")
//            loadedBitmap = z
//            storedimg = z!!
//            binding.imageview.setImageBitmap(z)
            binding.button1.setOnClickListener {
//                val options = arrayOf("Camera", "Gallery")
//                val builder = AlertDialog.Builder(this)
//                builder.setTitle("Choose an option")
//                builder.setItems(options) { _, which ->
//                    when (which) {
//                        0 -> openCamera()
//                        1 -> openGallery()
//                    }
//                }
//                builder.show()
//            }
                showImageSourceDialog()



        }

        binding.saver.setOnClickListener{
            imagelo.saveImageToGallery(viewModel.cropBitmap(viewModel.stickermap.value!!,viewModel.box.left,viewModel.box.top,viewModel.box.width(),viewModel.box.height()),this,"image-${System.currentTimeMillis()/100}")

        }

//        binding.emoji.setOnClickListener {
//            if (recyclerView.visibility == View.GONE) {
//                recyclerView.visibility = View.VISIBLE
//            } else {
//                recyclerView.visibility = View.GONE
//            }
//
//
//
//            fun addNewZoomableView(bitmap: Bitmap) {
//                val newImageView = ZoomableImageView(this, null).apply {
//                    id = View.generateViewId()
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
//                    var b = imagelo.loader(assets, l)
//                    addNewZoomableView(b!!)
//                }
//                // Only add a new view if the bitmap was successfully loaded
////                bitmap?.let {
////                    addNewZoomableView(it)
////                }
//                l = ""
//            }
//        }

//        the view is added above the code
        var flag = false
        binding.button2.setOnClickListener {
            if (loadedBitmap != null)
                loadedBitmap?.let {
                    viewModel.processImageObjectDetection(it)
                    if (!flag) {
                        MyAdapter(
                            imageList,
                            this,
                            ::copyImageLocation
                        ).updateData(viewModel.getImageListWithDynamicPath(imagelo))
                        flag = true
                    }

                }
            else
                showToast("No image loaded")
        }
        binding.button3.setOnClickListener {
          //  binding.emoji.visibility = View.VISIBLE
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
            binding.saver.visibility = View.VISIBLE
        }
//        binding.blue.setOnClickListener {
//            loadedBitmap.let {
//                if (it != null && viewModel.maskBitmap.value != null) {
//                    viewModel.background(it, viewModel.maskBitmap.value!!, BLUE)
//                    showToast("Blue BackgroundAdded")
//                } else
//                    showToast("Mask is missing!")
//            }
//        }
//        binding.purple.setOnClickListener {
//            loadedBitmap.let {
//                if (it != null && viewModel.maskBitmap.value != null) {
//                    viewModel.background(it, viewModel.maskBitmap.value!!, Color.MAGENTA)
//                    showToast("Purple BackgroundAdded")
//                } else
//                    showToast("Mask is missing!")
//            }
//        }
//
//        binding.red.setOnClickListener {
//            loadedBitmap.let {
//                if (it != null && viewModel.maskBitmap.value != null) {
//                    viewModel?.background(it, viewModel.maskBitmap.value!!, Color.RED)
//                    showToast("Red BackgroundAdded")
//                } else
//                    showToast("Mask is missing!")
//            }
//        }
//        binding.yellow.setOnClickListener {
//            loadedBitmap?.let {
//                if (viewModel.maskBitmap.value != null) {
//                    viewModel.background(
//                        it,
//                        viewModel.maskBitmap.value!!,
//                        viewModel.calculateavgforeground(it, viewModel.maskBitmap.value!!)
//                    )
//                    showToast("Contrasting color")
//                } else
//                    showToast("Mask is missing!")
//            }
//        }
//        binding.image.setOnClickListener {
//            loadedBitmap?.let {
//                if (viewModel.maskBitmap.value != null) {
//                    viewModel.setBackground(assets, "damn.jpg")
//                    viewModel.applyNewBackground(loadedBitmap!!, viewModel.maskBitmap.value!!)
//                    showToast("Image bg added")
//                } else {
//                    showToast("Mask is missing")
//                }
//            }
//        }
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

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkAndRequestPermissions() // Check permissions before opening the camera
                    1 -> openGallery()
                }
            }.show()
    }
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(android.Manifest.permission.CAMERA)
        val missingPermissions = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            // Permissions are already granted, proceed with camera
            openCamera()
        }
    }
    private fun openCamera() {
        val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        try {
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Log.e("CameraError", "Failed to launch camera", e)
            showToast("Failed to open camera. Please try again.")
        }
    }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }


    private fun initializeLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri))
                val scaledBitmap = viewModel.scaleDownBitmap(originalBitmap, 800, 800) // Scale down to 800x800 max
                binding.imageview.setImageBitmap(scaledBitmap)
               // savebits(scaledBitmap)
                loadedBitmap = scaledBitmap
            }
        }


        // Gallery launcher
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                val originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
                val scaledBitmap = viewModel.scaleDownBitmap(originalBitmap, 800, 800) // Scale down to 800x800 max
                binding.imageview.setImageBitmap(scaledBitmap)
              //  savebits(scaledBitmap)
                loadedBitmap = scaledBitmap
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFinalImageWithSticker() {
        // Assuming the image with sticker is in the ImageView
        val finalBitmap = viewModel.stickermap.value!!// Get the current view as Bitmap
        imagelo.saveImageToGallery(finalBitmap, this, "final_image") // Save the final Bitmap
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas) // Draw the view into the canvas
        return bitmap
    }





}
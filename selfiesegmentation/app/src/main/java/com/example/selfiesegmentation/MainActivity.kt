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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selfiesegmentation.databinding.LayoutBinding
import java.io.File
import java.io.Serializable

class MainActivity : AppCompatActivity(),Serializable {
    private companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
    object BitmapHolder {
        var bitmap: Bitmap? = null
    }

    object InitialHolder{
        var bitmap : Bitmap ?= null
    }
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoUri: Uri
    private var l = ""
    private var loadedBitmap: Bitmap? = null
    var bits : Bitmap ?=null
    private lateinit var storedimg: Bitmap
    val sharedViewModel: SharedViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
//    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var binding: LayoutBinding
    private val imagelo: ImageLoader = ImageLoader(this)
    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
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


          binding.original.setImageBitmap(InitialHolder.bitmap)
            binding.button1.setOnClickListener {
                showImageSourceDialog()
        }
        binding.saver.setOnClickListener{
            imagelo.saveImageToGallery(sharedViewModel.cropBitmap(sharedViewModel.stickermap.value!!,sharedViewModel.boxs.left,sharedViewModel.boxs.top,sharedViewModel.boxs.width(),sharedViewModel.boxs.height()),this,"image-${System.currentTimeMillis()/100}")
            imagelo.saveBitmapAsPNG(sharedViewModel.cropBitmap(sharedViewModel.stickermap.value!!,sharedViewModel.boxs.left,sharedViewModel.boxs.top,sharedViewModel.boxs.width(),sharedViewModel.boxs.height()),"stick")
            println("the path is ${imagelo.getImagePath("stick")}")
        }

//        binding.button2.setOnClickListener {
//
//        }
        binding.button3.setOnClickListener {
            var f = false
            if (loadedBitmap != null)
                loadedBitmap?.let {
                    sharedViewModel.processImageObjectDetection(it)
                    f = true
                }
            else {
                showToast("No image loaded")
            }
            if(f) {
                loadedBitmap?.let {
                    sharedViewModel.selfie_segmentation(it)
                }

// Observe stickermap to save and retrieve when ready
                sharedViewModel.stickermap.observe(this) { segmentedBitmap ->
                    segmentedBitmap?.let {
                        imagelo.saveBitmapAsPNG(it, "hey") // Save the bitmap when it's available
                        println(imagelo.getImagePath("hey"))
                    }
                }

                binding.saver.visibility = View.VISIBLE
            }
        }
        binding.switch1.setOnClickListener{
            bits = getBitmapFromView(binding.imageview)
            sharedViewModel.updateBitmap(bits)
            BitmapHolder.bitmap = sharedViewModel.bitmap.value
            val intent = Intent(this, backg_emoji::class.java)
            startActivity(intent)
        }
        sharedViewModel.bitmap.observe(
            this,
            Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
        sharedViewModel.statusMessage.observe(
            this,
            Observer { message -> binding.textView.text = message })
        sharedViewModel.maskBitmap.observe(
            this,
            Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
//        sharedViewModel.backgrounds.observe(
//            this,
//            Observer { bitmap -> binding.imageview.setImageBitmap(bitmap) })
        sharedViewModel.bnwBitmap.observe(this) { bitmap -> binding.imageview.setImageBitmap(bitmap) }
        sharedViewModel.sepiaBitmap.observe(this) { bitmap -> binding.imageview.setImageBitmap(bitmap) }

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
                val scaledBitmap = sharedViewModel.scaleDownBitmap(originalBitmap, 800, 800) // Scale down to 800x800 max
                binding.imageview.setImageBitmap(scaledBitmap)
               // savebits(scaledBitmap)
                loadedBitmap = scaledBitmap
                binding.original.setImageBitmap(loadedBitmap)
                InitialHolder.bitmap = scaledBitmap
            }
        }


        // Gallery launcher
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                val originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
                val scaledBitmap = sharedViewModel.scaleDownBitmap(originalBitmap, 800, 800) // Scale down to 800x800 max
                binding.imageview.setImageBitmap(scaledBitmap)
              //  savebits(scaledBitmap)
                loadedBitmap = scaledBitmap
                binding.original.setImageBitmap(loadedBitmap)
                InitialHolder.bitmap = scaledBitmap
            }
        }

    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas) // Draw the view into the canvas
        return bitmap
    }

}
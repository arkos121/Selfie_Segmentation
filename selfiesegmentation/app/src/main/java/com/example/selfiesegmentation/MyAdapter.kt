package com.example.selfiesegmentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException

class MyAdapter(
    private var dataListitem: List<String>,
    private var context: Context,
    private var copyImageLocation: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = dataListitem.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val imageLocation = dataListitem[position]
        val myHolder = holder as MyViewHolder
        val bitmap = getBitmapFromUri(imageLocation)

        if (bitmap != null) {
            myHolder.imageView.setImageBitmap(bitmap)
        }

        myHolder.imageView.setOnClickListener {
            copyImageLocation(imageLocation)
        }
    }

    private fun getBitmapFromUri(uri: String): Bitmap? {
        return try {
            when {
                uri.startsWith("/storage/") -> {
                    // Load from file path
                   // val filePath = uri.removePrefix("file://")
                    BitmapFactory.decodeFile(uri)
                }
                else -> {
                    // Load from assets as default
                    context.assets.open(uri).use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

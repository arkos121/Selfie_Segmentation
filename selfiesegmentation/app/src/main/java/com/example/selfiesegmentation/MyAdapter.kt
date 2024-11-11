package com.example.selfiesegmentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

class MyAdapter(private var dataListitem: List<String>, private var context: Context,private var copyImageLocation : (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

     class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
         val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataListitem.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       val bitmap = getBitmapFromUri(dataListitem[position])
        val imageloc = dataListitem[position]
        (holder as MyViewHolder).imageView.setImageBitmap(bitmap)

        holder.imageView.setOnClickListener {
            copyImageLocation(imageloc)
        }
    }

    private fun getBitmapFromUri(s: String): Bitmap?
    {
        return try {
            val assetManager = context.assets
            val inputStream = assetManager.open(s)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

}

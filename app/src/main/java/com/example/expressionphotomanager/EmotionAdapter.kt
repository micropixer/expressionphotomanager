package com.example.expressionphotomanager

import ImageAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EmotionAdapter(
    private val context: Context,
    private val data: Map<String, MutableList<Pair<Int, ByteArray>>>,
    private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<EmotionAdapter.EmotionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmotionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_emotion_section, parent, false)
        return EmotionViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmotionViewHolder, position: Int) {
        val emotion = data.keys.elementAt(position)
        val images = data[emotion]

        holder.textViewEmotionHeader.text = emotion
        holder.recyclerViewImages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        holder.recyclerViewImages.adapter = ImageAdapter(context, images ?: mutableListOf(), onImageClick)
    }

    override fun getItemCount(): Int = data.size

    class EmotionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewEmotionHeader: TextView = view.findViewById(R.id.textViewEmotionHeader)
        val recyclerViewImages: RecyclerView = view.findViewById(R.id.recyclerViewImages)
    }
}

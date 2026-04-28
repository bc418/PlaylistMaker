package com.practicum.playlistmaker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TracksViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {

    private val trackName: TextView = itemView.findViewById(R.id.trackName)
    private val trackArtwork: ImageView = itemView.findViewById(R.id.trackArtwork)

    private val trackTime: TextView = itemView.findViewById(R.id.trackTime)

    private val artistName: TextView = itemView.findViewById(R.id.artistName)

    fun bind(model: Track) {
        trackName.text = model.trackName
        trackTime.text = model.trackTime
        artistName.text = model.artistName

        Glide.with(itemView.context)
            .load(model.artworkUrl100)
            .centerCrop()
            .transform(RoundedCorners(10))
            .placeholder(R.drawable.ic_search_placeholder_45)
            .into(trackArtwork)
    }

}
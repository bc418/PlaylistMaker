package com.practicum.playlistmaker.presentation.playlistdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.search.TracksViewHolder

class PlaylistTracksAdapter(
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit
) : RecyclerView.Adapter<TracksViewHolder>() {
    private val items = mutableListOf<Track>()

    fun submitList(tracks: List<Track>) {
        items.clear()
        items.addAll(tracks)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksViewHolder =
        TracksViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tracks_view, parent, false))

    override fun onBindViewHolder(holder: TracksViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)
        holder.itemView.setOnClickListener { onTrackClick(track) }
        holder.itemView.setOnLongClickListener {
            onTrackLongClick(track)
            true
        }
    }
}

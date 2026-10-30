package com.practicum.playlistmaker.presentation.media.playlists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Playlist
import java.io.File

class PlaylistGridAdapter : RecyclerView.Adapter<PlaylistGridAdapter.PlaylistViewHolder>() {
    private val items = mutableListOf<Playlist>()

    fun submitList(playlists: List<Playlist>) {
        items.clear()
        items.addAll(playlists)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder =
        PlaylistViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_grid, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) = holder.bind(items[position])

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cover = view.findViewById<ImageView>(R.id.playlistGridCover)
        private val name = view.findViewById<TextView>(R.id.playlistGridName)
        private val count = view.findViewById<TextView>(R.id.playlistGridCount)
        fun bind(playlist: Playlist) {
            name.text = playlist.name
            count.text = itemView.resources.getQuantityString(
                R.plurals.playlist_track_count,
                playlist.trackCount,
                playlist.trackCount
            )

            Glide.with(cover).clear(cover)

            val coverFile = playlist.coverPath?.let(::File)

            if (coverFile == null || !coverFile.isFile) {
                val padding = (12 * itemView.resources.displayMetrics.density).toInt()

                cover.setBackgroundResource(R.drawable.bg_playlist_grid_empty)
                cover.setPadding(padding, padding, padding, padding)
                cover.scaleType = ImageView.ScaleType.FIT_XY
                cover.setImageResource(R.drawable.ic_search_placeholder_45)
            } else {
                cover.setBackgroundResource(R.drawable.bg_playlist_grid_cover)
                cover.setPadding(0, 0, 0, 0)
                cover.scaleType = ImageView.ScaleType.CENTER_CROP

                Glide.with(cover)
                    .load(coverFile)
                    .centerCrop()
                    .transform(
                        RoundedCorners(
                            (8 * itemView.resources.displayMetrics.density).toInt()
                        )
                    )
                    .into(cover)
            }
        }
    }
}

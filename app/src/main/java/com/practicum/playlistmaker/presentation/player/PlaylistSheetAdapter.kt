package com.practicum.playlistmaker.presentation.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Playlist
import androidx.core.content.ContextCompat
import java.io.File

class PlaylistSheetAdapter(private val onClick: (Playlist) -> Unit) :
    RecyclerView.Adapter<PlaylistSheetAdapter.SheetViewHolder>() {
    private val items = mutableListOf<Playlist>()

    fun submitList(newItems: List<Playlist>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SheetViewHolder =
        SheetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_sheet, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SheetViewHolder, position: Int) = holder.bind(items[position], onClick)

    class SheetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cover = view.findViewById<ImageView>(R.id.sheetPlaylistCover)
        private val name = view.findViewById<TextView>(R.id.sheetPlaylistName)
        private val count = view.findViewById<TextView>(R.id.sheetPlaylistCount)
        fun bind(playlist: Playlist, onClick: (Playlist) -> Unit) {
            name.text = playlist.name
            name.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.playlist_sheet_text_color
                )
            )

            count.text = itemView.resources.getQuantityString(
                R.plurals.playlist_track_count,
                playlist.trackCount,
                playlist.trackCount
            )

            Glide.with(cover).clear(cover)

            val coverFile = playlist.coverPath
                ?.takeIf { it.isNotBlank() }
                ?.let(::File)
                ?.takeIf { it.isFile }

            if (coverFile == null) {
                cover.scaleType = ImageView.ScaleType.CENTER
                cover.setImageResource(R.drawable.ic_search_placeholder_45)
            } else {
                cover.scaleType = ImageView.ScaleType.CENTER_CROP

                Glide.with(cover)
                    .load(coverFile)
                    .centerCrop()
                    .into(cover)
            }

            itemView.setOnClickListener {
                onClick(playlist)
            }
        }
    }
}

package com.practicum.playlistmaker.presentation.playlistdetail

import android.content.Intent
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Gravity
import android.view.Window
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.graphics.Outline
import android.widget.ImageView
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.PlaylistDetails
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.player.PlayerFragment
import java.io.File
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailFragment : Fragment() {

    private val viewModel by viewModel<PlaylistDetailViewModel>()
    private val playlistId: Long by lazy { requireArguments().getLong(PLAYLIST_ID) }
    private var currentDetails: PlaylistDetails? = null
    private var menuBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_playlist_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Keep the large playlist cover square on phones of different widths.
        val coverFrame = view.findViewById<ImageView>(R.id.detailCover).parent as FrameLayout
        coverFrame.doOnLayout { frame ->
            if (frame.height != frame.width) frame.updateLayoutParams { height = frame.width }
        }
        view.findViewById<View>(R.id.detailBack).setOnClickListener { findNavController().popBackStack() }
        view.findViewById<View>(R.id.detailShare).setOnClickListener { sharePlaylist() }
        view.findViewById<View>(R.id.detailMenu).setOnClickListener { showMenu() }

        // Keep the 45dp cover rounded in both themes and for placeholders.
        view.findViewById<ImageView>(R.id.detailMenuCover).apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val radius = 4f * view.resources.displayMetrics.density
                    outline.setRoundRect(0, 0, view.width, view.height, radius)
                }
            }
            clipToOutline = true
        }

        val tracksAdapter = PlaylistTracksAdapter(
            onTrackClick = { track ->
                findNavController().navigate(
                    R.id.action_playlistDetailFragment_to_playerFragment,
                    bundleOf(PlayerFragment.TRACK_EXTRA to track)
                )
            },
            onTrackLongClick = ::confirmTrackDeletion
        )
        view.findViewById<RecyclerView>(R.id.detailTracksRecycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
        }
        val tracksSheet = view.findViewById<View>(R.id.detailTracksSheet)
        BottomSheetBehavior.from(tracksSheet).apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val overlay = view.findViewById<View>(R.id.detailMenuOverlay)
        val sheet = view.findViewById<View>(R.id.detailMenuSheet)
        val behavior = BottomSheetBehavior.from(sheet)
        menuBehavior = behavior
        behavior.isHideable = true
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    return
                }
                overlay.isVisible = newState != BottomSheetBehavior.STATE_HIDDEN
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
        overlay.setOnClickListener { closeMenu() }
        view.findViewById<View>(R.id.detailMenuShare).setOnClickListener {
            closeMenu()
            sharePlaylist()
        }
        view.findViewById<View>(R.id.detailMenuEdit).setOnClickListener {
            closeMenu()
            findNavController().navigate(
                R.id.action_playlistDetailFragment_to_createPlaylistFragment,
                bundleOf(PLAYLIST_ID to playlistId)
            )
        }
        view.findViewById<View>(R.id.detailMenuDelete).setOnClickListener {
            closeMenu()
            confirmPlaylistDeletion()
        }

        viewModel.details.observe(viewLifecycleOwner) { details ->
            currentDetails = details
            if (details != null) {
                render(details)
                tracksAdapter.submitList(details.tracks)
                view.findViewById<View>(R.id.detailEmptyTracks).isVisible = details.tracks.isEmpty()
                view.findViewById<View>(R.id.detailTracksRecycler).isVisible = details.tracks.isNotEmpty()
            }
        }
        viewModel.deleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted) findNavController().popBackStack()
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error) {
                Toast.makeText(requireContext(), R.string.playlist_operation_failed, Toast.LENGTH_SHORT).show()
                viewModel.onErrorShown()
            }
        }
        viewModel.load(playlistId)
    }

    private fun render(details: PlaylistDetails) {
        val view = requireView()
        val playlist = details.playlist
        view.findViewById<TextView>(R.id.detailName).text = playlist.name
        view.findViewById<TextView>(R.id.detailDescription).apply {
            text = playlist.description
            isVisible = playlist.description.isNotBlank()
        }
        val minutes = details.tracks.sumOf { trackDurationSeconds(it) } / 60
        val trackCount = resources.getQuantityString(
            R.plurals.playlist_track_count, details.tracks.size, details.tracks.size
        )
        view.findViewById<TextView>(R.id.detailDuration).text =
            getString(R.string.playlist_minutes_only, minutes)
        view.findViewById<TextView>(R.id.detailTrackCount).text = trackCount
        view.findViewById<TextView>(R.id.detailMenuName).text = playlist.name
        view.findViewById<TextView>(R.id.detailMenuCount).text = trackCount
        showCover(view.findViewById(R.id.detailCover), playlist.coverPath)
        showCover(view.findViewById(R.id.detailMenuCover), playlist.coverPath)
    }

    private fun showCover(image: ImageView, path: String?) {
        Glide.with(image).clear(image)
        val cover = path?.let(::File)?.takeIf { it.isFile }
        if (cover != null) {
            image.setPadding(0, 0, 0, 0)
            image.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(image).load(cover).centerCrop().into(image)
        } else {
            val padding = if (image.id == R.id.detailCover) {
                (24 * resources.displayMetrics.density).toInt()
            } else 0
            image.setPadding(padding, padding, padding, padding)
            image.scaleType = ImageView.ScaleType.FIT_CENTER
            image.setImageResource(R.drawable.ic_playlist_detail_placeholder)
        }
    }

    private fun trackDurationSeconds(track: Track): Long {
        val parts = track.trackTime.split(":")
        if (parts.size != 2) return 0
        val minutes = parts[0].toLongOrNull() ?: return 0
        val seconds = parts[1].toLongOrNull() ?: return 0
        return minutes * 60 + seconds
    }

    private fun showMenu() {
        menuBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun closeMenu() {
        menuBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun confirmTrackDeletion(track: Track) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_playlist_track_delete)

        dialog.findViewById<View>(R.id.deleteTrackCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.deleteTrackConfirm).setOnClickListener {
            dialog.dismiss()
            viewModel.deleteTrack(track.trackId, playlistId)
        }

        dialog.show()
        val density = resources.displayMetrics.density
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_playlist_track_delete_dialog)
            setLayout((280 * density).toInt(), (123 * density).toInt())
            setGravity(Gravity.CENTER)
        }
    }

    private fun confirmPlaylistDeletion() {
        val details = currentDetails ?: return
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_playlist_delete)

        dialog.findViewById<TextView>(R.id.deletePlaylistMessage).text =
            getString(R.string.playlist_delete_named_message, details.playlist.name)
        dialog.findViewById<View>(R.id.deletePlaylistCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.deletePlaylistConfirm).setOnClickListener {
            dialog.dismiss()
            viewModel.deletePlaylist(playlistId)
        }

        dialog.show()
        val density = resources.displayMetrics.density
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_playlist_track_delete_dialog)
            setLayout((280 * density).toInt(), (143 * density).toInt())
            setGravity(Gravity.CENTER)
        }
    }

    private fun sharePlaylist() {
        val details = currentDetails ?: return
        if (details.tracks.isEmpty()) {
            Toast.makeText(requireContext(), R.string.playlist_share_empty, Toast.LENGTH_LONG).show()
            return
        }
        val count = details.tracks.size
        val countText = resources.getQuantityString(R.plurals.playlist_track_count, count, count)
        val message = buildString {
            appendLine(details.playlist.name)
            if (details.playlist.description.isNotBlank()) appendLine(details.playlist.description)
            appendLine(countText)
            details.tracks.forEachIndexed { index, track ->
                appendLine("${index + 1}. ${track.artistName} - ${track.trackName} (${track.trackTime})")
            }
        }.trimEnd()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.playlist_share)))
    }

    override fun onDestroyView() {
        menuBehavior = null
        currentDetails = null
        super.onDestroyView()
    }

    companion object {
        const val PLAYLIST_ID = "playlist_id"
    }
}

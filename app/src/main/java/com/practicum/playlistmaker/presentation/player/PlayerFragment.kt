package com.practicum.playlistmaker.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment() {

    private val viewModel by viewModel<PlayerViewModel>()
    private lateinit var playButton: ImageButton
    private lateinit var favoriteButton: ImageButton
    private lateinit var playerProgress: TextView

    private var renderedTrack: Track? = null
    private var sheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playButton = view.findViewById(R.id.playButton)
        favoriteButton = view.findViewById(R.id.addToFavoriteButton)
        favoriteButton.setOnClickListener { viewModel.onFavoriteClicked() }
        playerProgress = view.findViewById(R.id.playerProgress)
        setupPlaylistsSheet(view)

        view.findViewById<ImageButton>(R.id.playerBackButton).setOnClickListener {
            viewModel.onBackClicked()
            findNavController().popBackStack()
        }

        playButton.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            render(state)
        }

        viewModel.onScreenOpened(getTrackFromArguments())
    }

    private fun setupPlaylistsSheet(view: View) {
        val bottomSheet = view.findViewById<LinearLayout>(R.id.playlistsBottomSheet)
        val overlay = view.findViewById<View>(R.id.playlistSheetOverlay)
        val behavior = BottomSheetBehavior.from(bottomSheet)
        sheetBehavior = behavior
        behavior.isHideable = true
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    return
                }

                overlay.visibility = if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
        val sheetAdapter = PlaylistSheetAdapter(viewModel::onPlaylistSelected)
        view.findViewById<RecyclerView>(R.id.sheetPlaylistsRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sheetAdapter
        }
        viewModel.playlists.observe(viewLifecycleOwner) { sheetAdapter.submitList(it) }
        viewModel.playlistAddResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                Toast.makeText(
                    requireContext(),
                    getString(if (result.added) R.string.playlist_track_added else R.string.playlist_track_duplicate, result.playlistName),
                    Toast.LENGTH_SHORT
                ).show()
                if (result.added) behavior.state = BottomSheetBehavior.STATE_HIDDEN
                viewModel.onPlaylistResultHandled()
            }
        }
        view.findViewById<View>(R.id.addToPlaylistButton).setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        view.findViewById<View>(R.id.sheetNewPlaylistButton).setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_global_createPlaylistFragment)
        }
        overlay.setOnClickListener { behavior.state = BottomSheetBehavior.STATE_HIDDEN }
    }

    private fun render(state: PlayerScreenState) {
        state.track?.let { track ->
            if (renderedTrack != track) {
                renderedTrack = track
                renderTrack(track)
            }
        }

        playerProgress.text = state.progress
        favoriteButton.setImageResource(
            if (state.isFavorite) R.drawable.ic_player_favorite_selected_51
            else R.drawable.ic_player_favorite_51
        )

        if (state.isPlaying) {
            playButton.setImageResource(R.drawable.ic_player_pause_100)
            playButton.contentDescription = getString(R.string.player_pause)
        } else {
            playButton.setImageResource(R.drawable.ic_player_play_100)
            playButton.contentDescription = getString(R.string.player_play)
        }
    }

    private fun renderTrack(track: Track) {
        val view = requireView()
        view.findViewById<TextView>(R.id.playerTrackName).text = track.trackName
        view.findViewById<TextView>(R.id.playerArtistName).text = track.artistName
        view.findViewById<TextView>(R.id.playerDurationValue).text = track.trackTime
        view.findViewById<TextView>(R.id.playerGenreValue).text = track.primaryGenreName.orEmpty()
        view.findViewById<TextView>(R.id.playerCountryValue).text = track.country.orEmpty()

        setupOptionalField(
            container = view.findViewById(R.id.playerAlbumContainer),
            titleView = view.findViewById(R.id.playerAlbumTitle),
            valueView = view.findViewById(R.id.playerAlbumValue),
            value = track.collectionName
        )

        setupOptionalField(
            container = view.findViewById(R.id.playerYearContainer),
            titleView = view.findViewById(R.id.playerYearTitle),
            valueView = view.findViewById(R.id.playerYearValue),
            value = track.getReleaseYear()
        )

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_search_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(8)))
            .into(view.findViewById(R.id.playerCover))
    }

    private fun setupOptionalField(
        container: View,
        titleView: TextView,
        valueView: TextView,
        value: String?
    ) {
        if (value.isNullOrEmpty()) {
            container.visibility = View.GONE
        } else {
            container.visibility = View.VISIBLE
            titleView.visibility = View.VISIBLE
            valueView.visibility = View.VISIBLE
            valueView.text = value
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    @Suppress("DEPRECATION")
    private fun getTrackFromArguments(): Track {
        return requireArguments().getSerializable(TRACK_EXTRA) as Track
    }

    override fun onDestroyView() {
        sheetBehavior = null
        renderedTrack = null
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    companion object {
        const val TRACK_EXTRA = "track"
    }
}

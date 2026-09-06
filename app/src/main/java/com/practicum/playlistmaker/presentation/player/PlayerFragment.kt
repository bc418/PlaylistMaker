package com.practicum.playlistmaker.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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
    private lateinit var playerProgress: TextView

    private var renderedTrack: Track? = null

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
        playerProgress = view.findViewById(R.id.playerProgress)

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

    private fun render(state: PlayerScreenState) {
        state.track?.let { track ->
            if (renderedTrack != track) {
                renderedTrack = track
                renderTrack(track)
            }
        }

        playerProgress.text = state.progress

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

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    companion object {
        const val TRACK_EXTRA = "track"
    }
}

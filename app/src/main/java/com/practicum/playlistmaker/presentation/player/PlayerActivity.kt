package com.practicum.playlistmaker.presentation.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track

class PlayerActivity : AppCompatActivity() {

    private lateinit var viewModel: PlayerViewModel
    private lateinit var playButton: ImageButton
    private lateinit var playerProgress: TextView

    private var renderedTrack: Track? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        viewModel = ViewModelProvider(this)[PlayerViewModel::class.java]

        val root = findViewById<View>(R.id.root_activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            insets
        }

        playButton = findViewById(R.id.playButton)
        playerProgress = findViewById(R.id.playerProgress)

        findViewById<ImageButton>(R.id.playerBackButton).setOnClickListener {
            viewModel.onBackClicked()
            finish()
        }

        playButton.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        viewModel.screenState.observe(this) { state ->
            render(state)
        }

        viewModel.onScreenOpened(getTrackFromIntent())
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
        findViewById<TextView>(R.id.playerTrackName).text = track.trackName
        findViewById<TextView>(R.id.playerArtistName).text = track.artistName
        findViewById<TextView>(R.id.playerDurationValue).text = track.trackTime
        findViewById<TextView>(R.id.playerGenreValue).text = track.primaryGenreName.orEmpty()
        findViewById<TextView>(R.id.playerCountryValue).text = track.country.orEmpty()

        setupOptionalField(
            container = findViewById(R.id.playerAlbumContainer),
            titleView = findViewById(R.id.playerAlbumTitle),
            valueView = findViewById(R.id.playerAlbumValue),
            value = track.collectionName
        )

        setupOptionalField(
            container = findViewById(R.id.playerYearContainer),
            titleView = findViewById(R.id.playerYearTitle),
            valueView = findViewById(R.id.playerYearValue),
            value = track.getReleaseYear()
        )

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_search_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(8)))
            .into(findViewById(R.id.playerCover))
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
    private fun getTrackFromIntent(): Track {
        return intent.getSerializableExtra(TRACK_EXTRA) as Track
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    companion object {
        const val TRACK_EXTRA = "track"
    }
}


package com.practicum.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var playerState = STATE_DEFAULT

    private lateinit var playButton: ImageButton
    private lateinit var playerProgress: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer?.isPlaying == true) {
                playerProgress.text = SimpleDateFormat(
                    "mm:ss",
                    Locale.getDefault()
                ).format(mediaPlayer?.currentPosition ?: 0)
                handler.postDelayed(this, UPDATE_PROGRESS_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        val root = findViewById<View>(R.id.root_activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            insets
        }

        val track = getTrackFromIntent()

        playButton = findViewById(R.id.playButton)
        playerProgress = findViewById(R.id.playerProgress)

        findViewById<ImageButton>(R.id.playerBackButton).setOnClickListener {
            finish()
        }

        playButton.setOnClickListener {
            playbackControl()
        }

        findViewById<TextView>(R.id.playerTrackName).text = track.trackName
        findViewById<TextView>(R.id.playerArtistName).text = track.artistName
        playerProgress.text = getString(R.string.player_default_progress)
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

        preparePlayer(track.previewUrl.orEmpty())
    }

    private fun preparePlayer(previewUrl: String) {
        if (previewUrl.isBlank()) {
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)
            setOnPreparedListener {
                playerState = STATE_PREPARED
            }
            setOnCompletionListener {
                handler.removeCallbacks(updateProgressRunnable)
                it.seekTo(0)
                playerProgress.text = getString(R.string.player_default_progress)
                playButton.setImageResource(R.drawable.ic_player_play_100)
                playButton.contentDescription = getString(R.string.player_play)
                playerState = STATE_PREPARED
            }
            setOnErrorListener { _, _, _ ->
                playerState = STATE_DEFAULT
                true
            }
            prepareAsync()
        }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED,
            STATE_PAUSED -> startPlayer()
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
        playButton.setImageResource(R.drawable.ic_player_pause_100)
        playButton.contentDescription = getString(R.string.player_pause)
        playerState = STATE_PLAYING
        handler.post(updateProgressRunnable)
    }

    private fun pausePlayer() {
        mediaPlayer?.pause()
        playButton.setImageResource(R.drawable.ic_player_play_100)
        playButton.contentDescription = getString(R.string.player_play)
        playerState = STATE_PAUSED
        handler.removeCallbacks(updateProgressRunnable)
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
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateProgressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    companion object {
        const val TRACK_EXTRA = "track"

        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val UPDATE_PROGRESS_DELAY = 300L
    }
}

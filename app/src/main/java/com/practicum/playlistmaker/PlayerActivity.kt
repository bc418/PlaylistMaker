package com.practicum.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide

class PlayerActivity : AppCompatActivity() {

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

        findViewById<ImageButton>(R.id.playerBackButton).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.playerTrackName).text = track.trackName
        findViewById<TextView>(R.id.playerArtistName).text = track.artistName
        findViewById<TextView>(R.id.playerProgress).text = getString(R.string.player_default_progress)
        findViewById<TextView>(R.id.playerDurationValue).text = track.trackTime
        findViewById<TextView>(R.id.playerGenreValue).text = track.primaryGenreName.orEmpty()
        findViewById<TextView>(R.id.playerCountryValue).text = track.country.orEmpty()

        setupOptionalField(
            titleView = findViewById(R.id.playerAlbumTitle),
            valueView = findViewById(R.id.playerAlbumValue),
            value = track.collectionName
        )

        setupOptionalField(
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

    private fun setupOptionalField(titleView: TextView, valueView: TextView, value: String?) {
        if (value.isNullOrEmpty()) {
            titleView.visibility = View.GONE
            valueView.visibility = View.GONE
        } else {
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

    companion object {
        const val TRACK_EXTRA = "track"
    }
}

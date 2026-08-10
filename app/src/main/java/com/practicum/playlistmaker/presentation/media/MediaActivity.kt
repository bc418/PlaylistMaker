package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.practicum.playlistmaker.R

class MediaActivity : AppCompatActivity() {

    private val viewModel by viewModel<MediaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media)

        val root = findViewById<View>(R.id.root_activity_media)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            insets
        }

        viewModel.state.observe(this) { state ->
            render(state)
        }
    }

    private fun render(state: MediaState) {
    }
}

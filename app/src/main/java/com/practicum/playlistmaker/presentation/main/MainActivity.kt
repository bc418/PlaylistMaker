package com.practicum.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.android.material.button.MaterialButton
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.media.MediaActivity
import com.practicum.playlistmaker.presentation.search.SearchActivity
import com.practicum.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.root_activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            insets
        }

        findViewById<MaterialButton>(R.id.button_settings).setOnClickListener {
            viewModel.onSettingsClicked()
        }

        findViewById<MaterialButton>(R.id.button_search).setOnClickListener {
            viewModel.onSearchClicked()
        }

        findViewById<MaterialButton>(R.id.button_media).setOnClickListener {
            viewModel.onMediaClicked()
        }

        viewModel.state.observe(this) { state ->
            render(state)
        }
    }

    private fun render(state: MainState) {
        when (state.destination) {
            MainDestination.SEARCH -> {
                startActivity(Intent(this, SearchActivity::class.java))
                viewModel.onNavigationHandled()
            }
            MainDestination.MEDIA -> {
                startActivity(Intent(this, MediaActivity::class.java))
                viewModel.onNavigationHandled()
            }
            MainDestination.SETTINGS -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                viewModel.onNavigationHandled()
            }
            null -> Unit
        }
    }
}

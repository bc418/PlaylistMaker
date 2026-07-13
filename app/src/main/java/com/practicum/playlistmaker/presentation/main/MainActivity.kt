package com.practicum.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.media.MediaActivity
import com.practicum.playlistmaker.presentation.search.SearchActivity
import com.practicum.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
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

        val settingsButton = findViewById<MaterialButton>(R.id.button_settings)

        settingsButton.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }

        val searchButton = findViewById<MaterialButton>(R.id.button_search)

        searchButton.setOnClickListener {
            val displayIntent = Intent(this, SearchActivity::class.java)
            startActivity(displayIntent)
        }

        val mediaButton = findViewById<MaterialButton>(R.id.button_media)

        mediaButton.setOnClickListener {
            val displayIntent = Intent(this, MediaActivity::class.java)
            startActivity(displayIntent)
        }
    }
}

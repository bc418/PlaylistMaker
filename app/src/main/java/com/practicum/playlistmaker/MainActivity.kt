package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        val imageClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(this@MainActivity, "Здесь какой-то текст. Поиск", Toast.LENGTH_SHORT).show()
            }
        }


        val searchImage = findViewById<LinearLayout>(R.id.button_search)
        searchImage.setOnClickListener (imageClickListener)

        val mediaImage = findViewById<LinearLayout>(R.id.button_media)
        mediaImage.setOnClickListener {
            Toast.makeText(this@MainActivity, "Здесь какой-то текст. Медиа", Toast.LENGTH_SHORT).show()
        }

        val settingsImage = findViewById<LinearLayout>(R.id.button_settings)
        settingsImage.setOnClickListener {
            Toast.makeText(this@MainActivity, "Здесь какой-то текст. Настройки", Toast.LENGTH_SHORT).show()
        }
        */

        val settingsButton = findViewById<LinearLayout>(R.id.button_settings)

        settingsButton.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }

        val searchButton = findViewById<LinearLayout>(R.id.button_search)

        searchButton.setOnClickListener {
            val displayIntent = Intent(this, SearchActivity::class.java)
            startActivity(displayIntent)
        }

        val mediaButton = findViewById<LinearLayout>(R.id.button_media)

        mediaButton.setOnClickListener {
            val displayIntent = Intent(this, MediaActivity::class.java)
            startActivity(displayIntent)
        }


    }

}
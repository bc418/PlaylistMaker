package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaActivity : AppCompatActivity() {

    private val viewModel by viewModel<MediaViewModel>()

    private lateinit var viewPager: ViewPager2

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

        findViewById<View>(R.id.button_media_back).setOnClickListener {
            finish()
        }

        viewPager = findViewById(R.id.mediaViewPager)
        viewPager.adapter = MediaViewPagerAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.mediaTabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                FAVORITES_TAB_POSITION -> getString(R.string.favorite_tracks)
                PLAYLISTS_TAB_POSITION -> getString(R.string.playlists)
                else -> null
            }
        }.attach()

        savedInstanceState?.getInt(SELECTED_TAB_KEY)?.let { selectedTab ->
            viewPager.setCurrentItem(selectedTab, false)
        }

        viewModel.state.observe(this) { state ->
            render(state)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB_KEY, viewPager.currentItem)
    }

    private fun render(state: MediaState) {
    }

    private companion object {
        const val SELECTED_TAB_KEY = "selected_tab_key"
        const val FAVORITES_TAB_POSITION = 0
        const val PLAYLISTS_TAB_POSITION = 1
    }
}

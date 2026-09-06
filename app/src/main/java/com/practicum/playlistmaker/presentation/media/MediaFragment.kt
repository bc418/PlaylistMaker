package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFragment : Fragment() {

    private val viewModel by viewModel<MediaViewModel>()

    private var viewPager: ViewPager2? = null
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    private var selectedTabPosition = FAVORITES_TAB_POSITION

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedTabPosition = savedInstanceState?.getInt(SELECTED_TAB_KEY) ?: FAVORITES_TAB_POSITION

        val mediaViewPager = view.findViewById<ViewPager2>(R.id.mediaViewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.mediaTabLayout)

        viewPager = mediaViewPager

        mediaViewPager.adapter = MediaViewPagerAdapter(this)

        tabLayoutMediator = TabLayoutMediator(tabLayout, mediaViewPager) { tab, position ->
            tab.text = when (position) {
                FAVORITES_TAB_POSITION -> getString(R.string.favorite_tracks)
                PLAYLISTS_TAB_POSITION -> getString(R.string.playlists)
                else -> null
            }
        }.also {
            it.attach()
        }

        mediaViewPager.setCurrentItem(selectedTabPosition, false)

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectedTabPosition = position
            }
        }.also {
            mediaViewPager.registerOnPageChangeCallback(it)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB_KEY, selectedTabPosition)
    }

    override fun onDestroyView() {
        pageChangeCallback?.let { callback ->
            viewPager?.unregisterOnPageChangeCallback(callback)
        }

        tabLayoutMediator?.detach()

        pageChangeCallback = null
        tabLayoutMediator = null
        viewPager?.adapter = null
        viewPager = null

        super.onDestroyView()
    }

    private fun render(state: MediaState) {
    }

    companion object {
        fun newInstance() = MediaFragment()

        private const val SELECTED_TAB_KEY = "selected_tab_key"
        private const val FAVORITES_TAB_POSITION = 0
        private const val PLAYLISTS_TAB_POSITION = 1
    }
}
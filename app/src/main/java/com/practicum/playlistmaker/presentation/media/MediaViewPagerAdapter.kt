package com.practicum.playlistmaker.presentation.media

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.practicum.playlistmaker.presentation.media.favorites.FavoriteTracksFragment
import com.practicum.playlistmaker.presentation.media.playlists.PlaylistsFragment

class MediaViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            FAVORITES_TAB_POSITION -> FavoriteTracksFragment.newInstance()
            PLAYLISTS_TAB_POSITION -> PlaylistsFragment.newInstance()
            else -> throw IllegalArgumentException("Unknown media tab position: $position")
        }
    }

    private companion object {
        const val TAB_COUNT = 2
        const val FAVORITES_TAB_POSITION = 0
        const val PLAYLISTS_TAB_POSITION = 1
    }
}

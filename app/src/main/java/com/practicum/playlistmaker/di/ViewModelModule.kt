package com.practicum.playlistmaker.di

import android.media.MediaPlayer
import com.practicum.playlistmaker.presentation.playlistcreate.CreatePlaylistViewModel
import com.practicum.playlistmaker.presentation.main.MainViewModel
import com.practicum.playlistmaker.presentation.media.MediaViewModel
import com.practicum.playlistmaker.presentation.player.PlayerViewModel
import com.practicum.playlistmaker.presentation.media.favorites.FavoriteTracksViewModel
import com.practicum.playlistmaker.presentation.media.playlists.PlaylistsViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        MainViewModel()
    }

    viewModel {
        MediaViewModel()
    }


    viewModel {
        FavoriteTracksViewModel(favoriteTracksInteractor = get())
    }

    viewModel {
        PlaylistsViewModel(get())
    }

    viewModel {
        SearchViewModel(
            searchTracksInteractor = get(),
            searchHistoryInteractor = get()
        )
    }

    factory {
        MediaPlayer()
    }

    viewModel { CreatePlaylistViewModel(get()) }

    viewModel {
        PlayerViewModel(mediaPlayer = get(), favoriteTracksInteractor = get(), playlistsInteractor = get())
    }

    viewModel {
        SettingsViewModel(settingsInteractor = get())
    }
}

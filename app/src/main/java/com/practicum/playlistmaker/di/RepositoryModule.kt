package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.repository.PlaylistsRepositoryImpl
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import com.practicum.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.repository.FavoriteTracksRepositoryImpl
import com.practicum.playlistmaker.domain.repository.FavoriteTracksRepository
import com.practicum.playlistmaker.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import com.practicum.playlistmaker.domain.repository.TracksRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val SETTINGS_PREFERENCES = "playlist_maker_preferences"

val repositoryModule = module {

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(database = get(), gson = get(), context = get())
    }

    single<TracksRepository> {
        TracksRepositoryImpl(
            searchTrackApi = get(),
            trackMapper = get(),
            database = get()
        )
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            storage = get(),
            mapper = get(),
            database = get()
        )
    }

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(database = get(), mapper = get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            sharedPreferences = get(named(SETTINGS_PREFERENCES)),
            defaultDarkThemeEnabled = get()
        )
    }
}

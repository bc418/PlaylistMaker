package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import com.practicum.playlistmaker.domain.repository.TracksRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val SETTINGS_PREFERENCES = "playlist_maker_preferences"

val repositoryModule = module {

    single<TracksRepository> {
        TracksRepositoryImpl(
            searchTrackApi = get(),
            trackMapper = get()
        )
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            storage = get(),
            mapper = get()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            sharedPreferences = get(named(SETTINGS_PREFERENCES)),
            defaultDarkThemeEnabled = get()
        )
    }
}

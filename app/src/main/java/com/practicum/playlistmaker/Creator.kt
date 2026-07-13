package com.practicum.playlistmaker

import android.content.Context
import com.google.gson.Gson
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.RetrofitClient
import com.practicum.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.storage.SearchHistoryStorageImpl
import com.practicum.playlistmaker.domain.history.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.history.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.domain.search.SearchTracksInteractor
import com.practicum.playlistmaker.domain.search.SearchTracksInteractorImpl
import com.practicum.playlistmaker.domain.settings.SettingsInteractor
import com.practicum.playlistmaker.domain.settings.SettingsInteractorImpl

object Creator {

    fun provideSearchTracksInteractor(): SearchTracksInteractor {
        return SearchTracksInteractorImpl(
            TracksRepositoryImpl(
                RetrofitClient.searchTrackService,
                TrackMapper()
            )
        )
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        val sharedPreferences = context.applicationContext.getSharedPreferences(
            SEARCH_HISTORY_PREFERENCES,
            Context.MODE_PRIVATE
        )

        val storage = SearchHistoryStorageImpl(sharedPreferences, Gson())
        val repository = SearchHistoryRepositoryImpl(storage, TrackMapper())
        return SearchHistoryInteractorImpl(repository)
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(SettingsRepositoryImpl(context.applicationContext))
    }

    private const val SEARCH_HISTORY_PREFERENCES = "search_history_preferences"
}

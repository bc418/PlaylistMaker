package com.practicum.playlistmaker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.settings.SettingsViewModel

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
        val storage = SearchHistoryStorageImpl(context.applicationContext, Gson())
        val repository = SearchHistoryRepositoryImpl(storage, TrackMapper())
        return SearchHistoryInteractorImpl(repository)
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(SettingsRepositoryImpl(context.applicationContext))
    }

    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                    return SearchViewModel(
                        provideSearchTracksInteractor(),
                        provideSearchHistoryInteractor(context)
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    fun provideSettingsViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    return SettingsViewModel(provideSettingsInteractor(context)) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

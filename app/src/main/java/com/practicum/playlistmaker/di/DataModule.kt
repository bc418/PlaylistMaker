package com.practicum.playlistmaker.di

import android.content.Context
import android.content.res.Configuration
import com.google.gson.Gson
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.SearchTrackApi
import com.practicum.playlistmaker.data.storage.SearchHistoryStorage
import com.practicum.playlistmaker.data.storage.SearchHistoryStorageImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ITUNES_BASE_URL = "https://itunes.apple.com/"
private const val SEARCH_HISTORY_PREFERENCES = "search_history_preferences"
private const val SETTINGS_PREFERENCES = "playlist_maker_preferences"

val dataModule = module {

    single<SearchTrackApi> {
        Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SearchTrackApi::class.java)
    }

    single(named(SEARCH_HISTORY_PREFERENCES)) {
        androidContext().getSharedPreferences(SEARCH_HISTORY_PREFERENCES, Context.MODE_PRIVATE)
    }

    single(named(SETTINGS_PREFERENCES)) {
        androidContext().getSharedPreferences(SETTINGS_PREFERENCES, Context.MODE_PRIVATE)
    }

    single {
        isSystemDarkThemeEnabled(androidContext())
    }

    factory { Gson() }

    factory { TrackMapper() }

    single<SearchHistoryStorage> {
        SearchHistoryStorageImpl(
            sharedPreferences = get(named(SEARCH_HISTORY_PREFERENCES)),
            gson = get()
        )
    }
}

private fun isSystemDarkThemeEnabled(context: Context): Boolean {
    return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
}

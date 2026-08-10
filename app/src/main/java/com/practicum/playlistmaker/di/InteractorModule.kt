package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.domain.history.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.history.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.domain.search.SearchTracksInteractor
import com.practicum.playlistmaker.domain.search.SearchTracksInteractorImpl
import com.practicum.playlistmaker.domain.settings.SettingsInteractor
import com.practicum.playlistmaker.domain.settings.SettingsInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    single<SearchTracksInteractor> {
        SearchTracksInteractorImpl(get())
    }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }
}

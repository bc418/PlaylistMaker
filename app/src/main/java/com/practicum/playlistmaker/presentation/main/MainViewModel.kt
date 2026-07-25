package com.practicum.playlistmaker.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _state = MutableLiveData(MainState())
    val state: LiveData<MainState> = _state

    fun onSearchClicked() {
        _state.value = MainState(MainDestination.SEARCH)
    }

    fun onMediaClicked() {
        _state.value = MainState(MainDestination.MEDIA)
    }

    fun onSettingsClicked() {
        _state.value = MainState(MainDestination.SETTINGS)
    }

    fun onNavigationHandled() {
        _state.value = MainState()
    }
}

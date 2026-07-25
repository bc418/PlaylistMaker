package com.practicum.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.settings.SettingsInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _state = MutableLiveData(
        SettingsState(settingsInteractor.isDarkThemeEnabled())
    )
    val state: LiveData<SettingsState> = _state

    fun onThemeSwitchChanged(enabled: Boolean) {
        settingsInteractor.switchTheme(enabled)
        _state.value = SettingsState(enabled)
    }
}

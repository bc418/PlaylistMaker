package com.practicum.playlistmaker.domain.settings

import com.practicum.playlistmaker.domain.repository.SettingsRepository

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {

    override fun isDarkThemeEnabled(): Boolean {
        return repository.isDarkThemeEnabled()
    }

    override fun switchTheme(enabled: Boolean) {
        repository.setDarkTheme(enabled)
    }
}

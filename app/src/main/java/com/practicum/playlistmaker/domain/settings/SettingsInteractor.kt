package com.practicum.playlistmaker.domain.settings

interface SettingsInteractor {
    fun isDarkThemeEnabled(): Boolean
    fun switchTheme(enabled: Boolean)
}

package com.practicum.playlistmaker.domain.repository

interface SettingsRepository {
    fun isDarkThemeEnabled(): Boolean
    fun setDarkTheme(enabled: Boolean)
}

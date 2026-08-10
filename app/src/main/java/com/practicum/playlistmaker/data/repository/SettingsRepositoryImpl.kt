package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.practicum.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val defaultDarkThemeEnabled: Boolean
) : SettingsRepository {

    override fun isDarkThemeEnabled(): Boolean {
        return if (sharedPreferences.contains(DARK_THEME_KEY)) {
            sharedPreferences.getBoolean(DARK_THEME_KEY, false)
        } else {
            defaultDarkThemeEnabled
        }
    }

    override fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(DARK_THEME_KEY, enabled)
            .apply()
    }

    companion object {
        private const val DARK_THEME_KEY = "dark_theme"
    }
}

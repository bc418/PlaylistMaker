package com.practicum.playlistmaker.data.repository

import android.content.Context
import android.content.res.Configuration
import com.practicum.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private val sharedPreferences = context.getSharedPreferences(
        PLAYLIST_MAKER_PREFERENCES,
        Context.MODE_PRIVATE
    )

    override fun isDarkThemeEnabled(): Boolean {
        return if (sharedPreferences.contains(DARK_THEME_KEY)) {
            sharedPreferences.getBoolean(DARK_THEME_KEY, false)
        } else {
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES
        }
    }

    override fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(DARK_THEME_KEY, enabled)
            .apply()
    }

    companion object {
        private const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences"
        private const val DARK_THEME_KEY = "dark_theme"
    }
}

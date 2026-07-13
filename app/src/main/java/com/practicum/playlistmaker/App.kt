package com.practicum.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        val settingsInteractor = Creator.provideSettingsInteractor(this)
        darkTheme = settingsInteractor.isDarkThemeEnabled()

        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled

        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
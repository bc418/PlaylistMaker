package com.practicum.playlistmaker.presentation.player

import com.practicum.playlistmaker.domain.models.Track

data class PlayerScreenState(
    val track: Track? = null,
    val progress: String = DEFAULT_PROGRESS,
    val isPlaying: Boolean = false
) {
    companion object {
        const val DEFAULT_PROGRESS = "00:00"
    }
}

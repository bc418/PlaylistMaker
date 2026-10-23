package com.practicum.playlistmaker.presentation.media.favorites

import com.practicum.playlistmaker.domain.models.Track

data class FavoriteTracksState(
    val tracks: List<Track> = emptyList(),
    val openPlayerTrack: Track? = null
)

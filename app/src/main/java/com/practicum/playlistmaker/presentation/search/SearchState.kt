package com.practicum.playlistmaker.presentation.search

import com.practicum.playlistmaker.domain.models.Track

data class SearchState(
    val searchText: String = "",
    val tracks: List<Track> = emptyList(),
    val historyTracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val showResults: Boolean = false,
    val showHistory: Boolean = false,
    val showNothingFound: Boolean = false,
    val showConnectionError: Boolean = false,
    val errorMessage: String? = null,
    val openPlayerTrack: Track? = null
)

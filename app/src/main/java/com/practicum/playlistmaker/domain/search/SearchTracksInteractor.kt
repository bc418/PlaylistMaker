package com.practicum.playlistmaker.domain.search

import com.practicum.playlistmaker.domain.models.Track

interface SearchTracksInteractor {
    fun searchTracks(text: String, consumer: (List<Track>?, String?) -> Unit)
    fun cancelSearch()
}

package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Track

interface TracksRepository {
    fun searchTracks(text: String, consumer: (List<Track>?, String?) -> Unit)
    fun cancelSearch()
}

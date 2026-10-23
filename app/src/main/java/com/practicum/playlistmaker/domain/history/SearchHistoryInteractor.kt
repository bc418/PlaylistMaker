package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.models.Track

interface SearchHistoryInteractor {
    suspend fun getHistory(): ArrayList<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}

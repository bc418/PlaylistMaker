package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    suspend fun getHistory(): ArrayList<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}

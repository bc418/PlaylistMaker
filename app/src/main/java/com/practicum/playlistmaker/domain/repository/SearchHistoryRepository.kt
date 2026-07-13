package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    fun getHistory(): ArrayList<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}

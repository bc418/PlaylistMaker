package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.models.Track

interface SearchHistoryInteractor {
    fun getHistory(): ArrayList<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}

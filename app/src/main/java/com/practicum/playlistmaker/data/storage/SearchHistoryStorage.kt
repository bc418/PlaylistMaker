package com.practicum.playlistmaker.data.storage

import com.practicum.playlistmaker.data.dto.TrackStorageDto

interface SearchHistoryStorage {
    fun getHistory(): ArrayList<TrackStorageDto>
    fun saveHistory(history: ArrayList<TrackStorageDto>)
    fun clearHistory()
}

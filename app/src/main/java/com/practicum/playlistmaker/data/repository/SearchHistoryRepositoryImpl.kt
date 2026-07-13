package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.storage.SearchHistoryStorage
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val storage: SearchHistoryStorage,
    private val mapper: TrackMapper
) : SearchHistoryRepository {

    override fun getHistory(): ArrayList<Track> {
        return ArrayList(storage.getHistory().map { mapper.mapToTrack(it) })
    }

    override fun addTrack(track: Track) {
        val history = storage.getHistory()
        val trackDto = mapper.mapToStorageDto(track)

        history.removeAll { it == trackDto }
        history.add(0, trackDto)

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.lastIndex)
        }

        storage.saveHistory(history)
    }

    override fun clearHistory() {
        storage.clearHistory()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
}

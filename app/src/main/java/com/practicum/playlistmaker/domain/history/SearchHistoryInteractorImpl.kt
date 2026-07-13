package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun getHistory(): ArrayList<Track> {
        return repository.getHistory()
    }

    override fun addTrack(track: Track) {
        repository.addTrack(track)
    }

    override fun clearHistory() {
        repository.clearHistory()
    }
}

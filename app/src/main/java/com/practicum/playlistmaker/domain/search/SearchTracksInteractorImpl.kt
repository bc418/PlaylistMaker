package com.practicum.playlistmaker.domain.search

import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository

class SearchTracksInteractorImpl(
    private val repository: TracksRepository
) : SearchTracksInteractor {

    override fun searchTracks(text: String, consumer: (List<Track>?, String?) -> Unit) {
        repository.searchTracks(text, consumer)
    }

    override fun cancelSearch() {
        repository.cancelSearch()
    }
}

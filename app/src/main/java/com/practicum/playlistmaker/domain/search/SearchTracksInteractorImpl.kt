package com.practicum.playlistmaker.domain.search

import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository
import kotlinx.coroutines.flow.Flow

class SearchTracksInteractorImpl(
    private val repository: TracksRepository
) : SearchTracksInteractor {

    override fun searchTracks(text: String): Flow<Pair<List<Track>?, String?>> {
        return repository.searchTracks(text)
    }
}

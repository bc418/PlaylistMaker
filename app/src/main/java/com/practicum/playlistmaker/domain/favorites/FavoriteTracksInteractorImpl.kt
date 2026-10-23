package com.practicum.playlistmaker.domain.favorites

import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow

class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {
    override suspend fun addTrack(track: Track) = repository.addTrack(track)
    override suspend fun removeTrack(track: Track) = repository.removeTrack(track)
    override suspend fun isFavorite(trackId: Long) = repository.isFavorite(trackId)
    override fun getFavoriteTracks(): Flow<List<Track>> = repository.getFavoriteTracks()
}

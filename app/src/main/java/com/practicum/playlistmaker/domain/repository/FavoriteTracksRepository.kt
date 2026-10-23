package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addTrack(track: Track)
    suspend fun removeTrack(track: Track)
    suspend fun isFavorite(trackId: Long): Boolean
    fun getFavoriteTracks(): Flow<List<Track>>
}

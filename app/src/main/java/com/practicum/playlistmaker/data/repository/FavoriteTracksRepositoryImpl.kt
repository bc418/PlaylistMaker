package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.mapper.FavoriteTrackMapper
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val database: AppDatabase,
    private val mapper: FavoriteTrackMapper
) : FavoriteTracksRepository {

    private val dao get() = database.favoriteTracksDao()

    override suspend fun addTrack(track: Track) {
        dao.insert(mapper.toEntity(track))
    }

    override suspend fun removeTrack(track: Track) {
        dao.delete(mapper.toEntity(track))
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return trackId in dao.getFavoriteTrackIds()
    }

    override fun getFavoriteTracks(): Flow<List<Track>> =
        dao.observeFavorites().map { favorites -> favorites.map(mapper::toTrack) }
}

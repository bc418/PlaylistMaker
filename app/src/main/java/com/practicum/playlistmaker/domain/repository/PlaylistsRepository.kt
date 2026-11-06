package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {
    fun observePlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean
}

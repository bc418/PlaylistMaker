package com.practicum.playlistmaker.domain.playlists

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    fun observePlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean
}

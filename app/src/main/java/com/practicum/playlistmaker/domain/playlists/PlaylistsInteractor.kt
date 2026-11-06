package com.practicum.playlistmaker.domain.playlists

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.PlaylistDetails
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    fun observePlaylists(): Flow<List<Playlist>>
    fun observePlaylistDetails(playlistId: Long): Flow<PlaylistDetails?>
    suspend fun getPlaylist(playlistId: Long): Playlist?
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun updatePlaylist(playlistId: Long, name: String, description: String, newCoverUri: String?)
    suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean
    suspend fun removeTrackFromPlaylist(trackId: Long, playlistId: Long)
    suspend fun deletePlaylist(playlistId: Long)
}

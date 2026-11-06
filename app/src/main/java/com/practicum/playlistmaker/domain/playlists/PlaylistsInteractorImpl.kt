package com.practicum.playlistmaker.domain.playlists

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.PlaylistDetails
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.Flow

class PlaylistsInteractorImpl(private val repository: PlaylistsRepository) : PlaylistsInteractor {
    override fun observePlaylists(): Flow<List<Playlist>> = repository.observePlaylists()
    override fun observePlaylistDetails(playlistId: Long): Flow<PlaylistDetails?> =
        repository.observePlaylistDetails(playlistId)
    override suspend fun getPlaylist(playlistId: Long) = repository.getPlaylist(playlistId)
    override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long =
        repository.createPlaylist(name, description, coverUri)
    override suspend fun updatePlaylist(playlistId: Long, name: String, description: String, newCoverUri: String?) =
        repository.updatePlaylist(playlistId, name, description, newCoverUri)
    override suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean =
        repository.addTrackToPlaylist(track, playlistId)
    override suspend fun removeTrackFromPlaylist(trackId: Long, playlistId: Long) =
        repository.removeTrackFromPlaylist(trackId, playlistId)
    override suspend fun deletePlaylist(playlistId: Long) = repository.deletePlaylist(playlistId)
}

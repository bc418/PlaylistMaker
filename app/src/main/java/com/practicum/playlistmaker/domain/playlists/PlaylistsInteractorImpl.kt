package com.practicum.playlistmaker.domain.playlists

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.Flow

class PlaylistsInteractorImpl(private val repository: PlaylistsRepository) : PlaylistsInteractor {
    override fun observePlaylists(): Flow<List<Playlist>> = repository.observePlaylists()
    override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long =
        repository.createPlaylist(name, description, coverUri)
    override suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean =
        repository.addTrackToPlaylist(track, playlistId)
}

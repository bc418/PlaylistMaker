package com.practicum.playlistmaker.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.db.PlaylistEntity
import com.practicum.playlistmaker.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.PlaylistDetails
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistsRepositoryImpl(
    private val database: AppDatabase,
    private val gson: Gson,
    private val context: Context
) : PlaylistsRepository {

    private val dao = database.playlistsDao()
    private val idsType = object : TypeToken<List<Long>>() {}.type

    override fun observePlaylists(): Flow<List<Playlist>> =
        dao.observePlaylists().map { rows -> rows.map(::toPlaylist) }

    override fun observePlaylistDetails(playlistId: Long): Flow<PlaylistDetails?> =
        combine(dao.observePlaylist(playlistId), dao.observeAllTracks()) { entity, storedTracks ->
            if (entity == null) null else {
                val playlist = toPlaylist(entity)
                val tracksById = storedTracks.associateBy { it.trackId }
                // IDs are stored in insertion order; the latest added track goes first.
                val tracks = playlist.trackIds.asReversed().mapNotNull { tracksById[it]?.toTrack() }
                PlaylistDetails(playlist, tracks)
            }
        }

    override suspend fun getPlaylist(playlistId: Long): Playlist? = withContext(Dispatchers.IO) {
        dao.getPlaylist(playlistId)?.let(::toPlaylist)
    }

    override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long =
        withContext(Dispatchers.IO) {
            require(name.isNotBlank())
            val coverPath = coverUri?.let(::copyCoverToPrivateStorage)
            try {
                dao.insertPlaylist(
                    PlaylistEntity(
                        name = name.trim(), description = description, coverPath = coverPath,
                        trackIdsJson = gson.toJson(emptyList<Long>()), trackCount = 0
                    )
                )
            } catch (e: Exception) {
                coverPath?.let { File(it).delete() }
                throw e
            }
        }

    override suspend fun updatePlaylist(
        playlistId: Long, name: String, description: String, newCoverUri: String?
    ): Unit = withContext(Dispatchers.IO) {
        require(name.isNotBlank())
        // A null URI means the previous cover must be preserved.
        val newCoverPath = newCoverUri?.let(::copyCoverToPrivateStorage)
        var previousCover: String? = null
        try {
            database.withTransaction {
                val old = dao.getPlaylist(playlistId) ?: error("Playlist not found")
                previousCover = old.coverPath
                dao.updatePlaylist(
                    old.copy(name = name.trim(), description = description,
                        coverPath = newCoverPath ?: old.coverPath)
                )
            }
            if (newCoverPath != null) previousCover?.let { File(it).delete() }
            Unit
        } catch (e: Exception) {
            newCoverPath?.let { File(it).delete() }
            throw e
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean =
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val playlist = dao.getPlaylist(playlistId) ?: return@withTransaction false
                val ids = decodeIds(playlist.trackIdsJson)
                if (track.trackId in ids) return@withTransaction false
                dao.insertTrack(track.toPlaylistTrackEntity())
                val newIds = ids + track.trackId
                dao.updatePlaylist(
                    playlist.copy(trackIdsJson = gson.toJson(newIds), trackCount = newIds.size)
                )
                true
            }
        }

    override suspend fun removeTrackFromPlaylist(trackId: Long, playlistId: Long) =
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val playlist = dao.getPlaylist(playlistId) ?: return@withTransaction
                val ids = decodeIds(playlist.trackIdsJson)
                if (trackId !in ids) return@withTransaction
                val updatedIds = ids.filterNot { it == trackId }
                dao.updatePlaylist(
                    playlist.copy(trackIdsJson = gson.toJson(updatedIds), trackCount = updatedIds.size)
                )
                removeUnreferencedTracks(setOf(trackId))
            }
        }

    override suspend fun deletePlaylist(playlistId: Long): Unit = withContext(Dispatchers.IO) {
        var coverToRemove: String? = null
        database.withTransaction {
            val playlist = dao.getPlaylist(playlistId) ?: return@withTransaction
            coverToRemove = playlist.coverPath
            val idsToCheck = decodeIds(playlist.trackIdsJson).toSet()
            dao.deletePlaylist(playlist)
            removeUnreferencedTracks(idsToCheck)
        }
        coverToRemove?.let { File(it).delete() }
        Unit
    }

    // Called inside a Room transaction after changing the referencing playlists.
    private suspend fun removeUnreferencedTracks(candidates: Set<Long>) {
        if (candidates.isEmpty()) return
        val referenced = dao.getAllPlaylists().flatMap { decodeIds(it.trackIdsJson) }.toSet()
        for (trackId in candidates) {
            if (trackId !in referenced) dao.deleteTrack(trackId)
        }
    }

    private fun decodeIds(json: String): List<Long> = gson.fromJson<List<Long>>(json, idsType) ?: emptyList()

    private fun toPlaylist(entity: PlaylistEntity): Playlist = Playlist(
        entity.id, entity.name, entity.description, entity.coverPath,
        decodeIds(entity.trackIdsJson), entity.trackCount
    )

    private fun PlaylistTrackEntity.toTrack() = Track(
        trackName = trackName, artistName = artistName, trackTime = trackTime,
        artworkUrl100 = artworkUrl100, collectionName = collectionName,
        releaseDate = releaseDate, primaryGenreName = primaryGenreName,
        country = country, previewUrl = previewUrl, trackId = trackId
    )

    private fun Track.toPlaylistTrackEntity() = PlaylistTrackEntity(
        trackId = trackId, trackName = trackName, artistName = artistName,
        trackTime = trackTime, artworkUrl100 = artworkUrl100,
        collectionName = collectionName, releaseDate = releaseDate,
        primaryGenreName = primaryGenreName, country = country, previewUrl = previewUrl,
        addedAt = System.currentTimeMillis()
    )

    private fun copyCoverToPrivateStorage(sourceUri: String): String {
        val directory = File(context.filesDir, "playlist_covers").apply { mkdirs() }
        val target = File(directory, "${UUID.randomUUID()}.jpg")
        try {
            val source = context.contentResolver.openInputStream(Uri.parse(sourceUri))
                ?: error("Cannot open selected cover")
            source.use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        } catch (e: Exception) {
            target.delete()
            throw e
        }
        return target.absolutePath
    }
}

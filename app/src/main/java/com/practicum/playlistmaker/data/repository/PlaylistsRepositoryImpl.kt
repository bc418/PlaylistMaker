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
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    override suspend fun createPlaylist(
        name: String,
        description: String,
        coverUri: String?
    ): Long = withContext(Dispatchers.IO) {
        require(name.isNotBlank())
        val coverPath = coverUri?.let(::copyCoverToPrivateStorage)
        try {
            dao.insertPlaylist(
                PlaylistEntity(
                    name = name.trim(),
                    description = description,
                    coverPath = coverPath,
                    trackIdsJson = gson.toJson(emptyList<Long>()),
                    trackCount = 0
                )
            )
        } catch (e: Exception) {
            coverPath?.let { File(it).delete() }
            throw e
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlistId: Long): Boolean =
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val playlist = dao.getPlaylist(playlistId) ?: return@withTransaction false
                val ids: List<Long> = gson.fromJson(playlist.trackIdsJson, idsType) ?: emptyList()
                if (track.trackId in ids) return@withTransaction false

                dao.insertTrack(track.toPlaylistTrackEntity())
                val newIds = ids + track.trackId
                dao.updatePlaylist(
                    playlist.copy(trackIdsJson = gson.toJson(newIds), trackCount = newIds.size)
                )
                true
            }
        }

    private fun toPlaylist(entity: PlaylistEntity): Playlist {
        val ids: List<Long> = gson.fromJson(entity.trackIdsJson, idsType) ?: emptyList()
        return Playlist(entity.id, entity.name, entity.description, entity.coverPath, ids, entity.trackCount)
    }

    private fun Track.toPlaylistTrackEntity() = PlaylistTrackEntity(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl,
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

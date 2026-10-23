package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.storage.SearchHistoryStorage
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val storage: SearchHistoryStorage,
    private val mapper: TrackMapper,
    private val database: AppDatabase
) : SearchHistoryRepository {

    override suspend fun getHistory(): ArrayList<Track> {
        val favoriteIds = database.favoriteTracksDao().getFavoriteTrackIds().toSet()
        return ArrayList(storage.getHistory().map { dto ->
            mapper.mapToTrack(dto).also { track ->
                track.isFavorite = track.trackId in favoriteIds
            }
        })
    }

    override fun addTrack(track: Track) {
        val history = storage.getHistory()
        val trackDto = mapper.mapToStorageDto(track)

        history.removeAll {
            (trackDto.trackId != 0L && it.trackId == trackDto.trackId) ||
                (it.trackName == trackDto.trackName && it.artistName == trackDto.artistName)
        }
        history.add(0, trackDto)

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.lastIndex)
        }

        storage.saveHistory(history)
    }

    override fun clearHistory() {
        storage.clearHistory()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
}

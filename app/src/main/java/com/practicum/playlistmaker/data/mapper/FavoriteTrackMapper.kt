package com.practicum.playlistmaker.data.mapper

import com.practicum.playlistmaker.data.db.FavoriteTrackEntity
import com.practicum.playlistmaker.domain.models.Track

class FavoriteTrackMapper {

    fun toEntity(track: Track, addedAt: Long = System.currentTimeMillis()): FavoriteTrackEntity =
        FavoriteTrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTime = track.trackTime,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl,
            addedAt = addedAt
        )

    fun toTrack(entity: FavoriteTrackEntity): Track = Track(
        trackName = entity.trackName,
        artistName = entity.artistName,
        trackTime = entity.trackTime,
        artworkUrl100 = entity.artworkUrl100,
        collectionName = entity.collectionName,
        releaseDate = entity.releaseDate,
        primaryGenreName = entity.primaryGenreName,
        country = entity.country,
        previewUrl = entity.previewUrl,
        trackId = entity.trackId,
        isFavorite = true
    )
}

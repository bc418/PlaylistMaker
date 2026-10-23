package com.practicum.playlistmaker.data.mapper

import com.practicum.playlistmaker.data.dto.TrackDto
import com.practicum.playlistmaker.data.dto.TrackStorageDto
import com.practicum.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class TrackMapper {

    fun mapToTrack(dto: TrackDto): Track = Track(
        trackName = dto.trackName,
        artistName = dto.artistName,
        trackTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(dto.trackTimeMillis),
        artworkUrl100 = dto.artworkUrl100,
        collectionName = dto.collectionName,
        releaseDate = dto.releaseDate,
        primaryGenreName = dto.primaryGenreName,
        country = dto.country,
        previewUrl = dto.previewUrl,
        trackId = dto.trackId
    )

    fun mapToTrack(dto: TrackStorageDto): Track = Track(
        trackName = dto.trackName,
        artistName = dto.artistName,
        trackTime = dto.trackTime,
        artworkUrl100 = dto.artworkUrl100,
        collectionName = dto.collectionName,
        releaseDate = dto.releaseDate,
        primaryGenreName = dto.primaryGenreName,
        country = dto.country,
        previewUrl = dto.previewUrl,
        trackId = if (dto.trackId != 0L) dto.trackId else legacyTrackId(dto)
    )

    private fun legacyTrackId(dto: TrackStorageDto): Long {
        // Старые записи SharedPreferences не содержат trackId. Не назначаем им
        // общий 0 — разные старые треки должны оставаться различимыми в Room.
        val key = "${dto.trackName}|${dto.artistName}|${dto.trackTime}|${dto.artworkUrl100}"
        return -(key.hashCode().toLong() and 0xffffffffL) - 1L
    }

    fun mapToStorageDto(track: Track): TrackStorageDto = TrackStorageDto(
        trackName = track.trackName,
        artistName = track.artistName,
        trackTime = track.trackTime,
        artworkUrl100 = track.artworkUrl100,
        collectionName = track.collectionName,
        releaseDate = track.releaseDate,
        primaryGenreName = track.primaryGenreName,
        country = track.country,
        previewUrl = track.previewUrl,
        trackId = track.trackId
    )
}

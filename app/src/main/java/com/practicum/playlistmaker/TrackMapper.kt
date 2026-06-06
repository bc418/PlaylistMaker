package com.practicum.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

class TrackMapper {
    fun mapToTrack(dto: TrackDto) : Track = Track(
            trackName = dto.trackName,
            artistName = dto.artistName,
            trackTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(dto.trackTimeMillis),
            artworkUrl100 = dto.artworkUrl100
        )

}
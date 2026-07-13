package com.practicum.playlistmaker.data.dto

data class TrackStorageDto(
    val trackName: String,
    val artistName: String,
    val trackTime: String,
    val artworkUrl100: String,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null,
    val previewUrl: String? = null
)

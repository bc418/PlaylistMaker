package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.SearchTrackResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchTrackApi {

    @GET("/search?entity=song ")
    suspend fun search(@Query("term") text: String): SearchTrackResponse
}

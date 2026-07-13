package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.SearchTrackResponse
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.SearchTrackApi
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(
    private val searchTrackApi: SearchTrackApi,
    private val trackMapper: TrackMapper
) : TracksRepository {

    private var searchCall: Call<SearchTrackResponse>? = null

    override fun searchTracks(text: String, consumer: (List<Track>?, String?) -> Unit) {
        searchCall?.cancel()
        val call = searchTrackApi.search(text)
        searchCall = call

        call.enqueue(object : Callback<SearchTrackResponse> {
            override fun onResponse(
                call: Call<SearchTrackResponse>,
                response: Response<SearchTrackResponse>
            ) {
                if (call.isCanceled) {
                    return
                }

                if (response.code() == 200) {
                    val tracks = response.body()?.results
                        ?.map { trackMapper.mapToTrack(it) }
                        .orEmpty()
                    consumer(tracks, null)
                } else {
                    consumer(null, response.code().toString())
                }
            }

            override fun onFailure(call: Call<SearchTrackResponse>, t: Throwable) {
                if (call.isCanceled) {
                    return
                }

                consumer(null, t.message.toString())
            }
        })
    }

    override fun cancelSearch() {
        searchCall?.cancel()
        searchCall = null
    }
}

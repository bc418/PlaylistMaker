package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.SearchTrackApi
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException

class TracksRepositoryImpl(
    private val searchTrackApi: SearchTrackApi,
    private val trackMapper: TrackMapper
) : TracksRepository {

    override fun searchTracks(text: String): Flow<Pair<List<Track>?, String?>> = flow {
        try {
            val response = searchTrackApi.search(text)
            val tracks = response.results.map { trackMapper.mapToTrack(it) }
            emit(tracks to null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            emit(null to e.code().toString())
        } catch (e: IOException) {
            emit(null to e.message.orEmpty())
        } catch (e: Exception) {
            emit(null to e.message.orEmpty())
        }
    }.flowOn(Dispatchers.IO)
}

package com.practicum.playlistmaker.presentation.playlistdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.models.PlaylistDetails
import com.practicum.playlistmaker.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(private val interactor: PlaylistsInteractor) : ViewModel() {
    private val _details = MutableLiveData<PlaylistDetails?>()
    val details: LiveData<PlaylistDetails?> = _details
    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> = _deleted
    private val _error = MutableLiveData(false)
    val error: LiveData<Boolean> = _error
    private var observeJob: Job? = null

    fun load(playlistId: Long) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            interactor.observePlaylistDetails(playlistId).collect { _details.value = it }
        }
    }

    fun deleteTrack(trackId: Long, playlistId: Long) {
        viewModelScope.launch {
            runCatching { interactor.removeTrackFromPlaylist(trackId, playlistId) }
                .onFailure { _error.value = true }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            runCatching { interactor.deletePlaylist(playlistId) }
                .onSuccess { _deleted.value = true }
                .onFailure { _error.value = true }
        }
    }

    fun onErrorShown() { _error.value = false }
}

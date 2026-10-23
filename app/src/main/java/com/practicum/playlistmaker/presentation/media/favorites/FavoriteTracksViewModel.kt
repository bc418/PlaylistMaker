package com.practicum.playlistmaker.presentation.media.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.favorites.FavoriteTracksInteractor
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val _state = MutableLiveData(FavoriteTracksState())
    val state: LiveData<FavoriteTracksState> = _state

    private var clickAllowed = true
    private var clickJob: Job? = null

    init {
        viewModelScope.launch {
            favoriteTracksInteractor.getFavoriteTracks().collect { tracks ->
                _state.value = (_state.value ?: FavoriteTracksState()).copy(tracks = tracks)
            }
        }
    }

    fun onTrackClicked(track: Track) {
        if (!clickAllowed) return
        clickAllowed = false
        _state.value = (_state.value ?: FavoriteTracksState()).copy(openPlayerTrack = track)
        clickJob?.cancel()
        clickJob = viewModelScope.launch {
            delay(1_000L)
            clickAllowed = true
        }
    }

    fun onPlayerOpened() {
        _state.value = (_state.value ?: FavoriteTracksState()).copy(openPlayerTrack = null)
    }
}

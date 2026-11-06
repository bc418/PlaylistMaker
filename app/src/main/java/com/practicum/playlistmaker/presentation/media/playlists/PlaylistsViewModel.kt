package com.practicum.playlistmaker.presentation.media.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class PlaylistsViewModel(private val interactor: PlaylistsInteractor) : ViewModel() {
    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    init {
        viewModelScope.launch {
            interactor.observePlaylists().collect { _playlists.value = it }
        }
    }
}

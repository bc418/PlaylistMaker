package com.practicum.playlistmaker.presentation.playlistcreate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.launch

sealed class CreatePlaylistState {
    object Idle : CreatePlaylistState()
    object Saving : CreatePlaylistState()
    data class Created(val name: String) : CreatePlaylistState()
    object Error : CreatePlaylistState()
}

class CreatePlaylistViewModel(private val interactor: PlaylistsInteractor) : ViewModel() {
    private val _state = MutableLiveData<CreatePlaylistState>(CreatePlaylistState.Idle)
    val state: LiveData<CreatePlaylistState> = _state

    fun createPlaylist(name: String, description: String, coverUri: String?) {
        if (name.isBlank() || _state.value == CreatePlaylistState.Saving) return
        _state.value = CreatePlaylistState.Saving
        viewModelScope.launch {
            try {
                interactor.createPlaylist(name, description, coverUri)
                _state.value = CreatePlaylistState.Created(name.trim())
            } catch (_: Exception) {
                _state.value = CreatePlaylistState.Error
            }
        }
    }

    fun clearError() { _state.value = CreatePlaylistState.Idle }
}

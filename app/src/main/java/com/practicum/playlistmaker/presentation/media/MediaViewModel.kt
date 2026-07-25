package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MediaViewModel : ViewModel() {

    private val _state = MutableLiveData(MediaState())
    val state: LiveData<MediaState> = _state
}

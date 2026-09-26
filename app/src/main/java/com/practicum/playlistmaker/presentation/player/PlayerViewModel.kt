package com.practicum.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val mediaPlayer: MediaPlayer
) : ViewModel() {

    private var progressJob: Job? = null
    private var isPlayerReleased = false
    private var playerState = PlayerState.DEFAULT
    private var currentTrack: Track? = null

    private val _screenState = MutableLiveData(PlayerScreenState())
    val screenState: LiveData<PlayerScreenState> = _screenState

    fun onScreenOpened(track: Track) {
        if (currentTrack != null) {
            updateScreenState()
            return
        }

        currentTrack = track
        updateScreenState(
            progress = PlayerScreenState.DEFAULT_PROGRESS,
            isPlaying = false
        )
        preparePlayer(track.previewUrl.orEmpty())
    }

    fun onPlayButtonClicked() {
        when (playerState) {
            PlayerState.PLAYING -> pausePlayer()
            PlayerState.PREPARED,
            PlayerState.PAUSED -> startPlayer()
            PlayerState.DEFAULT -> Unit
        }
    }

    fun onPause() {
        if (playerState == PlayerState.PLAYING) {
            pausePlayer()
        }
    }

    fun onBackClicked() {
        releasePlayer()
    }

    private fun preparePlayer(previewUrl: String) {
        if (previewUrl.isBlank()) {
            return
        }

        runCatching {
            mediaPlayer.apply {
                isPlayerReleased = false
                setDataSource(previewUrl)
                setOnPreparedListener {
                    playerState = PlayerState.PREPARED
                }
                setOnCompletionListener {
                    stopProgressUpdate()
                    it.seekTo(0)
                    playerState = PlayerState.PREPARED
                    updateScreenState(
                        progress = PlayerScreenState.DEFAULT_PROGRESS,
                        isPlaying = false
                    )
                }
                setOnErrorListener { _, _, _ ->
                    stopProgressUpdate()
                    playerState = PlayerState.DEFAULT
                    updateScreenState(
                        progress = PlayerScreenState.DEFAULT_PROGRESS,
                        isPlaying = false
                    )
                    true
                }
                prepareAsync()
            }
        }.onFailure {
            playerState = PlayerState.DEFAULT
            updateScreenState(
                progress = PlayerScreenState.DEFAULT_PROGRESS,
                isPlaying = false
            )
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = PlayerState.PLAYING
        updateScreenState(isPlaying = true)
        startProgressUpdate()
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playerState = PlayerState.PAUSED
        updateScreenState(isPlaying = false)
        stopProgressUpdate()
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (playerState == PlayerState.PLAYING && mediaPlayer.isPlaying) {
                updateScreenState(
                    progress = formatProgress(mediaPlayer.currentPosition),
                    isPlaying = true
                )
                delay(UPDATE_PROGRESS_DELAY)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateScreenState(
        progress: String = _screenState.value?.progress ?: PlayerScreenState.DEFAULT_PROGRESS,
        isPlaying: Boolean = _screenState.value?.isPlaying ?: false
    ) {
        _screenState.value = PlayerScreenState(
            track = currentTrack,
            progress = progress,
            isPlaying = isPlaying
        )
    }

    private fun formatProgress(progress: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(progress)
    }

    private fun releasePlayer() {
        stopProgressUpdate()
        if (!isPlayerReleased) {
            mediaPlayer.release()
            isPlayerReleased = true
        }
        playerState = PlayerState.DEFAULT
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }

    companion object {
        private const val UPDATE_PROGRESS_DELAY = 300L
    }
}

package com.practicum.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.history.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.search.SearchTracksInteractor

class SearchViewModel(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())
    private var hasSearchFocus = false
    private var isClickAllowed = true

    private val _state = MutableLiveData(SearchState())
    val state: LiveData<SearchState> = _state

    private val searchRunnable = Runnable {
        searchTracks(_state.value?.searchText.orEmpty())
    }

    fun onScreenReady(hasFocus: Boolean) {
        hasSearchFocus = hasFocus
        showHistoryIfNeeded()
    }

    fun onSearchTextChanged(text: String) {
        val currentState = _state.value ?: SearchState()
        if (currentState.searchText == text) {
            return
        }

        handler.removeCallbacks(searchRunnable)
        searchTracksInteractor.cancelSearch()

        if (text.isBlank()) {
            _state.value = currentState.copy(
                searchText = text,
                tracks = emptyList(),
                isLoading = false,
                showResults = false,
                showHistory = false,
                showNothingFound = false,
                showConnectionError = false,
                errorMessage = null
            )
            showHistoryIfNeeded()
        } else {
            _state.value = currentState.copy(
                searchText = text,
                isLoading = false,
                showHistory = false,
                showNothingFound = false,
                showConnectionError = false,
                errorMessage = null,
                showResults = currentState.tracks.isNotEmpty()
            )
            searchDebounce()
        }
    }

    fun onSearchFocusChanged(hasFocus: Boolean) {
        hasSearchFocus = hasFocus
        if (hasFocus) {
            showHistoryIfNeeded()
        } else {
            hideHistory()
        }
    }

    fun onKeyboardDoneClicked(text: String) {
        handler.removeCallbacks(searchRunnable)
        searchTracks(text)
    }

    fun onClearClicked() {
        handler.removeCallbacks(searchRunnable)
        searchTracksInteractor.cancelSearch()

        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(
            searchText = "",
            tracks = emptyList(),
            isLoading = false,
            showResults = false,
            showHistory = false,
            showNothingFound = false,
            showConnectionError = false,
            errorMessage = null
        )
        showHistoryIfNeeded()
    }

    fun onClearHistoryClicked() {
        searchHistoryInteractor.clearHistory()
        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(
            historyTracks = emptyList(),
            showHistory = false
        )
    }

    fun onRetryClicked() {
        handler.removeCallbacks(searchRunnable)
        searchTracks(_state.value?.searchText.orEmpty())
    }

    fun onTrackClicked(track: Track) {
        if (!clickDebounce()) {
            return
        }

        searchHistoryInteractor.addTrack(track)
        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(openPlayerTrack = track)
    }

    fun onPlayerOpened() {
        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(openPlayerTrack = null)
    }

    fun onErrorMessageShown() {
        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(errorMessage = null)
    }

    fun onResume() {
        showHistoryIfNeeded()
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun searchTracks(text: String) {
        val query = text.trim()
        if (query.isEmpty()) {
            return
        }

        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(
            searchText = text,
            tracks = emptyList(),
            historyTracks = emptyList(),
            isLoading = true,
            showResults = false,
            showHistory = false,
            showNothingFound = false,
            showConnectionError = false,
            errorMessage = null
        )

        searchTracksInteractor.cancelSearch()
        searchTracksInteractor.searchTracks(query) { tracks, errorMessage ->
            if (tracks != null) {
                showSearchResults(tracks)
            } else {
                showConnectionError(errorMessage.orEmpty())
            }
        }
    }

    private fun showSearchResults(tracks: List<Track>) {
        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(
            tracks = tracks,
            isLoading = false,
            showResults = tracks.isNotEmpty(),
            showHistory = false,
            showNothingFound = tracks.isEmpty(),
            showConnectionError = false,
            errorMessage = null
        )
    }

    private fun showConnectionError(errorMessage: String) {
        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(
            tracks = emptyList(),
            isLoading = false,
            showResults = false,
            showHistory = false,
            showNothingFound = false,
            showConnectionError = true,
            errorMessage = errorMessage
        )
    }

    private fun showHistoryIfNeeded() {
        val currentState = _state.value ?: SearchState()
        if (!hasSearchFocus || currentState.searchText.isNotEmpty()) {
            hideHistory()
            return
        }

        val history = searchHistoryInteractor.getHistory()
        _state.value = currentState.copy(
            historyTracks = history,
            isLoading = false,
            showResults = false,
            showHistory = history.isNotEmpty(),
            showNothingFound = false,
            showConnectionError = false,
            errorMessage = null
        )
    }

    private fun hideHistory() {
        val currentState = _state.value ?: SearchState()
        _state.value = currentState.copy(
            showHistory = false,
            showResults = currentState.tracks.isNotEmpty() && !currentState.isLoading
        )
    }

    override fun onCleared() {
        handler.removeCallbacksAndMessages(null)
        searchTracksInteractor.cancelSearch()
        super.onCleared()
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2_000L
        private const val CLICK_DEBOUNCE_DELAY = 1_000L
    }
}

package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private var searchText: String = ""
    private lateinit var inputEditText: EditText

    private lateinit var placeholderContainer: View
    private lateinit var connectionErrorContainer: View
    private lateinit var updateButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var resultRecyclerView: RecyclerView
    private lateinit var historyContainer: View

    private val searchTrackService = RetrofitClient.searchTrackService
    private var searchCall: Call<SearchTrackResponse>? = null

    private val trackRepository = ArrayList<Track>()
    private val historyTrackRepository = ArrayList<Track>()

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        searchTracks(inputEditText.text.toString())
    }
    private var isClickAllowed = true

    private val searchHistory: SearchHistory by lazy {
        SearchHistory(
            getSharedPreferences(SEARCH_HISTORY_PREFERENCES, MODE_PRIVATE),
            Gson()
        )
    }

    private val tracksAdapter = TracksAdapter(trackRepository) { track ->
        if (clickDebounce()) {
            searchHistory.addTrack(track)
            openPlayer(track)
        }
    }

    private val historyTracksAdapter = TracksAdapter(historyTrackRepository) { track ->
        if (clickDebounce()) {
            searchHistory.addTrack(track)
            openPlayer(track)
        }
    }

    private val trackMapper = TrackMapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val root = findViewById<View>(R.id.root_activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            insets
        }

        placeholderContainer = findViewById(R.id.placeholderContainer)
        connectionErrorContainer = findViewById(R.id.connectionErrorContainer)
        updateButton = findViewById(R.id.updateButton)
        progressBar = findViewById(R.id.progressBar)
        historyContainer = findViewById(R.id.historyContainer)

        resultRecyclerView = findViewById(R.id.recyclerView)
        resultRecyclerView.layoutManager = LinearLayoutManager(this)
        resultRecyclerView.adapter = tracksAdapter

        val historyRecyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyTracksAdapter

        val searchBackButton = findViewById<ImageButton>(R.id.button_search_back)
        searchBackButton.setOnClickListener {
            finish()
        }

        val clearButton = findViewById<ImageView>(R.id.searchClearIcon)
        inputEditText = findViewById(R.id.searchInputEditText)

        clearButton.setOnClickListener {
            inputEditText.setText("")
            clearSearchResults()
            placeholderContainer.visibility = View.GONE
            connectionErrorContainer.visibility = View.GONE
            hideLoading()
            showHistoryIfNeeded()
        }

        inputEditText.doOnTextChanged { s, _, _, _ ->
            searchText = s?.toString() ?: ""
            clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

            handler.removeCallbacks(searchRunnable)
            searchCall?.cancel()
            hideLoading()
            placeholderContainer.visibility = View.GONE
            connectionErrorContainer.visibility = View.GONE

            if (s.isNullOrEmpty()) {
                clearSearchResults()
                showHistoryIfNeeded()
            } else {
                hideHistory()
                searchDebounce()
            }
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showHistoryIfNeeded()
            } else {
                hideHistory()
            }
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRunnable)
                searchTracks(inputEditText.text.toString())
                true
            } else {
                false
            }
        }

        findViewById<Button>(R.id.clearHistoryButton).setOnClickListener {
            searchHistory.clearHistory()
            historyTrackRepository.clear()
            historyTracksAdapter.notifyDataSetChanged()
            hideHistory()
        }

        updateButton.setOnClickListener {
            val query = inputEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                connectionErrorContainer.visibility = View.GONE
                handler.removeCallbacks(searchRunnable)
                searchTracks(query)
            }
        }

        inputEditText.post {
            showHistoryIfNeeded()
        }
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

    private fun openPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.TRACK_EXTRA, track)
        startActivity(intent)
    }

    private fun searchTracks(text: String) {
        val query = text.trim()
        if (query.isEmpty()) {
            return
        }

        hideHistory()
        clearSearchResults()
        showLoading()

        searchCall?.cancel()
        val call = searchTrackService.search(query)
        searchCall = call

        call.enqueue(object : Callback<SearchTrackResponse> {
            override fun onResponse(
                call: Call<SearchTrackResponse>,
                response: Response<SearchTrackResponse>
            ) {
                if (call.isCanceled) {
                    return
                }

                hideLoading()
                resultRecyclerView.visibility = View.VISIBLE

                if (response.code() == 200) {
                    if (response.body()?.results?.isNotEmpty() == true) {
                        trackRepository.addAll(response.body()?.results!!.map { trackMapper.mapToTrack(it) })
                        tracksAdapter.notifyDataSetChanged()
                    }

                    if (trackRepository.isEmpty()) {
                        showMessage(getString(R.string.nothing_found), "")
                    } else {
                        showMessage("", "")
                    }
                } else {
                    showMessage(getString(R.string.something_went_wrong), response.code().toString())
                }
            }

            override fun onFailure(call: Call<SearchTrackResponse>, t: Throwable) {
                if (call.isCanceled) {
                    return
                }

                hideLoading()
                showConnectionError(getString(R.string.something_went_wrong), t.message.toString())
            }
        })
    }

    private fun clearSearchResults() {
        trackRepository.clear()
        tracksAdapter.notifyDataSetChanged()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        resultRecyclerView.visibility = View.GONE
        historyContainer.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        connectionErrorContainer.visibility = View.GONE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showHistoryIfNeeded() {
        if (!inputEditText.hasFocus() || inputEditText.text.isNotEmpty()) {
            hideHistory()
            return
        }

        historyTrackRepository.clear()
        historyTrackRepository.addAll(searchHistory.getHistory())
        historyTracksAdapter.notifyDataSetChanged()

        if (historyTrackRepository.isNotEmpty()) {
            historyContainer.visibility = View.VISIBLE
            resultRecyclerView.visibility = View.GONE
            placeholderContainer.visibility = View.GONE
            connectionErrorContainer.visibility = View.GONE
        } else {
            hideHistory()
            resultRecyclerView.visibility = View.GONE
        }
    }

    private fun hideHistory() {
        historyContainer.visibility = View.GONE
        resultRecyclerView.visibility = View.VISIBLE
    }

    private fun showMessage(text: String, additionalMessage: String) {
        if (text.isNotEmpty()) {
            placeholderContainer.visibility = View.VISIBLE
            connectionErrorContainer.visibility = View.GONE
            resultRecyclerView.visibility = View.GONE
            clearSearchResults()

            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG).show()
            }
        } else {
            placeholderContainer.visibility = View.GONE
        }
    }

    private fun showConnectionError(text: String, additionalMessage: String) {
        if (text.isNotEmpty()) {
            connectionErrorContainer.visibility = View.VISIBLE
            placeholderContainer.visibility = View.GONE
            resultRecyclerView.visibility = View.GONE
            clearSearchResults()

            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG).show()
            }
        } else {
            connectionErrorContainer.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        showHistoryIfNeeded()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredText = savedInstanceState.getString(SEARCH_TEXT, "")
        inputEditText.setText(restoredText)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        searchCall?.cancel()
        super.onDestroy()
    }

    companion object {
        private const val SEARCH_TEXT = "SEARCH_TEXT"
        private const val SEARCH_HISTORY_PREFERENCES = "search_history_preferences"
        private const val SEARCH_DEBOUNCE_DELAY = 2_000L
        private const val CLICK_DEBOUNCE_DELAY = 1_000L
    }
}

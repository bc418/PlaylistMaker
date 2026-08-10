package com.practicum.playlistmaker.presentation.search

import android.content.Intent
import android.os.Bundle
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.player.PlayerActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var placeholderContainer: View
    private lateinit var connectionErrorContainer: View
    private lateinit var updateButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resultRecyclerView: RecyclerView
    private lateinit var historyContainer: View

    private val trackRepository = ArrayList<Track>()
    private val historyTrackRepository = ArrayList<Track>()

    private val viewModel by viewModel<SearchViewModel>()

    private val tracksAdapter = TracksAdapter(trackRepository) { track ->
        viewModel.onTrackClicked(track)
    }

    private val historyTracksAdapter = TracksAdapter(historyTrackRepository) { track ->
        viewModel.onTrackClicked(track)
    }

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

        clearButton = findViewById(R.id.searchClearIcon)
        inputEditText = findViewById(R.id.searchInputEditText)

        clearButton.setOnClickListener {
            viewModel.onClearClicked()
        }

        inputEditText.doOnTextChanged { s, _, _, _ ->
            viewModel.onSearchTextChanged(s?.toString().orEmpty())
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onSearchFocusChanged(hasFocus)
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onKeyboardDoneClicked(inputEditText.text.toString())
                true
            } else {
                false
            }
        }

        findViewById<Button>(R.id.clearHistoryButton).setOnClickListener {
            viewModel.onClearHistoryClicked()
        }

        updateButton.setOnClickListener {
            viewModel.onRetryClicked()
        }

        viewModel.state.observe(this) { state ->
            render(state)
        }

        inputEditText.post {
            viewModel.onScreenReady(inputEditText.hasFocus())
        }
    }

    private fun render(state: SearchState) {
        if (inputEditText.text.toString() != state.searchText) {
            inputEditText.setText(state.searchText)
            inputEditText.setSelection(state.searchText.length)
        }

        clearButton.visibility = if (state.searchText.isEmpty()) View.GONE else View.VISIBLE
        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        resultRecyclerView.visibility = if (state.showResults) View.VISIBLE else View.GONE
        historyContainer.visibility = if (state.showHistory) View.VISIBLE else View.GONE
        placeholderContainer.visibility = if (state.showNothingFound) View.VISIBLE else View.GONE
        connectionErrorContainer.visibility = if (state.showConnectionError) View.VISIBLE else View.GONE

        trackRepository.clear()
        trackRepository.addAll(state.tracks)
        tracksAdapter.notifyDataSetChanged()

        historyTrackRepository.clear()
        historyTrackRepository.addAll(state.historyTracks)
        historyTracksAdapter.notifyDataSetChanged()

        state.errorMessage?.takeIf { it.isNotEmpty() }?.let { message ->
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            viewModel.onErrorMessageShown()
        }

        state.openPlayerTrack?.let { track ->
            openPlayer(track)
            viewModel.onPlayerOpened()
        }
    }

    private fun openPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.TRACK_EXTRA, track)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}


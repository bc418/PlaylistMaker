package com.practicum.playlistmaker.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.player.PlayerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placeholderContainer = view.findViewById(R.id.placeholderContainer)
        connectionErrorContainer = view.findViewById(R.id.connectionErrorContainer)
        updateButton = view.findViewById(R.id.updateButton)
        progressBar = view.findViewById(R.id.progressBar)
        historyContainer = view.findViewById(R.id.historyContainer)

        resultRecyclerView = view.findViewById(R.id.recyclerView)
        resultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        resultRecyclerView.adapter = tracksAdapter

        val historyRecyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = historyTracksAdapter

        clearButton = view.findViewById(R.id.searchClearIcon)
        inputEditText = view.findViewById(R.id.searchInputEditText)

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

        view.findViewById<Button>(R.id.clearHistoryButton).setOnClickListener {
            viewModel.onClearHistoryClicked()
        }

        updateButton.setOnClickListener {
            viewModel.onRetryClicked()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
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
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            viewModel.onErrorMessageShown()
        }

        state.openPlayerTrack?.let { track ->
            openPlayer(track)
            viewModel.onPlayerOpened()
        }
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.action_searchFragment_to_playerFragment,
            bundleOf(PlayerFragment.TRACK_EXTRA to track)
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}

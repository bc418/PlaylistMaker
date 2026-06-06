package com.practicum.playlistmaker

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private var searchText: String = ""
    private lateinit var inputEditText: EditText

    private lateinit var placeholderContainer: View

    private val searchTrackService = RetrofitClient.searchTrackService

    private val trackRepository = ArrayList<Track>()
    private val tracksAdapter = TracksAdapter(trackRepository)

    private val trackMapper = TrackMapper()

    private lateinit var connectionErrorContainer: View
    private lateinit var updateButton: Button

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

        val searchBackButton = findViewById<ImageButton>(R.id.button_search_back)

        searchBackButton.setOnClickListener {
            finish()
        }


        val clearButton = findViewById<ImageView>(R.id.searchClearIcon)
        inputEditText = findViewById(R.id.searchInputEditText)

        clearButton.setOnClickListener {
            inputEditText.setText("")
            inputEditText.clearFocus()
            hideKeyboard(inputEditText)
            trackRepository.clear()
            tracksAdapter.notifyDataSetChanged()

            placeholderContainer.visibility = View.GONE
            connectionErrorContainer.visibility = View.GONE
        }

        inputEditText.doOnTextChanged { s, _, _, _ ->
            searchText = s?.toString() ?: ""
            clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

            placeholderContainer.visibility = View.GONE
            connectionErrorContainer.visibility = View.GONE

            if (s.isNullOrEmpty()) {
                trackRepository.clear()
                tracksAdapter.notifyDataSetChanged()
            }
        }

        placeholderContainer = findViewById(R.id.placeholderContainer)

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchTracks(inputEditText.text.toString())
            }
            false
        }


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        recyclerView.adapter = tracksAdapter


        connectionErrorContainer = findViewById(R.id.connectionErrorContainer)
        updateButton = findViewById(R.id.updateButton)

        updateButton.setOnClickListener {
            val query = inputEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                connectionErrorContainer.visibility = View.GONE
                searchTracks(query)
            }
        }
    }

    private fun searchTracks(text: String) {
        searchTrackService
            .search(text)
            .enqueue(object : Callback<SearchTrackResponse> {
                override fun onResponse(call: Call<SearchTrackResponse>,
                                        response: Response<SearchTrackResponse>
                ) {
                    if (response.code() == 200) {
                        trackRepository.clear()
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
                    showConnectionError(getString(R.string.something_went_wrong), t.message.toString())
                }

            })
    }

    private fun showMessage(text: String, additionalMessage: String) {

        if (text.isNotEmpty()) {
            placeholderContainer.visibility = View.VISIBLE
            connectionErrorContainer.visibility = View.GONE
            trackRepository.clear()
            tracksAdapter.notifyDataSetChanged()

            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            placeholderContainer.visibility = View.GONE
        }
    }

    private fun showConnectionError(text: String, additionalMessage: String) {

        if (text.isNotEmpty()) {
            connectionErrorContainer.visibility = View.VISIBLE
            placeholderContainer.visibility = View.GONE
            trackRepository.clear()
            tracksAdapter.notifyDataSetChanged()

            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            connectionErrorContainer.visibility = View.GONE
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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

    companion object {
        private const val SEARCH_TEXT = "SEARCH_TEXT"
    }

}




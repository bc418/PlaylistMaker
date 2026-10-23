package com.practicum.playlistmaker.presentation.media.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.player.PlayerFragment
import com.practicum.playlistmaker.presentation.search.TracksAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment() {

    private val viewModel by viewModel<FavoriteTracksViewModel>()
    private val tracks = ArrayList<Track>()
    private val tracksAdapter = TracksAdapter(tracks) { track ->
        viewModel.onTrackClicked(track)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_favorite_tracks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.favoriteTracksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = tracksAdapter

        val placeholderImage = view.findViewById<View>(R.id.favoriteTracksPlaceholderImage)
        val placeholderText = view.findViewById<View>(R.id.favoriteTracksPlaceholderText)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            tracks.clear()
            tracks.addAll(state.tracks)
            tracksAdapter.notifyDataSetChanged()

            val empty = tracks.isEmpty()
            placeholderImage.visibility = if (empty) View.VISIBLE else View.GONE
            placeholderText.visibility = if (empty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (empty) View.GONE else View.VISIBLE

            state.openPlayerTrack?.let { track ->
                viewModel.onPlayerOpened()
                findNavController().navigate(
                    R.id.action_mediaFragment_to_playerFragment,
                    bundleOf(PlayerFragment.TRACK_EXTRA to track)
                )
            }
        }
    }

    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.favoriteTracksRecyclerView)?.adapter = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}

package com.practicum.playlistmaker.presentation.media.playlists

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel by viewModel<PlaylistsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_playlists, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.newPlaylistButton).setOnClickListener {
            findNavController().navigate(R.id.action_global_createPlaylistFragment)
        }

        val adapter = PlaylistGridAdapter()
        val recycler = view.findViewById<RecyclerView>(R.id.playlistsRecyclerView)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter
        val gap = (8 * resources.displayMetrics.density).toInt()
        recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, item: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(item)
                if (position == RecyclerView.NO_POSITION) return
                if (position % 2 == 0) outRect.right = gap / 2
                else outRect.left = gap / 2
            }
        })

        val emptyImage = view.findViewById<View>(R.id.playlistsPlaceholderImage)
        val emptyText = view.findViewById<View>(R.id.playlistsPlaceholderText)
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            val empty = playlists.isEmpty()
            emptyImage.isVisible = empty
            emptyText.isVisible = empty
            recycler.isVisible = !empty
            adapter.submitList(playlists)
        }
    }

    companion object { fun newInstance() = PlaylistsFragment() }
}

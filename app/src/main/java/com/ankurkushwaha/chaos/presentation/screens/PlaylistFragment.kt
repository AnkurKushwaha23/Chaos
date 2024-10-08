package com.ankurkushwaha.chaos.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ankurkushwaha.chaos.data.model.Playlist
import com.ankurkushwaha.chaos.databinding.FragmentPlaylistBinding
import com.ankurkushwaha.chaos.presentation.adapter.OnPlaylistMenuClickListener
import com.ankurkushwaha.chaos.presentation.adapter.PlaylistAdapter
import com.ankurkushwaha.chaos.presentation.viewmodel.PlaylistViewModel
import com.ankurkushwaha.chaos.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : Fragment(), OnPlaylistMenuClickListener {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding!!

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePlaylist()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(requireContext(), this)
        binding.playlistSongRecyclerView.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observePlaylist() {
        playlistViewModel.allPlaylists.observe(viewLifecycleOwner, Observer { playlist ->
            playlistAdapter.submitList(playlist)
            if (playlist.isNotEmpty()) {
                binding.txtEmpty.visibility = View.GONE
            }
        })
    }

    override fun onPlaylistClick(playlist: Playlist) {
        val fragment = PlaylistsSongFragment.newInstance(playlist)
        fragment.show(childFragmentManager, "Song")
    }

    override fun onRemoveClick(playlist: Playlist) {
        playlistViewModel.deletePlaylist(playlist)
        showSnackBar(requireView(), "${playlist.name} playlist Deleted successfully!")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.ankurkushwaha.chaos.presentation.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.databinding.FragmentSongBinding
import com.ankurkushwaha.chaos.presentation.adapter.OnSongMenuClickListener
import com.ankurkushwaha.chaos.presentation.adapter.SongsAdapter
import com.ankurkushwaha.chaos.presentation.viewmodel.SongViewModel
import com.ankurkushwaha.chaos.services.MusicService
import com.ankurkushwaha.chaos.services.PLAY
import com.ankurkushwaha.chaos.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SongFragment : Fragment(), OnSongMenuClickListener {
    private var _binding: FragmentSongBinding? = null
    private val binding: FragmentSongBinding
        get() = _binding!!

    private val songViewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongsAdapter
    private lateinit var songList: List<Song>

    private var musicService: MusicService? = null
    private var musicBinder: IBinder? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val serviceBinder = binder as MusicService.MusicBinder
            musicService = serviceBinder.getService()
            musicBinder = binder
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeSongs()
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service
        val intent = Intent(activity, MusicService::class.java)
        activity?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            activity?.unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongsAdapter(requireContext(), this)
        binding.songRecyclerView.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeSongs() {
        lifecycleScope.launch {
            songViewModel.songs.observe(requireActivity(), Observer { songs ->
                songAdapter.submitList(songs)
                songList = songs
            })
        }
    }

    private fun openPlayerBottomSheet() {
        val bottomSheetFragment = PlayerFragment()
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    override fun onSongClick(song: Song) {
        try {
            val intent = Intent(activity, MusicService::class.java).apply {
                putExtra("currentSong", song)
                action = PLAY
            }
            activity?.startService(intent)
            (musicBinder as MusicService.MusicBinder).setMusicList(songList)
            openPlayerBottomSheet()
        } catch (e: Exception) {
            // Catch any unexpected exceptions and handle them
            e.printStackTrace() // Log the exception for debugging
            showToast(requireContext(), "Failed to play song: ${e.message}")
        }
    }

    override fun onPlayNext(song: Song) {
        musicService?.queueNextSong(song)
        showToast(requireContext(), "Song added to play next.")
    }

    override fun onAddToPlaylist(song: Song) {
        val addPlaylist = AddPlaylistFragment.newInstance(song)
        addPlaylist.show(childFragmentManager, "Playlist")
    }

    override fun onDetails(song: Song) {
        val detailFragment = SongDetailsBottomSheetFragment.newInstance(song)
        detailFragment.show(childFragmentManager, "Song")
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService = null
        _binding = null
    }
}
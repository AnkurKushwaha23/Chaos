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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ankurkushwaha.chaos.data.model.Playlist
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.databinding.FragmentPlaylistsSongBinding
import com.ankurkushwaha.chaos.presentation.adapter.OnSongMenuClickListener
import com.ankurkushwaha.chaos.presentation.adapter.PlaylistSongAdapter
import com.ankurkushwaha.chaos.presentation.viewmodel.PlaylistViewModel
import com.ankurkushwaha.chaos.services.MusicService
import com.ankurkushwaha.chaos.services.PLAY
import com.ankurkushwaha.chaos.utils.parcelable
import com.ankurkushwaha.chaos.utils.showSnackBar
import com.ankurkushwaha.chaos.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PlaylistsSongFragment : BottomSheetDialogFragment(), OnSongMenuClickListener {
    private var _binding: FragmentPlaylistsSongBinding? = null
    private val binding: FragmentPlaylistsSongBinding
        get() = _binding!!

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var songAdapter: PlaylistSongAdapter
    private lateinit var playlistSongList: List<Song>

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

    private var selectedPlaylist: Playlist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedPlaylist = it.parcelable(ARG_SONG) // Retrieve the passed data class
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsSongBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imgBack.setOnClickListener {
            dismiss()
        }
        binding.txtName.text = selectedPlaylist!!.name
        observeSongs()
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()

        val intent = Intent(activity, MusicService::class.java)
        activity?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        val dialog = dialog as? BottomSheetDialog
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        // Set the bottom sheet height to match the parent
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

        // Optionally, expand the bottom sheet to full height immediately
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            activity?.unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun setupRecyclerView() {
        songAdapter = PlaylistSongAdapter(requireContext(), this)
        binding.playlistSongRecyclerView.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeSongs() {
        playlistViewModel.fetchSongsForPlaylist(selectedPlaylist!!.playlistId)
        playlistViewModel.playlistWithSongs.observe(viewLifecycleOwner, Observer { playlistSong ->
            songAdapter.submitList(playlistSong.songs)
            playlistSongList = playlistSong.songs
        })
    }

    override fun onSongClick(song: Song) {
        try {
            val file = File(song.path)
            if (!file.exists()) {
                // If the file doesn't exist, show an error message or handle it appropriately
                showToast(requireContext(), "Song file not found!")
                return // Exit the function early to prevent further processing
            }

            val intent = Intent(activity, MusicService::class.java).apply {
                putExtra("currentSong", song)
                action = PLAY
            }

            activity?.startService(intent)
            (musicBinder as MusicService.MusicBinder).setMusicList(playlistSongList)
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
        playlistViewModel.removeSongFromPlaylist(
            playlistId = selectedPlaylist!!.playlistId,
            songId = song.id
        )
        showSnackBar(
            requireView(),
            "Song removed from ${selectedPlaylist!!.name} playlist successfully!"
        )
    }

    override fun onDetails(song: Song) {
        val detailFragment = SongDetailsBottomSheetFragment.newInstance(song)
        detailFragment.show(childFragmentManager, "Song")
    }

    companion object {
        private const val ARG_SONG = "Playlist"

        // newInstance method to create and pass the Song data class
        fun newInstance(playlist: Playlist): PlaylistsSongFragment {
            val fragment = PlaylistsSongFragment()
            val args = Bundle().apply {
                putParcelable(ARG_SONG, playlist) // Add the song object to the bundle
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService = null
        _binding = null
    }
}
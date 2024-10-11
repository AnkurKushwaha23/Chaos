package com.ankurkushwaha.chaos.presentation.screens

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.databinding.FragmentSearchBottomSheetBinding
import com.ankurkushwaha.chaos.presentation.adapter.OnSongMenuClickListener
import com.ankurkushwaha.chaos.presentation.adapter.SongsAdapter
import com.ankurkushwaha.chaos.presentation.viewmodel.SongViewModel
import com.ankurkushwaha.chaos.services.MusicService
import com.ankurkushwaha.chaos.services.PLAY
import com.ankurkushwaha.chaos.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchBottomSheetFragment : BottomSheetDialogFragment(), OnSongMenuClickListener {
    private var _binding: FragmentSearchBottomSheetBinding? = null
    private val binding: FragmentSearchBottomSheetBinding
        get() = _binding!!

    private val songViewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongsAdapter

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
        _binding = FragmentSearchBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        observeSongs()
        setupRecyclerView()
        setupSearchSong()

        binding.imgBackSearch.setOnClickListener {
            dismiss()
        }

        // Show keyboard and request focus for etSearchView
        binding.etSearchView.requestFocus()
        showKeyboard(binding.etSearchView)
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

        // Disable dragging of the BottomSheet
        behavior.isDraggable = false
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            activity?.unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun setupSearchSong() {
        binding.etSearchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before the text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the song list as the user types
                songViewModel.searchSongs(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after the text changes
            }
        })
    }

    private fun setupRecyclerView() {
        songAdapter = SongsAdapter(requireContext(), this)

        binding.searchSongRecyclerView.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeSongs() {
        songViewModel.filteredSongs.observe(viewLifecycleOwner, Observer { songs ->
            songAdapter.submitList(songs)
        })
    }

    private fun showKeyboard(editText: EditText) {
        val imm =
            editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onSongClick(song: Song) {
        try {
            val intent = Intent(activity, MusicService::class.java).apply {
                putExtra("currentSong", song)
                action = PLAY
            }
            activity?.startService(intent)
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
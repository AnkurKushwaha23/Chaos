package com.ankurkushwaha.chaos.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ankurkushwaha.chaos.R
import com.ankurkushwaha.chaos.data.model.Playlist
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.databinding.FragmentAddPlaylistBinding
import com.ankurkushwaha.chaos.presentation.adapter.OnPlaylistMenuClickListener
import com.ankurkushwaha.chaos.presentation.adapter.PlaylistAdapter
import com.ankurkushwaha.chaos.presentation.viewmodel.PlaylistViewModel
import com.ankurkushwaha.chaos.utils.parcelable
import com.ankurkushwaha.chaos.utils.showSnackBar
import com.ankurkushwaha.chaos.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPlaylistFragment : BottomSheetDialogFragment(), OnPlaylistMenuClickListener {
    private var _binding: FragmentAddPlaylistBinding? = null
    private val binding: FragmentAddPlaylistBinding
        get() = _binding!!

    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    private var selectedSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedSong = it.parcelable(ARG_SONG) // Retrieve the passed data class
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlaylistBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observePlaylist()
        setupRecyclerView()
        binding.createNewPlaylist.setOnClickListener {
            createNewPlaylistDialog()
        }
    }

    private fun createNewPlaylistDialog() {
        // Create an EditText for user input
        val editText = EditText(context)

        // Build the AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("New Playlist") // Set the title
            .setView(editText) // Set the EditText as the dialog's view
            .setPositiveButton("Ok") { dialogInterface, _ ->
                val userInput = editText.text.toString().trim()
                // Handle the input here
                if (userInput.isNotBlank()) {
                    playlistViewModel.createPlaylist(userInput)
                    showSnackBar(requireView(), "Playlist Created with name $userInput")
                } else {
                    showSnackBar(requireView(), "Please Provide a valid name")
                }
                dialogInterface.dismiss() // Dismiss the dialog
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss() // Dismiss the dialog
            }
            .create()

        dialog.show()

        // Set the tint for the buttons
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.colorAccent)
        )

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.colorAccent)
        )
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(requireContext(), this)
        binding.playlistRecyclerView.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observePlaylist() {
        playlistViewModel.allPlaylists.observe(viewLifecycleOwner, Observer { playlist ->
            playlistAdapter.submitList(playlist)
        })
    }

    override fun onPlaylistClick(playlist: Playlist) {
        if (selectedSong != null) {
            playlistViewModel.addSongToPlaylist(
                playlistId = playlist.playlistId,
                song = selectedSong!!
            )
            dismiss()
            showToast(requireContext(), "1 Song added to ${playlist.name}")
        }
    }

    override fun onRemoveClick(playlist: Playlist) {
        playlistViewModel.deletePlaylist(playlist)
        showSnackBar(requireView(), "${playlist.name} playlist Deleted successfully!")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val ARG_SONG = "song"

        // newInstance method to create and pass the Song data class
        fun newInstance(song: Song): AddPlaylistFragment {
            val fragment = AddPlaylistFragment()
            val args = Bundle().apply {
                putParcelable(ARG_SONG, song) // Add the song object to the bundle
            }
            fragment.arguments = args
            return fragment
        }
    }
}
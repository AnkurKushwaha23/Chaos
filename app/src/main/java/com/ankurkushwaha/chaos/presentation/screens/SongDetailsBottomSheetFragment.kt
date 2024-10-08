package com.ankurkushwaha.chaos.presentation.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.databinding.FragmentSongDetailsBottomSheetBinding
import com.ankurkushwaha.chaos.utils.formatDuration
import com.ankurkushwaha.chaos.utils.parcelable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongDetailsBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentSongDetailsBottomSheetBinding? = null
    private val binding: FragmentSongDetailsBottomSheetBinding
        get() = _binding!!

    private lateinit var songDetail: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the song data from arguments
        val song: Song? = arguments?.parcelable(ARG_SONG)
        song?.let {
            songDetail = song
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongDetailsBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            txtDetailTitle.text = "Title : \n${songDetail.title}"
            txtDetailArtist.text = "Artist : \n${songDetail.artist}"
            txtDetailAlbum.text = "Album : \n${songDetail.album}"
            txtDetailLocation.text = "Location : \n${songDetail.path}"

            val duration = songDetail.duration.toLong().formatDuration()
            txtDetailDuration.text = "Duration \n${duration} Minutes"
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val ARG_SONG = "arg_song_details"

        // Function to create a new instance of PlayerFragment and pass Song data
        fun newInstance(song: Song): SongDetailsBottomSheetFragment {
            val fragment = SongDetailsBottomSheetFragment()
            val bundle = Bundle().apply {
                putParcelable(ARG_SONG, song)  // Assuming Song implements Parcelable
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
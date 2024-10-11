package com.ankurkushwaha.chaos.presentation.screens

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ankurkushwaha.chaos.R
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.databinding.FragmentPlayerBinding
import com.ankurkushwaha.chaos.presentation.viewmodel.SongViewModel
import com.ankurkushwaha.chaos.services.MusicService
import com.ankurkushwaha.chaos.utils.formatDuration
import com.ankurkushwaha.chaos.utils.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment : BottomSheetDialogFragment() {
    private val currentMusic = MutableStateFlow<Song?>(null)

    private var _binding: FragmentPlayerBinding? = null
    private val binding: FragmentPlayerBinding
        get() = _binding!!

    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var serviceConnection: ServiceConnection

    private val songViewModel: SongViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Apply the full-height bottom sheet style
        return BottomSheetDialog(
            requireContext(),
            com.ankurkushwaha.chaos.R.style.FullHeightBottomSheetDialogTheme
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        setupListeners()
        setupSeekBar()
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) { // Only bind if not already bound
            initializeServiceConnection()
            val intent = Intent(activity, MusicService::class.java)
            activity?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

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

    private fun setupUI(nowPlayingSong: Song) {
        nowPlayingSong.let { song ->
            binding.tvSongTitlePA.text = song.title
            binding.tvSongTitlePA.isSelected = true
            binding.tvSongArtistPA.text = song.artist
            binding.tvSeekBarStart.text = "00:00"
            binding.tvSeekBarEnd.text = song.duration.formatDuration()

            Glide.with(this)
                .load(song.imageUri)
                .placeholder(com.ankurkushwaha.chaos.R.drawable.musicbg)
                .into(binding.imgCurrentSongPA)

            // Load the current song image into a Bitmap
            Glide.with(this)
                .asBitmap()
                .load(song.imageUri ?: com.ankurkushwaha.chaos.R.drawable.musicbg)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        // Apply the blur effect
                        Blurry.with(requireContext())
                            .radius(5) // Set the blur radius
                            .sampling(3) // Set the sampling factor
                            .from(resource)
                            .into(binding.backgroundPA)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle the placeholder if needed
                    }
                })
        }
    }

    private fun setupListeners() {
        binding.imgBack.setOnClickListener {
            dismiss()
        }

        binding.fabPlayPause.setOnClickListener {
            if (isBound) {
                musicService?.playPause()
            }
        }

        binding.imgNext.setOnClickListener {
            if (isBound) {
                musicService?.nextSong()
            }
        }

        binding.imgPrevious.setOnClickListener {
            if (isBound) {
                musicService?.previousSong()
            }
        }

        binding.imgShuffle.setOnClickListener {
            musicService?.toggleShuffle()
        }

        binding.imgRepeat.setOnClickListener {
            musicService?.toggleRepeat()
        }
    }

    private fun setupSeekBar() {
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isBound) {
                    musicService?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeServiceConnection() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                musicService = (binder as MusicService.MusicBinder).getService()

                // Observe current song and update the UI
                musicService?.getCurrentSong()?.onEach { song ->
                    if (song != null) {
                        setupUI(song)
                        currentMusic.update { song }
                        // Call setUpFavBtn only after currentMusic is updated
                        setUpFavBtn()
                    }
                }?.launchIn(lifecycleScope)

                // Observe the playing status and update the play/pause button
                musicService?.isPlaying()?.onEach { isPlaying ->
                    updatePlayPauseIcon(isPlaying)
                }?.launchIn(lifecycleScope)

                musicService?.isShuffle()?.onEach { isShuffle ->
                    updateShuffle(isShuffle)
                }?.launchIn(lifecycleScope)

                musicService?.isRepeat()?.onEach { isRepeatOn ->
                    updateOnRepeat(isRepeatOn)
                }?.launchIn(lifecycleScope)

                // Observe the duration of the current song
                musicService?.currentDuration()?.onEach { duration ->
                    binding.seekBarPA.progress = duration
                    binding.tvSeekBarStart.text = duration.toLong().formatDuration()
                }?.launchIn(lifecycleScope)

                musicService?.maxDuration()?.onEach { max ->
                    binding.seekBarPA.max = max
                }?.launchIn(lifecycleScope)

                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                musicService = null
                isBound = false
            }
        }
    }

    private fun setUpFavBtn() {
        // Observe the favorite songs list
        viewLifecycleOwner.lifecycleScope.launch {
            songViewModel.favSongsList.collect { favSongsList ->
                val currentSong = currentMusic.value
                currentSong?.let { song ->
                    val isFavorite = favSongsList.any { favSong -> favSong.id == song.id }

                    // Set the favorite icon based on current state
                    binding.imgFavPA.setImageResource(
                        if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                    )

                    // Handle the click to toggle favorite state
                    binding.imgFavPA.setOnClickListener {
                        if (isFavorite) {
                            songViewModel.removeFavorite(song)
                            showToast(requireContext(), "Removed from favorites")
                        } else {
                            songViewModel.addFavorite(song)
                            showToast(requireContext(), "Added to favorites")
                        }
                    }

                }
            }
        }
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        if (isPlaying) {
            binding.fabPlayPause.setImageResource(com.ankurkushwaha.chaos.R.drawable.ic_pause)
        } else {
            binding.fabPlayPause.setImageResource(com.ankurkushwaha.chaos.R.drawable.ic_play)
        }
    }

    private fun updateShuffle(isShuffleOn: Boolean) {
        if (isShuffleOn) {
            binding.imgShuffle.setImageResource(com.ankurkushwaha.chaos.R.drawable.ic_shuffle_on)
        } else {
            binding.imgShuffle.setImageResource(com.ankurkushwaha.chaos.R.drawable.ic_shuffle)
        }
    }

    private fun updateOnRepeat(isRepeatOn: Boolean) {
        if (isRepeatOn) {
            binding.imgRepeat.setImageResource(com.ankurkushwaha.chaos.R.drawable.ic_repeat_one)
        } else {
            binding.imgRepeat.setImageResource(com.ankurkushwaha.chaos.R.drawable.ic_repeat)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unbind from the service if still bound
        if (isBound) {
            activity?.unbindService(serviceConnection)
            isBound = false
        }
        // Reset the music service reference
        musicService = null
        // Clear any listeners or callbacks
        binding.seekBarPA.setOnSeekBarChangeListener(null)
        // Clear the binding reference
        _binding = null
    }

}
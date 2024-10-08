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
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.databinding.FragmentMiniPlayerBinding
import com.ankurkushwaha.chaos.services.MusicService
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MiniPlayerFragment : Fragment() {
    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var serviceConnection: ServiceConnection

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding: FragmentMiniPlayerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMiniPlayerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupSeekBar()
    }

    private fun showMiniPlayer(isVisible: Boolean) {
        (activity as? MainActivity)?.miniPlayerVisible(isVisible)
    }

    private fun setupListeners() {
        binding.imgPlayPauseMP.setOnClickListener {
            if (isBound) {
                musicService?.playPause()
            }
        }

        binding.imgNextMP.setOnClickListener {
            if (isBound) {
                musicService?.nextSong()
            }
        }

        binding.miniPlayerLayout.setOnClickListener {
            val bottomSheetFragment = PlayerFragment()
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun setupUI(nowPlayingSong: Song) {
        nowPlayingSong.let { song ->
            binding.tvSongTitleMP.text = song.title
            binding.tvSongTitleMP.isSelected = true

            Glide.with(this)
                .load(song.imageUri)
                .placeholder(com.ankurkushwaha.chaos.R.drawable.musicbg)
                .into(binding.imgCurrentSongMP)
        }
    }

    override fun onStart() {
        super.onStart()
        super.onStart()
        if (!isBound) { // Only bind if not already bound
            initializeServiceConnection()
            val intent = Intent(activity, MusicService::class.java)
            activity?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            activity?.unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun setupSeekBar() {
        binding.seekBarMP.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                    }
                }?.launchIn(lifecycleScope)

                // Observe the playing status and update the play/pause button
                musicService?.isPlaying()?.onEach { isPlaying ->
                    updatePlayPauseIcon(isPlaying)
                }?.launchIn(lifecycleScope)

                musicService?.showMiniPlayer()?.onEach { isVisible ->
                    showMiniPlayer(isVisible)
                }?.launchIn(lifecycleScope)


                // Observe the duration of the current song
                musicService?.currentDuration()?.onEach { duration ->
                    binding.seekBarMP.progress = duration
                }?.launchIn(lifecycleScope)

                musicService?.maxDuration()?.onEach { max ->
                    binding.seekBarMP.max = max
                }?.launchIn(lifecycleScope)

                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                musicService = null
                isBound = false
            }
        }
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        if (isPlaying) {
            binding.imgPlayPauseMP.setImageResource(com.ankurkushwaha.chaos.R.drawable.ic_pause) // Show pause icon
        } else {
            binding.imgPlayPauseMP.setImageResource(com.ankurkushwaha.chaos.R.drawable.ic_play) // Show play icon
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.seekBarMP.setOnSeekBarChangeListener(null)
        if (isBound) {
            activity?.unbindService(serviceConnection)
            isBound = false
        }
        // Reset the music service reference
        musicService = null
        // Clear any listeners or callbacks
        _binding = null
    }
}

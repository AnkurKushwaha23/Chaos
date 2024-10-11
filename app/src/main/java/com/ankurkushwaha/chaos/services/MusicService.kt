package com.ankurkushwaha.chaos.services

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ankurkushwaha.chaos.R
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.presentation.screens.MainActivity
import com.ankurkushwaha.chaos.receivers.HeadphoneReceiver
import com.ankurkushwaha.chaos.receivers.TimerStopMusicReceiver
import com.ankurkushwaha.chaos.utils.parcelable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val PREV = "PREV"
const val NEXT = "NEXT"
const val PLAY_PAUSE = "PLAY_PAUSE"
const val PLAY = "PLAY"
const val CANCEL = "CANCEL"
const val CHANNEL_ID = "ChaosServiceChannel"
const val CHANNEL_NAME = "Chaos Music Player"

class MusicService : Service() {
    private var mediaPlayer = MediaPlayer()
    private lateinit var mediaSession: MediaSessionCompat
    private val currentMusic = MutableStateFlow<Song?>(null)
    private var nextUpSong: Song? = null
    private var musicList = mutableListOf<Song>()
    private val originalMusicList = mutableListOf<Song>()
    private val isPlaying = MutableStateFlow<Boolean>(false)
    private val isShuffleOn = MutableStateFlow<Boolean>(false)
    private val isRepeatSongOn = MutableStateFlow<Boolean>(false)
    private val isShowMiniPlayer = MutableStateFlow<Boolean>(false)
    private val maxDuration = MutableStateFlow(0)
    private val currentDuration = MutableStateFlow(0)
    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private lateinit var headphoneReceiver: HeadphoneReceiver
    private lateinit var timerStopMusicReceiver: TimerStopMusicReceiver

    private val binder = MusicBinder()


    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService

        fun setMusicList(songs: List<Song>) {
            this@MusicService.musicList = songs.toMutableList()
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initializeMediaSession()
        initializeReceivers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Fetch the song from the intent
        val song: Song? = intent?.extras?.parcelable("currentSong")
        song?.let {
            currentMusic.value = it
        }

        val action = intent?.action
        when (action) {
            PREV -> previousSong()
            PLAY_PAUSE -> playPause()
            NEXT -> nextSong()
            CANCEL -> {
                job?.cancel()
                pauseMusic()
                removeForeground()
                // Stop the service if the media player is not playing
                if (!mediaPlayer.isPlaying) {
                    stopSelf()
                }
            }

            PLAY -> {
                currentMusic.value?.let {
                    isShowMiniPlayer.update { true }
                    play(it)
                }
            }
        }

        return START_STICKY
    }

    /** Initialize BroadCastReceivers*/
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun initializeReceivers() {
        headphoneReceiver = HeadphoneReceiver()
        timerStopMusicReceiver = TimerStopMusicReceiver()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        registerReceiver(headphoneReceiver, filter)

        val timerStopFilter = IntentFilter("com.ankurkushwaha.chaos.STOP_MUSIC_SERVICE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerStopMusicReceiver, timerStopFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(timerStopMusicReceiver, timerStopFilter)
        }
    }

    /** Media Player useful functions **/
    /**Play the song */
    private fun play(song: Song) {
        if (requestAudioFocus()) {
            // Update the current music state to the song being played
            currentMusic.value = song

            // Check if the media player is already playing
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset() // Reset to clear the previous state
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(song.path)
                prepareAsync() // Prepare the MediaPlayer asynchronously
                setOnPreparedListener {
                    mediaPlayer.start() // Start playback when prepared
                    sendNotification(song)
                    updateDuration()
                }
                setOnCompletionListener {
                    if (isRepeatSongOn.value) {
                        // Repeat the current song indefinitely
                        play(song)
                    } else if (nextUpSong != null) {
                        // Play the queued song if any
                        val queuedSong = nextUpSong
                        nextUpSong = null // Clear the queue after playing
                        currentMusic.value = queuedSong
                        play(queuedSong!!)
                    } else if (musicList.isNotEmpty()) {
                        // Play the next song in the list
                        nextSong()
                    } else {
                        // No queued song and no more songs in the list, loop the current song
                        job?.cancel()
                        currentMusic.value?.let { song ->
                            play(song) // Play the current song again when it finishes
                        }
                    }
                }
            }
        }
    }

    /**It play and pause Song  */
    internal fun playPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause() // Pause if currently playing
        } else {
            mediaPlayer.start() // Start playback if paused
        }
        sendNotification(currentMusic.value!!)
    }

    /**Pause the music */
    fun pauseMusic() {
        mediaPlayer.pause()
        isPlaying.update { false }
    }

    /**Return song total duration */
    private fun getDuration(): Int {
        return mediaPlayer.duration ?: 0
    }

    /**Next Song */
    internal fun nextSong() {
        job?.cancel()

        if (musicList.isNotEmpty()) {
            val currentIndex = musicList.indexOf(currentMusic.value)
            val nextIndex = (currentIndex + 1) % musicList.size

            currentMusic.update { musicList[nextIndex] }
            play(currentMusic.value!!) // Play the next song
        }
    }

    /**user add song to play next song in queue*/
    internal fun queueNextSong(song: Song) {
        nextUpSong = song
    }

    /**Previous Song */
    internal fun previousSong() {
        job?.cancel()
        if (musicList.isNotEmpty()) {
            val currentIndex = musicList.indexOf(currentMusic.value)
            val prevIndex = if (currentIndex > 0) currentIndex - 1 else musicList.size - 1
            currentMusic.update { musicList[prevIndex] }
            play(currentMusic.value!!) // Call play with the previous song
        }
    }

    /**This function update the seekbar progress duration */
    private fun updateDuration() {
        job = scope.launch {
            if (!mediaPlayer.isPlaying) return@launch
            maxDuration.update { mediaPlayer.duration.toInt() }

            while (mediaPlayer.isPlaying) {
                try {
                    currentDuration.update { mediaPlayer.currentPosition.toInt() }
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                } catch (e: IllegalStateException) {
                    Log.e("MusicService", "MediaPlayer is not in a valid state", e)
                    break // Exit the loop if the media player is in an invalid state
                }
                delay(1000) // Update duration every second
            }
        }
    }

    /**This function use to seek a particular time interval in seekbar  */
    fun seekTo(position: Int) {
        mediaPlayer.let {
            if (position in 0..it.duration) {
                it.seekTo(position)
                currentDuration.update { position }
                updatePlaybackState(if (it.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED)
            }
        }
    }

    /** Function to toggle shuffle mode*/
    internal fun toggleShuffle() {
        isShuffleOn.update { !it } // Toggle the shuffle mode

        if (isShuffleOn.value) {
            shuffleMusicList() // Shuffle the music list
        } else {
            resetMusicList()
        }
    }

    /** Function to shuffle the music list*/
    private fun shuffleMusicList() {
        job?.cancel()

        if (originalMusicList.isEmpty()) {
            originalMusicList.addAll(musicList) // Save the original list if not already saved
        }

        if (musicList.isNotEmpty()) {
            musicList.shuffle() // Shuffle the song list
            updateDuration()
//            currentMusic.update { musicList[0] } // Set the first song from the shuffled list
//            play(currentMusic.value!!) // Play the shuffled song
        }
    }

    /**this function reset the music list when shuffle turned off */
    private fun resetMusicList() {
        job?.cancel()

        if (originalMusicList.isNotEmpty()) {
            musicList.clear()
            musicList.addAll(originalMusicList) // Restore the original list
            updateDuration()
//            currentMusic.update { musicList[0] } // Optionally set the first song from the original list
//            play(currentMusic.value!!) // Play the first song from the original list
        }
    }

    /** Function to toggle repeat mode*/
    internal fun toggleRepeat() {
        isRepeatSongOn.update { !it } // Toggle the repeat mode

        if (isRepeatSongOn.value) {
            repeatCurrentSong() // Enable repeat mode
        } else {
            disableRepeat() // Disable repeat mode
        }
    }

    /** Function to repeat the current song*/
    private fun repeatCurrentSong() {
        job?.cancel()
        currentMusic.value?.let { song ->
            mediaPlayer.setOnCompletionListener {
                play(song) // Play the current song again when it finishes
            }
        }
    }

    /**This function disable the repeat single song  */
    private fun disableRepeat() {
        mediaPlayer.setOnCompletionListener {
            nextSong()
        }
    }

    /** Functions for show MediaPlayer in other fragment or activity for UI purpose*/
    /**update the ui based on current song playing */
    fun getCurrentSong(): StateFlow<Song?> {
        return currentMusic
    }

    /**it return true if song is playing else false to update ui */
    fun isPlaying(): StateFlow<Boolean> {
        return isPlaying
    }

    /**this function used to show mini player */
    fun showMiniPlayer(): StateFlow<Boolean> {
        return isShowMiniPlayer
    }

    /**this function use to update ui based on either shuffle is on or off */
    fun isShuffle(): StateFlow<Boolean> {
        return isShuffleOn
    }

    /**this function use to update ui based on either repeat is on or off */
    fun isRepeat(): StateFlow<Boolean> {
        return isRepeatSongOn
    }

    /**this function return current duration to update ui and seekbar */
    fun currentDuration(): StateFlow<Int> {
        return currentDuration
    }

    /**this function return song total duration */
    fun maxDuration(): StateFlow<Int> {
        return maxDuration
    }

    /**Initialize the MediaSession */
    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(baseContext, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    playPause()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }

                override fun onPause() {
                    playPause()
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }

                override fun onSkipToNext() {
                    nextSong()
                    updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
                }

                override fun onSkipToPrevious() {
                    previousSong()
                    updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
                }

                override fun onSeekTo(pos: Long) {
                    seekTo(pos.toInt())
                    updatePlaybackState(if (mediaPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED)
                }

                override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                    val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mediaButtonEvent.getParcelableExtra(
                            Intent.EXTRA_KEY_EVENT,
                            KeyEvent::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                    }

                    if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                        when (keyEvent.keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                playPause()
                                return true
                            }

                            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                nextSong()
                                return true
                            }

                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                previousSong()
                                return true
                            }
                        }
                    }
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }
            })

            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)


            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_SEEK_TO or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1f)
                    .build()
            )

            isActive = true
        }
    }

    /**this function update the MediaSessionCompat playback state */
    private fun updatePlaybackState(state: Int) {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SEEK_TO or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .setState(state, mediaPlayer.currentPosition.toLong(), 1f)
                .build()
        )
    }

    /**Initialize the Notification */
    private fun sendNotification(song: Song) {
        val isMediaPlayerValid = try {
            mediaPlayer.isPlaying
        } catch (e: IllegalStateException) {
            false // MediaPlayer is not in a valid state, handle it gracefully.
        }

        isPlaying.update { isMediaPlayerValid }

        val playPauseAction = NotificationCompat.Action(
            if (mediaPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            "Play-Pause",
            createPendingIntent("PLAY_PAUSE")
        )

        val nextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next, "Next", createPendingIntent(NEXT)
        )

        val prevAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous, "Prev", createPendingIntent(PREV)
        )

        val cancelAction = NotificationCompat.Action(
            R.drawable.ic_close, "Cancel", createPendingIntent(CANCEL)
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            action = "OPEN_APP"
            putExtra("OPEN_APP", "Chaos")
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playbackDuration = getDuration()

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, playbackDuration.toLong())
                .build()
        )

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SEEK_TO or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .setState(
                    if (mediaPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED,
                    mediaPlayer.currentPosition.toLong(),
                    1f
                )
                .build()
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setContentTitle(song.title) // Song title
            .setContentText(song.artist) // Song artist
            .setSmallIcon(R.drawable.ic_music_note) // Small icon
            .setContentIntent(pendingIntent) // Intent to open app when notification is clicked
            .addAction(prevAction) // Previous song action
            .addAction(playPauseAction) // Play/Pause action
            .addAction(nextAction) // Next song action
            .addAction(cancelAction) // Cancel action
            .setProgress(playbackDuration, currentDuration.value, false) // Show the progress
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Priority for the notification
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Ensure visibility on lock screen
            .setOngoing(true) // Keep the notification ongoing (common for media players)
            .setOnlyAlertOnce(true) // Avoid alerting multiple times for the same notification

        Glide.with(this)
            .asBitmap()
            .placeholder(R.drawable.musicbg)
            .load(song.imageUri)
            .into(object : CustomTarget<Bitmap>() {
                @SuppressLint("MissingPermission")
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    notification.setLargeIcon(resource)
                    NotificationManagerCompat.from(this@MusicService)
                        .notify(1, notification.build())
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startForeground(1, notification.build())
            }
        } else {
            startForeground(1, notification.build())
        }
    }

    /**This function create pending intent for different actions*/
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**This function remove foreground service */
    private fun removeForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(STOP_FOREGROUND_REMOVE) // Use new API from Android 13
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true) // Use old API for versions below Android 13
        }
    }

    /**This variable Method to handle audio focus changes */
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Pause your music when another app gains audio focus
                pauseMusic()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause music temporarily when there's a transient audio loss (e.g., phone call)
                pauseMusic()
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume playback
                playPause()
            }
        }
    }

    /**This function request audio focus */
    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /**this function properly release the audio focus when chaos no longer needs it */
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    /**clean the resources by stop service and unregister receivers */
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicService", "MusicService Destroyed")
        // Release the MediaPlayer resources
        mediaPlayer.let {
            if (it.isPlaying) {
                it.stop() // Stop the media player if it's playing
            }
            it.release() // Release resources held by MediaPlayer
            mediaSession.release()
        }
        // Remove the service from foreground
        try{
            removeForeground()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        // Unregister the broadcast receivers
        try {
            unregisterReceiver(headphoneReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace() // Receiver wasn't registered, log it if needed
        }

        try {
            unregisterReceiver(timerStopMusicReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace() // Receiver wasn't registered, log it if needed
        }
        // Abandon audio focus when the service is destroyed
        try {
            abandonAudioFocus()
            audioFocusRequest = null
        } catch (e: IllegalArgumentException) {
            e.printStackTrace() // Receiver wasn't registered, log it if needed
        }
    }
}
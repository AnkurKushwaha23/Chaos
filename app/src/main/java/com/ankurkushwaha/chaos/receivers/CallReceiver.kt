package com.ankurkushwaha.chaos.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.telephony.TelephonyManager
import com.ankurkushwaha.chaos.services.MusicService

class CallReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // Pause the music if there's an incoming call
                (context as? MusicService)?.pauseMusic()
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call is answered, pause the music
                (context as? MusicService)?.pauseMusic()
                //    // Call is answered, check if it's on speaker
                val isOnSpeaker = audioManager?.isSpeakerphoneOn ?: false

                // If the call is on speaker, you can choose to do something, like pause again or continue
                if (isOnSpeaker) {
                    (context as? MusicService)?.pauseMusic() // Optional: Pause if on speaker
                }
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                // The call has ended, resume music if it was playing before
                (context as? MusicService)?.playPause()
            }
        }
    }
}
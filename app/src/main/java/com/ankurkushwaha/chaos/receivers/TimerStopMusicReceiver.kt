package com.ankurkushwaha.chaos.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ankurkushwaha.chaos.services.MusicService

class TimerStopMusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.ankurkushwaha.chaos.STOP_MUSIC_SERVICE") {
            (context as? MusicService)?.playPause()
        }
    }
}
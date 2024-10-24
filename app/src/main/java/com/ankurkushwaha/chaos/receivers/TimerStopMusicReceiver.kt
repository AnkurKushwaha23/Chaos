package com.ankurkushwaha.chaos.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.ankurkushwaha.chaos.services.MusicService

class TimerStopMusicReceiver : BroadcastReceiver() {
    private var musicService: MusicService? = null
    private var serviceBound = false
    private var currentContext: Context? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.MusicBinder
            musicService = binder?.getService()
            serviceBound = true

            // Now that we're connected, pause the music if service exists
            try {
                musicService?.pauseMusic()
            } catch (e: Exception) {
                // Log the error but don't crash
                e.printStackTrace()
            }

            // Clean up
            currentContext?.let { ctx ->
                try {
                    ctx.unbindService(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            serviceBound = false
            currentContext = null
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            serviceBound = false
            currentContext = null
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.ankurkushwaha.chaos.STOP_MUSIC_SERVICE") {
//            (context as? MusicService)?.playPause()
            try {
                // Store context for unbinding later
                currentContext = context

                // Bind to the MusicService
                val serviceIntent = Intent(context, MusicService::class.java)
                context?.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
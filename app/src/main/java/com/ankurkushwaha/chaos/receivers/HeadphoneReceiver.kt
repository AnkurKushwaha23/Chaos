package com.ankurkushwaha.chaos.receivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ankurkushwaha.chaos.services.MusicService

class HeadphoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
            val state = intent.getIntExtra("state", -1)
            when (state) {
                0 -> {
                    // Headphones are unplugged, pause the music
                    (context as? MusicService)?.pauseMusic() // Pauses the music
                }
            }
        } else if (intent?.action == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
            // Bluetooth device disconnected, pause the music
            (context as? MusicService)?.pauseMusic()
        }
    }
}

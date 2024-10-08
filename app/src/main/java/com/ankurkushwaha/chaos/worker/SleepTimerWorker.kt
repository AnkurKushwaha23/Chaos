package com.ankurkushwaha.chaos.worker

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters

class SleepTimerWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Create an intent to broadcast
        val intent = Intent("com.ankurkushwaha.chaos.STOP_MUSIC_SERVICE")
        applicationContext.sendBroadcast(intent)

        // Return success
        return Result.success()
    }
}
package com.example.aifriendship.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ProactiveScheduler {

    private const val WORK_NAME = "proactive_companion_message_work"
    private const val RAPID_WORK_PREFIX = "proactive_follow_up_"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<ProactiveMessageWorker>(
            repeatInterval = 3,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 30,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }

    fun scheduleFollowUp(context: Context, companionId: String, delayMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = OneTimeWorkRequestBuilder<ProactiveMessageWorker>()
            .setInitialDelay(delayMinutes.coerceAtLeast(1), TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "$RAPID_WORK_PREFIX$companionId",
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    fun cancelFollowUp(context: Context, companionId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("$RAPID_WORK_PREFIX$companionId")
    }
}

package com.example.aifriendship.data.worker

import android.content.Context
import com.example.aifriendship.domain.service.CompanionScheduler

class WorkManagerCompanionScheduler(private val context: Context) : CompanionScheduler {

    override fun scheduleFollowUp(companionId: String, delayMinutes: Long) {
        ProactiveScheduler.scheduleFollowUp(context, companionId, delayMinutes)
    }

    override fun cancelFollowUp(companionId: String) {
        ProactiveScheduler.cancelFollowUp(context, companionId)
    }
}


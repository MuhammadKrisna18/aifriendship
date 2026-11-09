package com.example.aifriendship.domain.service

interface CompanionScheduler {
    fun scheduleFollowUp(companionId: String, delayMinutes: Long)
    fun cancelFollowUp(companionId: String)
}

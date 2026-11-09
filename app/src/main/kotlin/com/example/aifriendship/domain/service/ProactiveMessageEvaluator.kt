package com.example.aifriendship.domain.service

import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.ProactiveTriggerReason
import java.util.Calendar

object ProactiveMessageEvaluator {

    fun evaluate(
        companion: AiCompanion,
        messages: List<ChatMessage>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): ProactiveTriggerReason? {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour in 0..6) {
            return null
        }

        val lastMessage = messages.lastOrNull()
        if (lastMessage == null) {
            val elapsedFromCreation = currentTimeMillis - companion.createdAt
            return if (elapsedFromCreation >= 6 * 60 * 60 * 1000L) {
                ProactiveTriggerReason.RANDOM_THOUGHT
            } else {
                null
            }
        }

        val memory = companion.memory
        val maxSpam = companion.currentMood.maxProactiveSpamCount
        val consecutiveCount = memory.consecutiveProactiveCount

        val lastProactive = memory.lastProactiveMessageTimestamp
        val isAlreadyProactiveAtOrAfterLast = lastMessage.sender == MessageSender.AI &&
                lastProactive != null &&
                lastProactive >= lastMessage.timestamp

        val effectiveCount = if (consecutiveCount > 0) {
            consecutiveCount
        } else if (isAlreadyProactiveAtOrAfterLast) {
            1
        } else {
            0
        }

        val elapsedMillis = currentTimeMillis - lastMessage.timestamp
        val elapsedMinutes = elapsedMillis / (1000 * 60)
        val elapsedHours = elapsedMillis / (1000 * 60 * 60)

        if (effectiveCount >= maxSpam) {
            return if (elapsedHours >= 14) {
                ProactiveTriggerReason.RANDOM_THOUGHT
            } else {
                null
            }
        }

        val lastUserMessage = messages.lastOrNull { it.sender == MessageSender.USER }
        val farewellActivity = memory.pendingFarewellActivity
            ?: lastUserMessage?.let { FarewellDetector.detectFarewell(it.text) }

        if (farewellActivity != null) {
            if (elapsedMinutes < 10) {
                return null
            }
            return if (effectiveCount == 0) {
                if (elapsedHours < 3) {
                    ProactiveTriggerReason.USER_FAREWELL_FOLLOWUP
                } else if (elapsedHours in 3..8) {
                    ProactiveTriggerReason.FOLLOW_UP_TOPIC
                } else {
                    ProactiveTriggerReason.RANDOM_THOUGHT
                }
            } else {
                ProactiveTriggerReason.MOOD_SPAM
            }
        }

        if (elapsedMinutes < 1) {
            return null
        }

        if (effectiveCount in 1 until maxSpam) {
            return ProactiveTriggerReason.MOOD_SPAM
        }

        val hasRecentActivity = !memory.lastActivity.isNullOrBlank() &&
                memory.activityTimestamp != null &&
                (currentTimeMillis - memory.activityTimestamp) <= 12 * 60 * 60 * 1000L

        if (hasRecentActivity && elapsedHours in 1..12) {
            return ProactiveTriggerReason.ACTIVITY_RETURN
        }

        if (lastMessage.sender == MessageSender.AI) {
            return when {
                elapsedHours in 0..2 -> ProactiveTriggerReason.RAPID_FOLLOW_UP
                elapsedHours in 3..14 -> ProactiveTriggerReason.UNANSWERED_PING
                else -> ProactiveTriggerReason.RANDOM_THOUGHT
            }
        }

        return when {
            elapsedHours in 0..2 -> ProactiveTriggerReason.RAPID_FOLLOW_UP
            elapsedHours in 3..8 -> ProactiveTriggerReason.FOLLOW_UP_TOPIC
            else -> ProactiveTriggerReason.RANDOM_THOUGHT
        }
    }
}

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

        if (lastMessage.sender == MessageSender.AI) {
            val lastProactive = companion.memory.lastProactiveMessageTimestamp
            if (lastProactive != null && lastProactive >= lastMessage.timestamp) {
                return null
            }
        }

        val elapsedMillis = currentTimeMillis - lastMessage.timestamp
        val elapsedHours = elapsedMillis / (1000 * 60 * 60)
        if (elapsedHours < 3) {
            return null
        }

        if (lastMessage.sender == MessageSender.AI) {
            return if (elapsedHours in 3..14) {
                ProactiveTriggerReason.UNANSWERED_PING
            } else {
                ProactiveTriggerReason.RANDOM_THOUGHT
            }
        }

        val memory = companion.memory
        val hasRecentActivity = !memory.lastActivity.isNullOrBlank() &&
                memory.activityTimestamp != null &&
                (currentTimeMillis - memory.activityTimestamp) <= 12 * 60 * 60 * 1000L

        return when {
            hasRecentActivity -> ProactiveTriggerReason.ACTIVITY_RETURN
            elapsedHours in 3..8 -> ProactiveTriggerReason.FOLLOW_UP_TOPIC
            else -> ProactiveTriggerReason.RANDOM_THOUGHT
        }
    }
}


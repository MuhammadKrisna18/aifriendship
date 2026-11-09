package com.example.aifriendship.domain.service

import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.CompanionMemory
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.ProactiveTriggerReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ProactiveMessageEvaluatorTest {

    private fun getMiddayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }

    private fun getMidnightTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }

    @Test
    fun evaluate_quietHours_returnsNull() {
        val now = getMidnightTimestamp()
        val companion = AiCompanion(id = "comp-1", name = "Lia")
        val messages = listOf(
            ChatMessage(
                companionId = "comp-1",
                sender = MessageSender.USER,
                text = "Halo",
                timestamp = now - 5 * 60 * 60 * 1000L
            )
        )

        val reason = ProactiveMessageEvaluator.evaluate(companion, messages, now)
        assertNull(reason)
    }

    @Test
    fun evaluate_tooSoon_returnsNull() {
        val now = getMiddayTimestamp()
        val companion = AiCompanion(id = "comp-1", name = "Lia")
        val messages = listOf(
            ChatMessage(
                companionId = "comp-1",
                sender = MessageSender.USER,
                text = "Halo",
                timestamp = now - 1 * 60 * 60 * 1000L
            )
        )

        val reason = ProactiveMessageEvaluator.evaluate(companion, messages, now)
        assertNull(reason)
    }

    @Test
    fun evaluate_antiSpam_lastMessageAlreadyProactive_returnsNull() {
        val now = getMiddayTimestamp()
        val messageTime = now - 5 * 60 * 60 * 1000L
        val companion = AiCompanion(
            id = "comp-1",
            name = "Lia",
            memory = CompanionMemory(
                lastProactiveMessageTimestamp = messageTime
            )
        )
        val messages = listOf(
            ChatMessage(
                companionId = "comp-1",
                sender = MessageSender.AI,
                text = "Dih diread doang...",
                timestamp = messageTime
            )
        )

        val reason = ProactiveMessageEvaluator.evaluate(companion, messages, now)
        assertNull(reason)
    }

    @Test
    fun evaluate_unansweredAiPing_after4Hours_returnsUnansweredPing() {
        val now = getMiddayTimestamp()
        val messageTime = now - 4 * 60 * 60 * 1000L
        val companion = AiCompanion(id = "comp-1", name = "Lia")
        val messages = listOf(
            ChatMessage(
                companionId = "comp-1",
                sender = MessageSender.AI,
                text = "Menurut lu gimana?",
                timestamp = messageTime
            )
        )

        val reason = ProactiveMessageEvaluator.evaluate(companion, messages, now)
        assertEquals(ProactiveTriggerReason.UNANSWERED_PING, reason)
    }

    @Test
    fun evaluate_afterUserChat_recentActivity_returnsActivityReturn() {
        val now = getMiddayTimestamp()
        val messageTime = now - 4 * 60 * 60 * 1000L
        val companion = AiCompanion(
            id = "comp-1",
            name = "Lia",
            memory = CompanionMemory(
                lastActivity = "Nonton drakor",
                activityTimestamp = now - 3 * 60 * 60 * 1000L
            )
        )
        val messages = listOf(
            ChatMessage(
                companionId = "comp-1",
                sender = MessageSender.USER,
                text = "Oke deh",
                timestamp = messageTime
            )
        )

        val reason = ProactiveMessageEvaluator.evaluate(companion, messages, now)
        assertEquals(ProactiveTriggerReason.ACTIVITY_RETURN, reason)
    }

    @Test
    fun evaluate_afterUserChat_elapsed4Hours_returnsFollowUp() {
        val now = getMiddayTimestamp()
        val messageTime = now - 4 * 60 * 60 * 1000L
        val companion = AiCompanion(id = "comp-1", name = "Lia")
        val messages = listOf(
            ChatMessage(
                companionId = "comp-1",
                sender = MessageSender.USER,
                text = "Gue mau meeting dulu",
                timestamp = messageTime
            )
        )

        val reason = ProactiveMessageEvaluator.evaluate(companion, messages, now)
        assertEquals(ProactiveTriggerReason.FOLLOW_UP_TOPIC, reason)
    }
}


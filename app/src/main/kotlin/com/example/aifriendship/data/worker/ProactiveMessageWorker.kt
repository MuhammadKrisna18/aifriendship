package com.example.aifriendship.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aifriendship.AiFriendshipApplication
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.ProactiveTriggerReason
import com.example.aifriendship.domain.service.FarewellDetector
import com.example.aifriendship.domain.service.ProactiveMessageEvaluator
import com.example.aifriendship.presentation.notification.ProactiveNotificationHelper
import kotlinx.coroutines.flow.first

class ProactiveMessageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val application = applicationContext as? AiFriendshipApplication ?: return Result.failure()
        val container = application.container
        val userRepository = container.userRepository
        val chatRepository = container.chatRepository
        val aiService = container.aiService

        val profile = userRepository.getUserProfile()
        val activeCompanion = profile.activeCompanion

        val history = chatRepository.getMessagesByCompanion(activeCompanion.id).first()
        val triggerReason = ProactiveMessageEvaluator.evaluate(activeCompanion, history) ?: return Result.success()

        val generationResult = aiService.generateProactiveMessage(
            companion = activeCompanion,
            userProfile = profile,
            history = history,
            reason = triggerReason
        )

        return generationResult.fold(
            onSuccess = { proactiveText ->
                val now = System.currentTimeMillis()
                val chatMessage = ChatMessage(
                    companionId = activeCompanion.id,
                    sender = MessageSender.AI,
                    text = proactiveText,
                    timestamp = now,
                    status = MessageDeliveryStatus.SENT
                )
                chatRepository.saveMessage(chatMessage)

                val newCount = activeCompanion.memory.consecutiveProactiveCount + 1
                val updatedFarewell = if (triggerReason == ProactiveTriggerReason.USER_FAREWELL_FOLLOWUP) {
                    null
                } else {
                    activeCompanion.memory.pendingFarewellActivity
                }

                val updatedMemory = activeCompanion.memory.copy(
                    lastProactiveMessageTimestamp = now,
                    consecutiveProactiveCount = newCount,
                    pendingFarewellActivity = updatedFarewell
                )
                val updatedCompanion = activeCompanion.copy(memory = updatedMemory)
                userRepository.updateCompanion(updatedCompanion)

                ProactiveNotificationHelper.showMessageNotification(
                    context = applicationContext,
                    companion = activeCompanion,
                    messageText = proactiveText
                )

                if (newCount < activeCompanion.currentMood.maxProactiveSpamCount) {
                    val nextDelay = FarewellDetector.suggestRapidDelayMinutes()
                    ProactiveScheduler.scheduleFollowUp(
                        context = applicationContext,
                        companionId = activeCompanion.id,
                        delayMinutes = nextDelay
                    )
                }

                Result.success()
            },
            onFailure = {
                Result.retry()
            }
        )
    }
}

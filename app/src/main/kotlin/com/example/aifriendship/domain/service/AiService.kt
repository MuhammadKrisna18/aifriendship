package com.example.aifriendship.domain.service

import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.AiResponse
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.ProactiveTriggerReason
import com.example.aifriendship.domain.model.QuotedMessage
import com.example.aifriendship.domain.model.UserProfile

interface AiService {
    suspend fun generateReply(
        history: List<ChatMessage>,
        quotedMessage: QuotedMessage? = null,
        userProfile: UserProfile? = null
    ): Result<AiResponse>

    suspend fun generateProactiveMessage(
        companion: AiCompanion,
        userProfile: UserProfile,
        history: List<ChatMessage>,
        reason: ProactiveTriggerReason
    ): Result<String>

    fun getActiveModel(): String
    fun setActiveModel(modelName: String)
}

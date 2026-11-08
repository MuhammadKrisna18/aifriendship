package com.example.aifriendship.domain.service

import com.example.aifriendship.domain.model.AiResponse
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.QuotedMessage

interface AiService {
    suspend fun generateReply(
        history: List<ChatMessage>,
        quotedMessage: QuotedMessage? = null
    ): Result<AiResponse>

    fun getActiveModel(): String
    fun setActiveModel(modelName: String)
}


package com.example.aifriendship.domain.repository

import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.KaleaStatus
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun saveMessages(messages: List<ChatMessage>)
    suspend fun deleteMessage(id: String)
    suspend fun clearMessages()
    fun getKaleaStatus(): Flow<KaleaStatus>
    suspend fun updateKaleaStatus(status: KaleaStatus)
}


package com.example.aifriendship.data.local

import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.KaleaStatus
import kotlinx.coroutines.flow.Flow

interface ChatLocalDataSource {
    fun getMessages(): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun saveMessages(messages: List<ChatMessage>)
    suspend fun deleteMessage(id: String)
    suspend fun clearMessages()
    fun getKaleaStatus(): Flow<KaleaStatus>
    suspend fun saveKaleaStatus(status: KaleaStatus)
}


package com.example.aifriendship.domain.repository

import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.LiaStatus
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(): Flow<List<ChatMessage>>
    fun getMessagesByCompanion(companionId: String): Flow<List<ChatMessage>>
    fun getLatestMessagesPerCompanion(): Flow<Map<String, ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun saveMessages(messages: List<ChatMessage>)
    suspend fun deleteMessage(id: String)
    suspend fun clearMessages()
    suspend fun clearMessagesByCompanion(companionId: String)
    fun getLiaStatus(): Flow<LiaStatus>
    suspend fun updateLiaStatus(status: LiaStatus)
}

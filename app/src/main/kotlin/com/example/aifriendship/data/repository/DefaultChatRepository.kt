package com.example.aifriendship.data.repository

import com.example.aifriendship.data.local.ChatLocalDataSource
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class DefaultChatRepository(
    private val localDataSource: ChatLocalDataSource
) : ChatRepository {

    override fun getMessages(): Flow<List<ChatMessage>> {
        return localDataSource.getMessages()
    }

    override fun getMessagesByCompanion(companionId: String): Flow<List<ChatMessage>> {
        return localDataSource.getMessagesByCompanion(companionId)
    }

    override fun getLatestMessagesPerCompanion(): Flow<Map<String, ChatMessage>> {
        return localDataSource.getLatestMessagesPerCompanion()
    }

    override suspend fun saveMessage(message: ChatMessage) {
        localDataSource.saveMessage(message)
    }

    override suspend fun saveMessages(messages: List<ChatMessage>) {
        localDataSource.saveMessages(messages)
    }

    override suspend fun deleteMessage(id: String) {
        localDataSource.deleteMessage(id)
    }

    override suspend fun clearMessages() {
        localDataSource.clearMessages()
    }

    override suspend fun clearMessagesByCompanion(companionId: String) {
        localDataSource.clearMessagesByCompanion(companionId)
    }

    override fun getLiaStatus(): Flow<LiaStatus> {
        return localDataSource.getLiaStatus()
    }

    override suspend fun updateLiaStatus(status: LiaStatus) {
        localDataSource.saveLiaStatus(status)
    }
}

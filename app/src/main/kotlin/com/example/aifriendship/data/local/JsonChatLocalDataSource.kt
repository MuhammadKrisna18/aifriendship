package com.example.aifriendship.data.local

import android.content.Context
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.LiaStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class JsonChatLocalDataSource(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }
) : ChatLocalDataSource {

    private val mutex = Mutex()
    private val messagesFile by lazy { File(context.filesDir, "chat_history.json") }
    private val statusFile by lazy { File(context.filesDir, "lia_status.json") }

    private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _statusFlow = MutableStateFlow(LiaStatus())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val loadedMessages = readMessagesFromFile()
        _messagesFlow.value = loadedMessages

        val loadedStatus = readStatusFromFile()
        _statusFlow.value = loadedStatus
    }

    override fun getMessages(): Flow<List<ChatMessage>> = _messagesFlow.asStateFlow()

    override suspend fun saveMessage(message: ChatMessage) {
        mutex.withLock {
            val updated = _messagesFlow.value + message
            _messagesFlow.value = updated
            writeMessagesToFile(updated)
        }
    }

    override suspend fun saveMessages(messages: List<ChatMessage>) {
        mutex.withLock {
            val updated = _messagesFlow.value + messages
            _messagesFlow.value = updated
            writeMessagesToFile(updated)
        }
    }

    override suspend fun deleteMessage(id: String) {
        mutex.withLock {
            val updated = _messagesFlow.value.filterNot { it.id == id }
            _messagesFlow.value = updated
            writeMessagesToFile(updated)
        }
    }

    override suspend fun clearMessages() {
        mutex.withLock {
            _messagesFlow.value = emptyList()
            writeMessagesToFile(emptyList())
        }
    }

    override fun getLiaStatus(): Flow<LiaStatus> = _statusFlow.asStateFlow()

    override suspend fun saveLiaStatus(status: LiaStatus) {
        mutex.withLock {
            _statusFlow.value = status
            writeStatusToFile(status)
        }
    }

    private fun readMessagesFromFile(): List<ChatMessage> {
        return try {
            if (messagesFile.exists()) {
                val content = messagesFile.readText()
                if (content.isNotBlank()) {
                    json.decodeFromString<List<ChatMessage>>(content)
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun writeMessagesToFile(messages: List<ChatMessage>) {
        withContext(Dispatchers.IO) {
            try {
                val content = json.encodeToString(messages)
                messagesFile.writeText(content)
            } catch (_: Exception) {
            }
        }
    }

    private fun readStatusFromFile(): LiaStatus {
        return try {
            if (statusFile.exists()) {
                val content = statusFile.readText()
                if (content.isNotBlank()) {
                    json.decodeFromString<LiaStatus>(content)
                } else {
                    LiaStatus()
                }
            } else {
                LiaStatus()
            }
        } catch (_: Exception) {
            LiaStatus()
        }
    }

    private suspend fun writeStatusToFile(status: LiaStatus) {
        withContext(Dispatchers.IO) {
            try {
                val content = json.encodeToString(status)
                statusFile.writeText(content)
            } catch (_: Exception) {
            }
        }
    }
}

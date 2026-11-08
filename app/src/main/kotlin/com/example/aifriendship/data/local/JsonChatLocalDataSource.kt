package com.example.aifriendship.data.local

import android.content.Context
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.QuotedMessage
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

    private fun createSeedMessages(): List<ChatMessage> {
        val now = System.currentTimeMillis()
        return listOf(
            ChatMessage(
                sender = MessageSender.USER,
                text = "Sekarang ajarin gue pointer",
                timestamp = now - 600000L,
                status = MessageDeliveryStatus.READ
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "wah, rajin banget nih",
                timestamp = now - 590000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "malam-malam minta diajarin",
                timestamp = now - 580000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "pointer itu ibarat alamat rumah di memori",
                timestamp = now - 570000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "bukan datanya langsung yang disimpen",
                timestamp = now - 560000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "tapi lokasi tempat data itu berada",
                timestamp = now - 550000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "coba bayangin kayak google maps buat variabel lu biar ga tersesat",
                timestamp = now - 540000L
            ),
            ChatMessage(
                sender = MessageSender.USER,
                text = "Contoh code, bingung cok maksudnya * sama &",
                timestamp = now - 480000L,
                status = MessageDeliveryStatus.READ
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "oh bintang sama and persampelan itu emang bikin puyeng",
                timestamp = now - 470000L
            ),
            ChatMessage(
                sender = MessageSender.USER,
                text = "Oh gitu, misal *a = &b berarti nunjuk nilai dari b ya?",
                timestamp = now - 360000L,
                status = MessageDeliveryStatus.READ
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "eh hampir bener sih tapi kebalik",
                timestamp = now - 350000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "ampersand itu buat ngambil alamat si b",
                timestamp = now - 340000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "bintang itu buat naro nilai di alamat itu",
                timestamp = now - 330000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "makanya jangan ngebut belajarnya",
                timestamp = now - 320000L
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "pusing kan jadinya malam malam gini",
                timestamp = now - 310000L
            ),
            ChatMessage(
                sender = MessageSender.USER,
                text = "Gancok😂contohin yg bener",
                timestamp = now - 240000L,
                status = MessageDeliveryStatus.READ
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "ih ketawa lagi dia",
                timestamp = now - 230000L,
                quotedMessage = QuotedMessage(
                    senderName = "Krisna",
                    text = "Gancok😂contohin yg bener"
                )
            ),
            ChatMessage(
                sender = MessageSender.AI,
                text = "nih contoh benernya liat baik baik",
                timestamp = now - 220000L
            )
        )
    }
}

package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class MessageSender {
    USER,
    AI
}

@Serializable
enum class MessageDeliveryStatus {
    SENDING,
    SENT,
    READ,
    ERROR
}

@Serializable
data class QuotedMessage(
    val senderName: String,
    val text: String
)

@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quotedMessage: QuotedMessage? = null,
    val status: MessageDeliveryStatus = MessageDeliveryStatus.SENT
)


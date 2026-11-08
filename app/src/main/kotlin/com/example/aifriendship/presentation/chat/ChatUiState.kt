package com.example.aifriendship.presentation.chat

import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.KaleaStatus

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val kaleaStatus: KaleaStatus = KaleaStatus(),
    val inputText: String = "",
    val replyingTo: ChatMessage? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)


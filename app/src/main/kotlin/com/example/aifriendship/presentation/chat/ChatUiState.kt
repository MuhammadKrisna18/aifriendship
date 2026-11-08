package com.example.aifriendship.presentation.chat

import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.LiaStatus

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val liaStatus: LiaStatus = LiaStatus(),
    val inputText: String = "",
    val replyingTo: ChatMessage? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val updateInfo: AppUpdateInfo? = null,
    val isCheckingUpdate: Boolean = false
)

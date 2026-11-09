package com.example.aifriendship.presentation.chat

import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.UserProfile

enum class OnboardingStep {
    COMPLETED,
    ASK_NAME
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val liaStatus: LiaStatus = LiaStatus(),
    val inputText: String = "",
    val replyingTo: ChatMessage? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val updateInfo: AppUpdateInfo? = null,
    val isCheckingUpdate: Boolean = false,
    val isDownloadingUpdate: Boolean = false,
    val downloadProgress: Int = 0,
    val userProfile: UserProfile = UserProfile(),
    val onboardingStep: OnboardingStep = OnboardingStep.COMPLETED,
    val isUserProfileDialogVisible: Boolean = false,
    val isMemoryDialogVisible: Boolean = false
)

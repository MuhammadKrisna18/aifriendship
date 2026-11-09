package com.example.aifriendship.presentation.home

import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.UserProfile

enum class HomeTab(val title: String) {
    CHATS("OBROLAN"),
    COMPANIONS("SAHABAT AI")
}

data class HomeUiState(
    val userProfile: UserProfile = UserProfile(),
    val latestMessages: Map<String, ChatMessage> = emptyMap(),
    val selectedTab: HomeTab = HomeTab.CHATS,
    val showProfileDialog: Boolean = false,
    val searchQuery: String = "",
    val updateInfo: AppUpdateInfo? = null,
    val isDownloadingUpdate: Boolean = false,
    val downloadProgress: Int = 0,
    val selectedCompanionForMemory: AiCompanion? = null
)

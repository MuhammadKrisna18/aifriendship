package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LiaStatus(
    val subtitle: String = "gemas juga ngajarin dia ngoding",
    val isOnline: Boolean = true,
    val isTyping: Boolean = false,
    val isCooldown: Boolean = false,
    val cooldownSecondsRemaining: Int = 0,
    val cooldownUntilTimestamp: Long = 0L
)

data class LiaProfile(
    val name: String = "Lia",
    val levelBadge: Int = 5,
    val avatarUrl: String = "",
    val status: LiaStatus = LiaStatus()
)


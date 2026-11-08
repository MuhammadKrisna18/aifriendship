package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class KaleaStatus(
    val subtitle: String = "gemas juga ngajarin dia ngoding",
    val isOnline: Boolean = true,
    val isTyping: Boolean = false,
    val isCooldown: Boolean = false,
    val cooldownSecondsRemaining: Int = 0
)

data class KaleaProfile(
    val name: String = "Kalea Restika",
    val levelBadge: Int = 5,
    val avatarUrl: String = "",
    val status: KaleaStatus = KaleaStatus()
)


package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String = "",
    val companion: AiCompanion = AiCompanion(),
    val companions: List<AiCompanion> = emptyList(),
    val activeCompanionId: String = "default-companion-lia",
    val isOnboardingCompleted: Boolean = false
) {
    val allCompanions: List<AiCompanion>
        get() {
            if (companions.isNotEmpty()) return companions
            return listOf(companion)
        }

    val activeCompanion: AiCompanion
        get() = allCompanions.firstOrNull { it.id == activeCompanionId }
            ?: allCompanions.firstOrNull()
            ?: companion
}

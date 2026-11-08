package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AiCompanion(
    val name: String = "Lia",
    val trait: CompanionTrait = CompanionTrait.BARBAR,
    val customTraitDescription: String = "",
    val avatarColorHex: String = "#6366F1"
)

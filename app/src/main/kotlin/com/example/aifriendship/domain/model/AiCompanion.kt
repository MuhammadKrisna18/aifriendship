package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AiCompanion(
    val id: String = "default-companion-lia",
    val name: String = "Lia",
    val trait: CompanionTrait = CompanionTrait.BARBAR,
    val traits: List<CompanionTrait> = emptyList(),
    val customTraitDescription: String = "",
    val avatarColorHex: String = "#6366F1",
    val relationship: CompanionRelationship = CompanionRelationship.ORANG_ASING,
    val memory: CompanionMemory = CompanionMemory(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val effectiveTraits: List<CompanionTrait>
        get() {
            if (traits.isNotEmpty()) return traits
            return listOf(trait)
        }

    val primaryTrait: CompanionTrait
        get() = effectiveTraits.firstOrNull() ?: trait

    val traitsSummary: String
        get() = effectiveTraits.joinToString(", ") {
            it.title.replace(Regex("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}]+\\s*"), "")
        }

    val currentRelationship: CompanionRelationship
        get() = memory.currentRelationship

    val currentMood: CompanionMood
        get() = memory.currentMood
}

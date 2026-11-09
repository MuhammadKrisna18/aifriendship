package com.example.aifriendship.presentation.setup

import com.example.aifriendship.domain.model.CompanionTrait
import com.example.aifriendship.domain.model.CompanionTraitCategory

data class CharacterSetupUiState(
    val userName: String = "",
    val companionName: String = "Lia",
    val selectedTraits: Set<CompanionTrait> = setOf(CompanionTrait.BARBAR),
    val selectedCategory: CompanionTraitCategory? = null,
    val avatarColorHex: String = "#6366F1",
    val isSaving: Boolean = false,
    val maxTraits: Int = 3
) {
    val selectedTrait: CompanionTrait
        get() = selectedTraits.firstOrNull() ?: CompanionTrait.BARBAR

    val customTraitDescription: String = ""

    val traitsSummary: String
        get() = selectedTraits.joinToString(", ") {
            it.title.replace(Regex("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}]+\\s*"), "")
        }
}

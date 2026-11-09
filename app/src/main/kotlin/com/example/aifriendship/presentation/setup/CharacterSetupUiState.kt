package com.example.aifriendship.presentation.setup

import com.example.aifriendship.domain.model.CompanionTrait

data class CharacterSetupUiState(
    val userName: String = "",
    val companionName: String = "Lia",
    val selectedTrait: CompanionTrait = CompanionTrait.BARBAR,
    val customTraitDescription: String = "",
    val avatarColorHex: String = "#6366F1",
    val isSaving: Boolean = false,
    val availableColors: List<String> = listOf(
        "#6366F1",
        "#EC4899",
        "#8B5CF6",
        "#10B981",
        "#F59E0B",
        "#06B6D4",
        "#F43F5E",
        "#3B82F6"
    ),
    val availableTraits: List<CompanionTrait> = CompanionTrait.entries.toList()
) {
    val traitsSummary: String
        get() = selectedTrait.title
}

package com.example.aifriendship.presentation.setup

import com.example.aifriendship.domain.model.CompanionTrait

data class CharacterSetupUiState(
    val userName: String = "",
    val companionName: String = "Lia",
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
    )
) {
    val selectedTrait: CompanionTrait
        get() = CompanionTrait.BARBAR

    val traitsSummary: String
        get() = "Sahabat AI Dinamis"
}

package com.example.aifriendship.presentation.setup

import com.example.aifriendship.domain.model.CompanionTrait
import com.example.aifriendship.domain.model.CompanionTraitCategory

data class CharacterSetupUiState(
    val userName: String = "",
    val companionName: String = "Lia",
    val selectedTrait: CompanionTrait = CompanionTrait.BARBAR,
    val customTraitDescription: String = "",
    val selectedCategory: CompanionTraitCategory? = null,
    val avatarColorHex: String = "#6366F1",
    val isSaving: Boolean = false
)


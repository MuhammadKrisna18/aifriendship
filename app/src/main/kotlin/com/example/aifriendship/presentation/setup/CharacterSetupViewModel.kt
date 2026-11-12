package com.example.aifriendship.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aifriendship.AiFriendshipApplication
import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.CompanionMemory
import com.example.aifriendship.domain.model.CompanionMood
import com.example.aifriendship.domain.model.CompanionRelationship
import com.example.aifriendship.domain.model.CompanionTrait
import com.example.aifriendship.domain.model.CompanionTraitCategory
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.RandomCompanionGenerator
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CharacterSetupViewModel(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterSetupUiState())
    val uiState: StateFlow<CharacterSetupUiState> = _uiState.asStateFlow()

    init {
        loadExistingProfile()
    }

    private fun loadExistingProfile() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            if (profile.name.isNotBlank()) {
                _uiState.update { current ->
                    current.copy(userName = profile.name)
                }
            }
        }
    }

    fun onUserNameChanged(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun onCompanionNameChanged(name: String) {
        _uiState.update { it.copy(companionName = name) }
    }

    fun onAvatarColorSelected(hex: String) {
        _uiState.update { it.copy(avatarColorHex = hex) }
    }

    fun onTraitSelected(trait: CompanionTrait) {
        _uiState.update { current ->
            val isCurrentNameDefaultOfAnyTrait = CompanionTrait.entries.any { it.defaultName.equals(current.companionName, ignoreCase = true) }
            val newName = if (current.companionName.isBlank() || isCurrentNameDefaultOfAnyTrait) {
                trait.defaultName
            } else {
                current.companionName
            }
            val newColor = when (trait.category) {
                CompanionTraitCategory.CASUAL -> "#6366F1"
                CompanionTraitCategory.WARM -> "#EC4899"
                CompanionTraitCategory.SPICY -> "#F43F5E"
                CompanionTraitCategory.SMART -> "#06B6D4"
            }
            current.copy(
                selectedTrait = trait,
                companionName = newName,
                avatarColorHex = newColor
            )
        }
    }

    fun onCustomTraitDescriptionChanged(description: String) {
        _uiState.update { it.copy(customTraitDescription = description) }
    }

    fun onRandomize() {
        val randomCompanion = RandomCompanionGenerator.generateRandom()
        _uiState.update { current ->
            current.copy(
                companionName = randomCompanion.name,
                avatarColorHex = randomCompanion.avatarColorHex,
                selectedTrait = randomCompanion.primaryTrait
            )
        }
    }

    fun saveAndStartChat(onComplete: () -> Unit) {
        val state = _uiState.value
        val cleanUserName = state.userName.trim().ifBlank { "Teman" }
        val cleanCompanionName = state.companionName.trim().ifBlank { "Lia" }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val companion = AiCompanion(
                id = UUID.randomUUID().toString(),
                name = cleanCompanionName,
                trait = state.selectedTrait,
                traits = listOf(state.selectedTrait),
                customTraitDescription = state.customTraitDescription.trim(),
                avatarColorHex = state.avatarColorHex,
                relationship = CompanionRelationship.ORANG_ASING,
                memory = CompanionMemory(
                    currentRelationship = CompanionRelationship.ORANG_ASING,
                    currentMood = CompanionMood.BIASA
                )
            )

            val currentProfile = userRepository.getUserProfile()
            val existingCompanions = currentProfile.allCompanions
            val updatedCompanions = existingCompanions + companion

            val updatedProfile = currentProfile.copy(
                name = cleanUserName,
                companion = companion,
                companions = updatedCompanions,
                activeCompanionId = companion.id,
                isOnboardingCompleted = true
            )
            userRepository.saveUserProfile(updatedProfile)

            val greeting = buildFirstGreeting(cleanUserName, companion)
            chatRepository.saveMessage(greeting)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Online",
                    isOnline = true,
                    isTyping = false
                )
            )

            _uiState.update { it.copy(isSaving = false) }
            onComplete()
        }
    }

    private fun buildFirstGreeting(userName: String, companion: AiCompanion): ChatMessage {
        val greetingText = when (companion.primaryTrait) {
            CompanionTrait.BARBAR -> "Halo, siapa nih? Kenalin gue ${companion.name}. Lu yang namanya $userName kan?"
            CompanionTrait.SAVAGE -> "Siapa ya? Lu $userName? Ada perlu apa?"
            CompanionTrait.TSUNDERE -> "Kamu $userName ya? ...Kenalin, aku ${companion.name}. Gak usah aneh-aneh ya."
            CompanionTrait.CHILL -> "Halo $userName, salam kenal ya. Gue ${companion.name}. Santai aja di sini."
            CompanionTrait.SOFT -> "Halo $userName, salam kenal ya... Aku ${companion.name}."
            CompanionTrait.CHEERFUL -> "Hai $userName! Salam kenal ya! Aku ${companion.name}, senang bisa kenal kamu! ✨"
            CompanionTrait.MATERNAL -> "Halo $userName, salam kenal. Nama saya ${companion.name}. Semoga hari kamu baik ya."
            CompanionTrait.ABSURD -> "Halo $userName! Aku ${companion.name}. Tiba-tiba muncul aja nih, salam kenal!"
            CompanionTrait.INDIE -> "Malam atau siang pun di sana, salam kenal $userName. Aku ${companion.name}."
            CompanionTrait.SMART -> "Halo $userName, salam kenal. Saya ${companion.name}."
            CompanionTrait.AMBITIOUS -> "Halo $userName, salam kenal. Aku ${companion.name}."
            CompanionTrait.CUSTOM -> "Halo $userName, salam kenal. Aku ${companion.name}."
        }

        return ChatMessage(
            companionId = companion.id,
            sender = MessageSender.AI,
            text = greetingText,
            status = MessageDeliveryStatus.SENT
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AiFriendshipApplication)
                CharacterSetupViewModel(
                    userRepository = application.container.userRepository,
                    chatRepository = application.container.chatRepository
                )
            }
        }
    }
}

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

    fun onRandomize() {
        val randomCompanion = RandomCompanionGenerator.generateRandom()
        _uiState.update { current ->
            current.copy(
                companionName = randomCompanion.name,
                avatarColorHex = randomCompanion.avatarColorHex
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
                avatarColorHex = state.avatarColorHex,
                relationship = CompanionRelationship.TEMAN_BIASA,
                memory = CompanionMemory(
                    currentRelationship = CompanionRelationship.TEMAN_BIASA,
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
        val greetings = listOf(
            "Halo $userName! Salam kenal ya. Santai aja, ada yang pengen kamu obrolin atau ceritain hari ini? 😊",
            "Hai $userName! Senang bisa kenalan sama kamu. Lagi senggang atau lagi ada kesibukan nih?",
            "Halo $userName! Akhirnya kita bisa ngobrol di sini. Mau cerita santai atau bahas hal seru apa hari ini?"
        )
        val selectedGreeting = greetings.random()

        return ChatMessage(
            companionId = companion.id,
            sender = MessageSender.AI,
            text = selectedGreeting,
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

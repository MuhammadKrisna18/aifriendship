package com.example.aifriendship.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aifriendship.AiFriendshipApplication
import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.CompanionTrait
import com.example.aifriendship.domain.model.CompanionTraitCategory
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.RandomCompanionGenerator
import com.example.aifriendship.domain.model.UserProfile
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
                val companion = profile.activeCompanion
                _uiState.update { current ->
                    current.copy(
                        userName = profile.name,
                        companionName = companion.name,
                        selectedTraits = companion.effectiveTraits.toSet(),
                        avatarColorHex = companion.avatarColorHex
                    )
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

    fun onTraitToggled(trait: CompanionTrait) {
        _uiState.update { current ->
            val currentSelected = current.selectedTraits
            val newSelected = if (currentSelected.contains(trait)) {
                if (currentSelected.size > 1) currentSelected - trait else currentSelected
            } else {
                if (currentSelected.size < current.maxTraits) currentSelected + trait else currentSelected
            }
            val primary = newSelected.firstOrNull() ?: CompanionTrait.BARBAR
            val updatedName = if (current.companionName.isBlank() || current.companionName == current.selectedTrait.defaultName) {
                primary.defaultName
            } else {
                current.companionName
            }
            current.copy(
                selectedTraits = newSelected,
                companionName = updatedName
            )
        }
    }

    fun onCategorySelected(category: CompanionTraitCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onRandomize() {
        val randomCompanion = RandomCompanionGenerator.generateRandom()
        _uiState.update { current ->
            current.copy(
                companionName = randomCompanion.name,
                selectedTraits = randomCompanion.effectiveTraits.toSet(),
                avatarColorHex = randomCompanion.avatarColorHex
            )
        }
    }

    fun saveAndStartChat(onComplete: () -> Unit) {
        val state = _uiState.value
        val cleanUserName = state.userName.trim()
        val primaryTrait = state.selectedTrait
        val cleanCompanionName = state.companionName.trim().ifBlank { primaryTrait.defaultName }

        if (cleanUserName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val companion = AiCompanion(
                id = UUID.randomUUID().toString(),
                name = cleanCompanionName,
                trait = primaryTrait,
                traits = state.selectedTraits.toList(),
                avatarColorHex = state.avatarColorHex
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
        val primary = companion.primaryTrait
        val greetingText = when (primary) {
            CompanionTrait.BARBAR -> "Yo $userName! Akhirnya lu dateng juga wkwk. Santai aja, mau cerita atau ngobrolin apa nih hari ini?"
            CompanionTrait.CHILL -> "Halo $userName... santai dulu ya. Hari ini gimana? Aman kan?"
            CompanionTrait.ABSURD -> "HALO $userName!! Tiba-tiba gue kepikiran sesuatu yang random banget wkwk, lu lagi sibuk gak?"
            CompanionTrait.SOFT -> "Halo $userName... Senang bisa kenal kamu. Kalau ada yang mau diceritakan, aku siap dengerin ya."
            CompanionTrait.CHEERFUL -> "Haii $userName! ✨ Semangat banget hari ini ketemu kamu! Lagi ngapain nih?"
            CompanionTrait.MATERNAL -> "Halo $userName, sudah makan belum hari ini? Jangan lupa jaga kesehatan ya, ada apa nih?"
            CompanionTrait.TSUNDERE -> "Hmph, $userName ya? Jangan mikir yang aneh-aneh dulu, gue cuma lagi luang aja makanya nyapa. Mau ngobrolin apa emangnya?"
            CompanionTrait.SAVAGE -> "Akhirnya nongol juga, $userName. Lu pasti butuh saran logis dari gue kan? Spill aja."
            CompanionTrait.INDIE -> "Malam atau sore yang tenang, $userName... Kadang obrolan sederhana bisa bikin pikiran lebih damai ya."
            CompanionTrait.SMART -> "Halo $userName! Menarik bisa kenalan. Mau diskusi teknologi, koding, atau ide gila apa hari ini?"
            CompanionTrait.AMBITIOUS -> "Halo $userName! Target hari ini udah beres belum? Tetap gas pol dan jangan kendor ya!"
            CompanionTrait.CUSTOM -> "Halo $userName! Senang bisa kenal kamu, yuk mulai ngobrol santai."
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

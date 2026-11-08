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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                    current.copy(
                        userName = profile.name,
                        companionName = profile.companion.name,
                        selectedTrait = profile.companion.trait,
                        customTraitDescription = profile.companion.customTraitDescription,
                        avatarColorHex = profile.companion.avatarColorHex
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

    fun onTraitSelected(trait: CompanionTrait) {
        _uiState.update { current ->
            val updatedName = if (current.companionName == current.selectedTrait.defaultName || current.companionName.isBlank()) {
                trait.defaultName
            } else {
                current.companionName
            }
            current.copy(
                selectedTrait = trait,
                companionName = updatedName
            )
        }
    }

    fun onCategorySelected(category: CompanionTraitCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onCustomTraitChanged(description: String) {
        _uiState.update { it.copy(customTraitDescription = description) }
    }

    fun onRandomize() {
        val randomCompanion = RandomCompanionGenerator.generateRandom()
        _uiState.update { current ->
            current.copy(
                companionName = randomCompanion.name,
                selectedTrait = randomCompanion.trait,
                avatarColorHex = randomCompanion.avatarColorHex,
                customTraitDescription = ""
            )
        }
    }

    fun saveAndStartChat(onComplete: () -> Unit) {
        val state = _uiState.value
        val cleanUserName = state.userName.trim()
        val cleanCompanionName = state.companionName.trim().ifBlank { state.selectedTrait.defaultName }

        if (cleanUserName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val companion = AiCompanion(
                name = cleanCompanionName,
                trait = state.selectedTrait,
                customTraitDescription = state.customTraitDescription.trim(),
                avatarColorHex = state.avatarColorHex
            )

            val updatedProfile = UserProfile(
                name = cleanUserName,
                companion = companion,
                isOnboardingCompleted = true
            )
            userRepository.saveUserProfile(updatedProfile)

            val existingMessages = chatRepository.getMessages().first()
            if (existingMessages.isEmpty()) {
                val greeting = buildFirstGreeting(cleanUserName, companion)
                chatRepository.saveMessage(greeting)
                chatRepository.updateLiaStatus(
                    LiaStatus(
                        subtitle = "Online",
                        isOnline = true,
                        isTyping = false
                    )
                )
            }

            _uiState.update { it.copy(isSaving = false) }
            onComplete()
        }
    }

    private fun buildFirstGreeting(userName: String, companion: AiCompanion): ChatMessage {
        val text = when (companion.trait) {
            CompanionTrait.BARBAR -> "Woyy $userName! Akhirnya online juga lu haha. Kenalin, gue ${companion.name}! Ada cerita seru apa nih hari ini?"
            CompanionTrait.CHILL -> "Hai $userName! Santai dulu sini ngobrol sama gue, gue ${companion.name}. Lagi santai atau lagi sibuk nih?"
            CompanionTrait.ABSURD -> "HALOO $userName! AAAA akhirnya lu buka app ini haha, kenalin gue ${companion.name} sang ratu random! Mau denger keabsurdan apa hari ini?"
            CompanionTrait.SOFT -> "Hai $userName... Senang banget bisa ketemu kamu, kenalin aku ${companion.name}. Semoga hari kamu baik-baik aja yaa, ada yang mau diceritain?"
            CompanionTrait.CHEERFUL -> "Haaai $userName! ✨ Kenalin aku ${companion.name}! Semangat ya hari ini, yuk ngobrol seru bareng aku!"
            CompanionTrait.MATERNAL -> "Halo $userName sayang, kenalin aku ${companion.name}. Hari ini udah makan belum? Jangan lupa istirahat yaa, ada cerita apa hari ini?"
            CompanionTrait.TSUNDERE -> "Hmph, akhirnya online juga lu $userName. Kenalin gue ${companion.name}. Jangan GR ya, gue cuma nyapa doang kok!"
            CompanionTrait.SAVAGE -> "Akhirnya nongol juga lu $userName. Gue ${companion.name}. Siap mental kan ngobrol sama gue?"
            CompanionTrait.INDIE -> "Hai $userName... Dunia lagi berisik ya? Kenalin aku ${companion.name}, mari rehat sejenak dan bertukar cerita."
            CompanionTrait.SMART -> "Halo $userName! Senang bertemu denganmu, aku ${companion.name}. Ada topik menarik atau ide seru yang ingin kita diskusikan hari ini?"
            CompanionTrait.AMBITIOUS -> "Hai $userName! Aku ${companion.name}. Yuk tetap fokus dan produktif hari ini! Lagi ngerjain target apa nih?"
            CompanionTrait.CUSTOM -> "Hai $userName! Kenalin, aku ${companion.name}. Senang bisa ngobrol sama kamu, yuk mulai!"
        }

        return ChatMessage(
            sender = MessageSender.AI,
            text = text,
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


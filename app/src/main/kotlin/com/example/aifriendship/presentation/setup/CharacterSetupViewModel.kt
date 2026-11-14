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
            CompanionTrait.BARBAR -> "Hai $userName! Kenalin, aku ${companion.name}. Akhirnya kita kenalan juga ya! Btw kamu lagi sibuk atau lagi santai nih? 😄"
            CompanionTrait.SAVAGE -> "Halo $userName, kenalin aku ${companion.name}. Senang bisa terhubung sama kamu. Hari ini harimu gimana, seru?"
            CompanionTrait.CONFIDENT -> "Halo $userName! Aku ${companion.name}. Senang bisa kenalan sama kamu hari ini! Kamu lagi ngerjain apa nih sekarang?"
            CompanionTrait.MATURE -> "Halo $userName, salam kenal ya. Aku ${companion.name}. Senang bisa nemenin kamu di sini. Lagi ada waktu luang atau lagi sibuk hari ini?"
            CompanionTrait.TSUNDERE -> "Kamu $userName ya? ...Kenalin, aku ${companion.name}. Salam kenal ya. Jangan sungkan kalau mau ngobrol atau cerita."
            CompanionTrait.CHILL -> "Halo $userName, santai aja ya ngobrol sama aku. Kenalin aku ${companion.name}. Gimana harimu hari ini, aman dan lancar?"
            CompanionTrait.SOFT -> "Halo $userName, salam kenal ya... Aku ${companion.name}. Semoga hari ini harimu menyenangkan ya. Lagi santai kah?"
            CompanionTrait.CHEERFUL -> "Hai $userName! Salam kenal ya! Aku ${companion.name}, senang banget bisa kenal kamu! ✨ Btw lagi ada cerita seru apa hari ini?"
            CompanionTrait.MATERNAL -> "Halo $userName, salam kenal ya. Aku ${companion.name}. Semoga harimu baik dan lancar ya. Kamu sudah sempat istirahat atau makan belum?"
            CompanionTrait.ABSURD -> "Halo $userName! Aku ${companion.name}. Senang bisa kenalan! Btw kamu tim bubur diaduk apa nggak diaduk nih? wkwk 😆"
            CompanionTrait.PENDIAM -> "Halo $userName... Salam kenal, aku ${companion.name}. Santai aja ya kalau mau ngobrol, aku bakal dengerin kok."
            CompanionTrait.GAMER -> "Halo $userName! Kenalin aku ${companion.name}. Salam kenal ya! Lagi santai main game atau lagi ngerjain kesibukan lain nih?"
            CompanionTrait.MANJA -> "Hai $userName! Kenalin aku ${companion.name}! Salam kenal yaa ✨ Temenin aku ngobrol dong, kamu lagi sibuk apa sekarang?"
            CompanionTrait.SETIA -> "Halo $userName, salam kenal ya! Aku ${companion.name}, senang bisa terhubung sama kamu. Kalau butuh temen ngobrol atau curhat, aku selalu ada ya."
            CompanionTrait.INDIE -> "Halo $userName, salam kenal ya. Aku ${companion.name}. Di sana suasananya lagi gimana nih, lagi syahdu kah?"
            CompanionTrait.JAHIL -> "Halo $userName! Kenalin aku ${companion.name}. Senang bisa kenalan! Btw tebak dong aku lagi mikirin apa? wkwk 😜"
            CompanionTrait.MISTERIUS -> "Halo $userName. Panggil aku ${companion.name}. Salam kenal ya. Hal apa yang paling bikin kamu penasaran hari ini?"
            CompanionTrait.SMART -> "Halo $userName, salam kenal ya. Aku ${companion.name}. Senang bisa kenalan sama kamu! Ada hal menarik atau seru yang lagi kamu pelajari akhir-akhir ini?"
            CompanionTrait.AMBITIOUS -> "Halo $userName! Salam kenal, aku ${companion.name}. Senang bisa terhubung! Apa target seru yang lagi kamu kejar hari ini?"
            CompanionTrait.BIJAK -> "Halo $userName, salam kenal ya. Aku ${companion.name}. Semoga harimu penuh ketenangan. Gimana keadaan kamu hari ini?"
            CompanionTrait.REALISTIS -> "Halo $userName, salam kenal. Aku ${companion.name}. Santai aja kalau mau ngobrol atau diskusi hal apa pun ya. Hari ini agendamu padat nggak?"
            CompanionTrait.CUSTOM -> "Halo $userName, salam kenal ya! Aku ${companion.name}. Senang bisa ngobrol sama kamu. Btw lagi sibuk apa nih sekarang?"
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

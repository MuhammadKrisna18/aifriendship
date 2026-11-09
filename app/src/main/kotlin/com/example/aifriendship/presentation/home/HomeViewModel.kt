package com.example.aifriendship.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aifriendship.AiFriendshipApplication
import com.example.aifriendship.domain.model.CommunicationVibe
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(HomeTab.CHATS)
    private val _showProfileDialog = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        userRepository.observeUserProfile(),
        chatRepository.getLatestMessagesPerCompanion(),
        _selectedTab,
        _showProfileDialog,
        _searchQuery
    ) { profile, latestMessages, tab, showDialog, query ->
        HomeUiState(
            userProfile = profile,
            latestMessages = latestMessages,
            selectedTab = tab,
            showProfileDialog = showDialog,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onTabSelected(tab: HomeTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onOpenProfileDialog() {
        _showProfileDialog.value = true
    }

    fun onDismissProfileDialog() {
        _showProfileDialog.value = false
    }

    fun onSaveProfile(name: String, vibe: CommunicationVibe) {
        viewModelScope.launch {
            userRepository.updateName(name)
            userRepository.updateVibe(vibe)
            _showProfileDialog.value = false
        }
    }

    fun onSelectCompanion(companionId: String) {
        viewModelScope.launch {
            userRepository.setActiveCompanion(companionId)
        }
    }

    fun onDeleteCompanion(companionId: String) {
        viewModelScope.launch {
            userRepository.removeCompanion(companionId)
            chatRepository.clearMessagesByCompanion(companionId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AiFriendshipApplication)
                HomeViewModel(
                    userRepository = application.container.userRepository,
                    chatRepository = application.container.chatRepository
                )
            }
        }
    }
}

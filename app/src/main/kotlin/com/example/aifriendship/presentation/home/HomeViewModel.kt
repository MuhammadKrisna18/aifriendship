package com.example.aifriendship.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aifriendship.AiFriendshipApplication
import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.repository.UserRepository
import com.example.aifriendship.domain.service.AppUpdateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val appUpdateService: AppUpdateService
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(HomeTab.CHATS)
    private val _showProfileDialog = MutableStateFlow(false)
    private val _selectedCompanionForMemory = MutableStateFlow<AiCompanion?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    private val _isDownloadingUpdate = MutableStateFlow(false)
    private val _downloadProgress = MutableStateFlow(0)

    private val _homeState = combine(
        combine(
            userRepository.observeUserProfile(),
            chatRepository.getLatestMessagesPerCompanion(),
            _selectedTab
        ) { profile, latestMessages, tab ->
            Triple(profile, latestMessages, tab)
        },
        _showProfileDialog,
        _selectedCompanionForMemory,
        _searchQuery
    ) { (profile, latestMessages, tab), showDialog, memoryCompanion, query ->
        val updatedMemoryCompanion = memoryCompanion?.let { comp ->
            profile.allCompanions.find { it.id == comp.id } ?: comp
        }
        HomeUiState(
            userProfile = profile,
            latestMessages = latestMessages,
            selectedTab = tab,
            showProfileDialog = showDialog,
            selectedCompanionForMemory = updatedMemoryCompanion,
            searchQuery = query
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        _homeState,
        _updateInfo,
        _isDownloadingUpdate,
        _downloadProgress
    ) { baseState, update, isDownloading, progress ->
        baseState.copy(
            updateInfo = update,
            isDownloadingUpdate = isDownloading,
            downloadProgress = progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        checkForUpdates()
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            appUpdateService.checkForUpdate().onSuccess { info ->
                if (info.isUpdateAvailable) {
                    _updateInfo.value = info
                }
            }
        }
    }

    fun downloadAndInstallUpdate() {
        val info = _updateInfo.value ?: return
        viewModelScope.launch {
            _isDownloadingUpdate.value = true
            _downloadProgress.value = 0
            val result = appUpdateService.downloadAndInstallUpdate(
                downloadUrl = info.downloadUrl,
                fileName = "update.apk",
                onProgress = { progress ->
                    _downloadProgress.value = progress
                }
            )
            _isDownloadingUpdate.value = false
            if (result.isSuccess) {
                _updateInfo.value = null
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

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

    fun onSaveProfile(name: String) {
        viewModelScope.launch {
            userRepository.updateName(name)
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

    fun onOpenMemoryDialog(companion: AiCompanion) {
        _selectedCompanionForMemory.value = companion
    }

    fun onDismissMemoryDialog() {
        _selectedCompanionForMemory.value = null
    }

    fun addUserFact(companionId: String, fact: String) {
        val clean = fact.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            val companion = profile.allCompanions.find { it.id == companionId } ?: return@launch
            if (companion.memory.userFacts.none { it.equals(clean, ignoreCase = true) }) {
                val updatedFacts = (companion.memory.userFacts + clean).takeLast(30)
                val updatedCompanion = companion.copy(
                    memory = companion.memory.copy(userFacts = updatedFacts)
                )
                userRepository.updateCompanion(updatedCompanion)
            }
        }
    }

    fun deleteUserFact(companionId: String, fact: String) {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            val companion = profile.allCompanions.find { it.id == companionId } ?: return@launch
            val updatedFacts = companion.memory.userFacts.filterNot { it.equals(fact, ignoreCase = true) }
            val updatedCompanion = companion.copy(
                memory = companion.memory.copy(userFacts = updatedFacts)
            )
            userRepository.updateCompanion(updatedCompanion)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AiFriendshipApplication)
                HomeViewModel(
                    userRepository = application.container.userRepository,
                    chatRepository = application.container.chatRepository,
                    appUpdateService = application.container.appUpdateService
                )
            }
        }
    }
}

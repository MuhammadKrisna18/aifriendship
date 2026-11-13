package com.example.aifriendship.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aifriendship.AiFriendshipApplication
import com.example.aifriendship.data.config.ApiKeyProvider
import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.QuotedMessage
import com.example.aifriendship.domain.model.UserProfile
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.repository.UserRepository
import com.example.aifriendship.domain.service.AiService
import com.example.aifriendship.domain.service.AllModelsQuotaExceededException
import com.example.aifriendship.domain.service.AppUpdateService
import com.example.aifriendship.domain.service.CompanionScheduler
import com.example.aifriendship.domain.service.DailyQuotaExceededException
import com.example.aifriendship.domain.service.FarewellDetector
import com.example.aifriendship.domain.service.QuotaExceededException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val aiService: AiService,
    val apiKeyProvider: ApiKeyProvider,
    private val appUpdateService: AppUpdateService,
    private val companionScheduler: CompanionScheduler? = null
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    private val _isCheckingUpdate = MutableStateFlow(false)
    private val _onboardingStep = MutableStateFlow(OnboardingStep.COMPLETED)
    private val _isUserProfileDialogVisible = MutableStateFlow(false)
    private val _isMemoryDialogVisible = MutableStateFlow(false)
    private val _isDownloadingUpdate = MutableStateFlow(false)
    private val _downloadProgress = MutableStateFlow(0)
    private var cooldownJob: Job? = null

    private val _dialogState = combine(
        _isUserProfileDialogVisible,
        _isMemoryDialogVisible
    ) { profileDialog, memoryDialog ->
        Pair(profileDialog, memoryDialog)
    }

    val uiState: StateFlow<ChatUiState> = combine(
        combine(
            userRepository.observeUserProfile(),
            chatRepository.getMessages(),
            chatRepository.getLiaStatus(),
            _onboardingStep,
            _inputText
        ) { userProfile, allMessages, status, step, input ->
            val activeCompanion = userProfile.activeCompanion
            val activeId = activeCompanion.id
            val filteredMessages = allMessages.filter {
                it.companionId == activeId || (activeId == "default-companion-lia" && it.companionId.isBlank())
            }
            ChatUiState(
                messages = filteredMessages,
                liaStatus = status,
                userProfile = userProfile,
                onboardingStep = step,
                inputText = input,
                replyingTo = _replyingTo.value,
                isLoading = _isLoading.value,
                errorMessage = _errorMessage.value
            )
        },
        _dialogState,
        _updateInfo,
        _isCheckingUpdate,
        _isDownloadingUpdate
    ) { baseState, (profileDialog, memoryDialog), updateInfo, isChecking, isDownloading ->
        baseState.copy(
            isUserProfileDialogVisible = profileDialog,
            isMemoryDialogVisible = memoryDialog,
            updateInfo = updateInfo,
            isCheckingUpdate = isChecking,
            isDownloadingUpdate = isDownloading,
            downloadProgress = _downloadProgress.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    init {
        checkForUpdates(silent = true)
        initOnboarding()
        checkAndRecoverCooldown()
    }

    private fun checkAndRecoverCooldown() {
        viewModelScope.launch {
            val status = chatRepository.getLiaStatus().first()
            val now = System.currentTimeMillis()
            if (status.isCooldown && status.cooldownUntilTimestamp > now) {
                resumeRoleplayCooldown(status.cooldownUntilTimestamp)
            } else if (status.isCooldown) {
                chatRepository.updateLiaStatus(
                    status.copy(
                        subtitle = if (status.subtitle.contains("warung", ignoreCase = true)) "online" else status.subtitle,
                        isOnline = true,
                        isCooldown = false,
                        cooldownSecondsRemaining = 0,
                        cooldownUntilTimestamp = 0L
                    )
                )
            }
        }
    }

    private fun initOnboarding() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            if (!profile.isOnboardingCompleted) {
                val messages = chatRepository.getMessages().first()
                if (messages.isEmpty()) {
                    _onboardingStep.value = OnboardingStep.ASK_NAME
                    triggerOpeningGreeting()
                } else if (profile.name.isBlank()) {
                    _onboardingStep.value = OnboardingStep.ASK_NAME
                } else {
                    _onboardingStep.value = OnboardingStep.COMPLETED
                }
            } else {
                _onboardingStep.value = OnboardingStep.COMPLETED
            }
        }
    }

    private fun triggerOpeningGreeting() {
        viewModelScope.launch {
            delay(500L)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Lagi ngetik...",
                    isOnline = true,
                    isTyping = true
                )
            )
            delay(1200L)
            val openingMessage = ChatMessage(
                sender = MessageSender.AI,
                text = "Halo! Senang akhirnya kita bisa terhubung. Kenalin, aku Lia! Btw, aku enaknya manggil kamu siapa nih? Siapa nama panggilan kamu?",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(openingMessage)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Penasaran sama kamu",
                    isOnline = true,
                    isTyping = false
                )
            )
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onReplyMessage(message: ChatMessage?) {
        _replyingTo.value = message
    }

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            appUpdateService.checkForUpdate().fold(
                onSuccess = { info ->
                    _isCheckingUpdate.value = false
                    if (info.isUpdateAvailable) {
                        _updateInfo.value = info
                    } else if (!silent) {
                        val notice = ChatMessage(
                            sender = MessageSender.AI,
                            text = "Aplikasi kamu sudah versi terbaru (${info.currentVersion}) kok!",
                            status = MessageDeliveryStatus.SENT
                        )
                        chatRepository.saveMessage(notice)
                    }
                },
                onFailure = {
                    _isCheckingUpdate.value = false
                }
            )
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

    fun sendMessage() {
        val textToSend = _inputText.value.trim()
        if (textToSend.isBlank()) return

        val currentStep = _onboardingStep.value
        when (currentStep) {
            OnboardingStep.ASK_NAME -> {
                handleOnboardingNameInput(textToSend)
            }
            OnboardingStep.COMPLETED -> {
                sendNormalMessage(textToSend)
            }
        }
    }

    private fun handleOnboardingNameInput(name: String) {
        _inputText.value = ""
        viewModelScope.launch {
            val userMessage = ChatMessage(
                sender = MessageSender.USER,
                text = name,
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(userMessage)
            userRepository.updateName(name)
            userRepository.completeOnboarding()
            _onboardingStep.value = OnboardingStep.COMPLETED

            delay(500L)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Lagi ngetik...",
                    isOnline = true,
                    isTyping = true
                )
            )
            delay(1200L)
            val liaReply = ChatMessage(
                sender = MessageSender.AI,
                text = "Salam kenal ya, $name! Kamu jangan sungkan ya, kalau lagi senggang, pengen curhat, atau mau cerita apa aja, langsung chat aku di sini ya!",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(liaReply)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Online",
                    isOnline = true,
                    isTyping = false
                )
            )
        }
    }

    private fun sendNormalMessage(textToSend: String) {
        val profile = uiState.value.userProfile
        val companionId = profile.activeCompanion.id
        val quoted = _replyingTo.value?.let {
            QuotedMessage(
                senderName = if (it.sender == MessageSender.USER) "Gue" else profile.activeCompanion.name,
                text = it.text
            )
        }

        val userMessage = ChatMessage(
            companionId = companionId,
            sender = MessageSender.USER,
            text = textToSend,
            quotedMessage = quoted,
            status = MessageDeliveryStatus.SENT
        )

        _inputText.value = ""
        _replyingTo.value = null

        val detectedFarewell = FarewellDetector.detectFarewell(textToSend)

        viewModelScope.launch {
            chatRepository.saveMessage(userMessage)
            companionScheduler?.cancelFollowUp(companionId)

            val activeCompanion = profile.activeCompanion
            val updatedMemory = activeCompanion.memory.copy(
                consecutiveProactiveCount = 0,
                pendingFarewellActivity = detectedFarewell ?: activeCompanion.memory.pendingFarewellActivity
            )
            userRepository.updateCompanion(activeCompanion.copy(memory = updatedMemory))

            processAiReply(quoted)
        }
    }

    private suspend fun processAiReply(quoted: QuotedMessage?) {
        val currentStatus = chatRepository.getLiaStatus().first()
        val now = System.currentTimeMillis()
        val isStillCooldown = currentStatus.isCooldown && currentStatus.cooldownUntilTimestamp > now
        if (currentStatus.isCooldown && !isStillCooldown) {
            chatRepository.updateLiaStatus(
                currentStatus.copy(
                    subtitle = if (currentStatus.subtitle.contains("warung", ignoreCase = true)) "online" else currentStatus.subtitle,
                    isOnline = true,
                    isCooldown = false,
                    cooldownSecondsRemaining = 0,
                    cooldownUntilTimestamp = 0L
                )
            )
        }

        val profile = userRepository.getUserProfile()
        val companionId = profile.activeCompanion.id
        if (isStillCooldown) {
            val cooldownNotice = ChatMessage(
                companionId = companionId,
                sender = MessageSender.AI,
                text = "Sabar sebentar ya, aku masih di warung nih! Tunggu sebentar lagi yaa.",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(cooldownNotice)
            return
        }

        chatRepository.updateLiaStatus(currentStatus.copy(isTyping = true))

        val history = chatRepository.getMessagesByCompanion(companionId).first()
        val result = aiService.generateReply(history, quoted, profile)

        result.fold(
            onSuccess = { response ->
                chatRepository.updateLiaStatus(
                    currentStatus.copy(
                        subtitle = response.statusSubtitle,
                        isTyping = true
                    )
                )

                for (bubble in response.bubbles) {
                    val typingDelay = (bubble.length * 35L).coerceIn(600L, 1400L)
                    delay(typingDelay)

                    val aiMessage = ChatMessage(
                        companionId = companionId,
                        sender = MessageSender.AI,
                        text = bubble,
                        status = MessageDeliveryStatus.SENT
                    )
                    chatRepository.saveMessage(aiMessage)
                    delay(250L)
                }

                val activeCompanion = profile.activeCompanion
                val currentMemory = activeCompanion.memory
                val hasNewActivity = !response.activityUpdate.isNullOrBlank()
                val newMood = response.parsedMood
                val newRelationship = response.parsedRelationship
                val newFact = response.newFactLearned?.trim()?.takeIf {
                    it.isNotBlank() && !it.equals("null", ignoreCase = true)
                }

                val updatedFacts = if (newFact != null && currentMemory.userFacts.none { it.equals(newFact, ignoreCase = true) }) {
                    (currentMemory.userFacts + newFact).takeLast(30)
                } else {
                    currentMemory.userFacts
                }
                val hasFactsChanged = updatedFacts.size != currentMemory.userFacts.size

                if (hasNewActivity || newMood != null || newRelationship != null || hasFactsChanged) {
                    val updatedMemory = currentMemory.copy(
                        lastActivity = if (hasNewActivity) response.activityUpdate else currentMemory.lastActivity,
                        activityTimestamp = if (hasNewActivity) System.currentTimeMillis() else currentMemory.activityTimestamp,
                        currentMood = newMood ?: currentMemory.currentMood,
                        currentRelationship = newRelationship ?: currentMemory.currentRelationship,
                        userFacts = updatedFacts
                    )
                    val updatedCompanion = activeCompanion.copy(memory = updatedMemory)
                    userRepository.updateCompanion(updatedCompanion)
                }

                chatRepository.updateLiaStatus(
                    currentStatus.copy(
                        subtitle = response.statusSubtitle,
                        isTyping = false
                    )
                )

                val latestProfile = userRepository.getUserProfile()
                val latestCompanion = latestProfile.activeCompanion
                val pendingFarewell = latestCompanion.memory.pendingFarewellActivity
                val delayMinutes = if (pendingFarewell != null) {
                    FarewellDetector.suggestDelayMinutes(pendingFarewell)
                } else {
                    FarewellDetector.suggestRapidDelayMinutes()
                }
                companionScheduler?.scheduleFollowUp(companionId, delayMinutes)
            },
            onFailure = { error ->
                chatRepository.updateLiaStatus(currentStatus.copy(isTyping = false))
                handleAiError(error)
            }
        )
    }

    private suspend fun handleAiError(error: Throwable) {
        when (error) {
            is DailyQuotaExceededException -> {
                delay(600L)
                val errorMessage = ChatMessage(
                    sender = MessageSender.AI,
                    text = "Duh mataku udah ngantuk banget, kuota energi ngobrol aku buat hari ini udah habis hehe. Kita lanjut besok yaa! Atau kalau kamu mau lanjut sekarang, kamu bisa pasang API Key baru di Pengaturan (⚙️).",
                    status = MessageDeliveryStatus.SENT
                )
                chatRepository.saveMessage(errorMessage)
                val current = chatRepository.getLiaStatus().first()
                chatRepository.updateLiaStatus(
                    current.copy(
                        subtitle = "istirahat dulu ya (kuota harian habis)",
                        isOnline = false,
                        isTyping = false,
                        isCooldown = false,
                        cooldownSecondsRemaining = 0,
                        cooldownUntilTimestamp = 0L
                    )
                )
            }
            is AllModelsQuotaExceededException, is QuotaExceededException -> {
                triggerRoleplayCooldown()
            }
            is IllegalStateException -> {
                delay(600L)
                val errorMessage = ChatMessage(
                    sender = MessageSender.AI,
                    text = "Duh ada kendala teknis pada koneksi AI aku nih, coba kirim lagi chatnya sebentar ya.",
                    status = MessageDeliveryStatus.SENT
                )
                chatRepository.saveMessage(errorMessage)
            }
            else -> {
                delay(600L)
                val errorMessage = ChatMessage(
                    sender = MessageSender.AI,
                    text = "Duh sinyal internetnya lagi kurang stabil sepertinya, coba kirim lagi chat kamu barusan ya.",
                    status = MessageDeliveryStatus.SENT
                )
                chatRepository.saveMessage(errorMessage)
            }
        }
    }

    private fun triggerRoleplayCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            val excuseMessage = ChatMessage(
                sender = MessageSender.AI,
                text = "Eh bentar yaa, ibuku minta tolong beli garem dulu ke warung... 1 menit lagi aku balik!",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(excuseMessage)

            val cooldownUntil = System.currentTimeMillis() + 60_000L
            runCooldownLoop(cooldownUntil)
        }
    }

    private fun resumeRoleplayCooldown(cooldownUntil: Long) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            runCooldownLoop(cooldownUntil)
        }
    }

    private suspend fun runCooldownLoop(cooldownUntil: Long) {
        var remaining = ((cooldownUntil - System.currentTimeMillis()) / 1000L).toInt().coerceIn(1, 60)
        while (remaining > 0) {
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Lagi ke warung bentar... (${remaining}s)",
                    isOnline = false,
                    isTyping = false,
                    isCooldown = true,
                    cooldownSecondsRemaining = remaining,
                    cooldownUntilTimestamp = cooldownUntil
                )
            )
            delay(1000L)
            remaining--
        }

        chatRepository.updateLiaStatus(
            LiaStatus(
                subtitle = "online",
                isOnline = true,
                isTyping = false,
                isCooldown = false,
                cooldownSecondsRemaining = 0,
                cooldownUntilTimestamp = 0L
            )
        )

        val backMessage = ChatMessage(
            sender = MessageSender.AI,
            text = "Udah balik nih dari warung, tadi kamu nanya apa?",
            status = MessageDeliveryStatus.SENT
        )
        chatRepository.saveMessage(backMessage)
    }

    fun clearChat() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            chatRepository.clearMessagesByCompanion(profile.activeCompanion.id)
        }
    }

    fun resetChat() {
        viewModelScope.launch {
            chatRepository.clearMessages()
            userRepository.saveUserProfile(
                UserProfile(
                    name = "",
                    isOnboardingCompleted = false
                )
            )
            _onboardingStep.value = OnboardingStep.ASK_NAME
            triggerOpeningGreeting()
        }
    }

    fun showUserProfileDialog() {
        _isUserProfileDialogVisible.value = true
    }

    fun hideUserProfileDialog() {
        _isUserProfileDialogVisible.value = false
    }

    fun updateUserProfile(name: String) {
        viewModelScope.launch {
            userRepository.updateName(name.trim())
            _isUserProfileDialogVisible.value = false
        }
    }

    fun showMemoryDialog() {
        _isMemoryDialogVisible.value = true
    }

    fun hideMemoryDialog() {
        _isMemoryDialogVisible.value = false
    }

    fun addUserFact(fact: String) {
        val clean = fact.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            val companion = profile.activeCompanion
            if (companion.memory.userFacts.none { it.equals(clean, ignoreCase = true) }) {
                val updatedFacts = (companion.memory.userFacts + clean).takeLast(30)
                val updatedCompanion = companion.copy(
                    memory = companion.memory.copy(userFacts = updatedFacts)
                )
                userRepository.updateCompanion(updatedCompanion)
            }
        }
    }

    fun deleteUserFact(fact: String) {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            val companion = profile.activeCompanion
            val updatedFacts = companion.memory.userFacts.filterNot { it.equals(fact, ignoreCase = true) }
            val updatedCompanion = companion.copy(
                memory = companion.memory.copy(userFacts = updatedFacts)
            )
            userRepository.updateCompanion(updatedCompanion)
        }
    }

    fun updateApiKey(apiKey: String) {
        apiKeyProvider.setApiKey(apiKey)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AiFriendshipApplication)
                ChatViewModel(
                    chatRepository = application.container.chatRepository,
                    userRepository = application.container.userRepository,
                    aiService = application.container.aiService,
                    apiKeyProvider = application.container.apiKeyProvider,
                    appUpdateService = application.container.appUpdateService,
                    companionScheduler = application.container.companionScheduler
                )
            }
        }
    }
}

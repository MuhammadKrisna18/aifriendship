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
import com.example.aifriendship.domain.model.CommunicationVibe
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
    private val appUpdateService: AppUpdateService
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    private val _isCheckingUpdate = MutableStateFlow(false)
    private val _onboardingStep = MutableStateFlow(OnboardingStep.COMPLETED)
    private val _isUserProfileDialogVisible = MutableStateFlow(false)
    private var cooldownJob: Job? = null

    val uiState: StateFlow<ChatUiState> = combine(
        chatRepository.getMessages(),
        chatRepository.getLiaStatus(),
        userRepository.observeUserProfile(),
        _onboardingStep,
        _inputText
    ) { messages, status, userProfile, step, input ->
        ChatUiState(
            messages = messages,
            liaStatus = status,
            userProfile = userProfile,
            onboardingStep = step,
            inputText = input,
            replyingTo = _replyingTo.value,
            isLoading = _isLoading.value,
            errorMessage = _errorMessage.value,
            updateInfo = _updateInfo.value,
            isCheckingUpdate = _isCheckingUpdate.value,
            isUserProfileDialogVisible = _isUserProfileDialogVisible.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    init {
        checkForUpdates(silent = true)
        initOnboarding()
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
                    _onboardingStep.value = OnboardingStep.ASK_VIBE
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
                text = "Woyy! Akhirnya online juga lu haha. Kenalin, gue Lia! Btw gue harus manggil lu siapa nih? Siapa nama panggilan lu?",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(openingMessage)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Penasaran sama lu",
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
        viewModelScope.launch(Dispatchers.IO) {
            appUpdateService.downloadAndInstallUpdate(info.downloadUrl, "update.apk")
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
            OnboardingStep.ASK_VIBE -> {
                val matchedVibe = when {
                    textToSend.contains("kalem", ignoreCase = true) || textToSend.contains("chill", ignoreCase = true) -> CommunicationVibe.CHILL
                    textToSend.contains("absurd", ignoreCase = true) || textToSend.contains("acak", ignoreCase = true) || textToSend.contains("campur", ignoreCase = true) -> CommunicationVibe.ABSURD
                    else -> CommunicationVibe.BARBAR
                }
                selectVibe(matchedVibe)
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
            _onboardingStep.value = OnboardingStep.ASK_VIBE

            delay(500L)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Menyesuaikan frekuensi...",
                    isOnline = true,
                    isTyping = true
                )
            )
            delay(1200L)
            val liaReply = ChatMessage(
                sender = MessageSender.AI,
                text = "Oalah $name! Siap sob $name. Btw gaya ngobrol yang lu suka gimana nih? Biar gue bisa nyesuaiin frekuensi kita haha.",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(liaReply)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Nungguin pilihan lu",
                    isOnline = true,
                    isTyping = false
                )
            )
        }
    }

    fun selectVibe(vibe: CommunicationVibe) {
        _inputText.value = ""
        viewModelScope.launch {
            val userMessage = ChatMessage(
                sender = MessageSender.USER,
                text = vibe.label,
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(userMessage)
            userRepository.updateVibe(vibe)
            userRepository.completeOnboarding()
            _onboardingStep.value = OnboardingStep.COMPLETED

            delay(500L)
            chatRepository.updateLiaStatus(
                LiaStatus(
                    subtitle = "Sahabat lu sekarang",
                    isOnline = true,
                    isTyping = true
                )
            )
            delay(1200L)
            val liaReply = ChatMessage(
                sender = MessageSender.AI,
                text = "Sip mantep, udah gue catet di memori gue! Lu jangan sungkan ya, kalau lagi gabut, pengen curhat, atau mau sharing apa aja langsung senggol gue di sini!",
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
        val quoted = _replyingTo.value?.let {
            QuotedMessage(
                senderName = if (it.sender == MessageSender.USER) "Gue" else "Lia",
                text = it.text
            )
        }

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = textToSend,
            quotedMessage = quoted,
            status = MessageDeliveryStatus.SENT
        )

        _inputText.value = ""
        _replyingTo.value = null

        viewModelScope.launch {
            chatRepository.saveMessage(userMessage)
            processAiReply(quoted)
        }
    }

    private suspend fun processAiReply(quoted: QuotedMessage?) {
        val currentStatus = chatRepository.getLiaStatus().first()
        if (currentStatus.isCooldown) {
            val cooldownNotice = ChatMessage(
                sender = MessageSender.AI,
                text = "Sabar napa, gue masih di warung nih! Tunggu bentar lagi ya.",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(cooldownNotice)
            return
        }

        chatRepository.updateLiaStatus(currentStatus.copy(isTyping = true))

        val history = chatRepository.getMessages().first()
        val profile = userRepository.getUserProfile()
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
                        sender = MessageSender.AI,
                        text = bubble,
                        status = MessageDeliveryStatus.SENT
                    )
                    chatRepository.saveMessage(aiMessage)
                    delay(250L)
                }

                chatRepository.updateLiaStatus(
                    currentStatus.copy(
                        subtitle = response.statusSubtitle,
                        isTyping = false
                    )
                )
            },
            onFailure = { error ->
                chatRepository.updateLiaStatus(currentStatus.copy(isTyping = false))
                handleAiError(error)
            }
        )
    }

    private suspend fun handleAiError(error: Throwable) {
        when (error) {
            is AllModelsQuotaExceededException, is QuotaExceededException -> {
                triggerRoleplayCooldown()
            }
            is IllegalStateException -> {
                delay(600L)
                val errorMessage = ChatMessage(
                    sender = MessageSender.AI,
                    text = "Duh ada kendala teknis pada koneksi AI gue nih, coba kirim lagi chatnya sebentar.",
                    status = MessageDeliveryStatus.SENT
                )
                chatRepository.saveMessage(errorMessage)
            }
            else -> {
                delay(600L)
                val errorMessage = ChatMessage(
                    sender = MessageSender.AI,
                    text = "Duh sinyal lu lagi bapuk kali ya, coba kirim lagi chatnya barusan.",
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
                text = "Eh bentar ya woy, emak gue nyuruh beli garem dulu ke warung... 1 menit lagi gue balik!",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(excuseMessage)

            var remaining = 60
            while (remaining > 0) {
                chatRepository.updateLiaStatus(
                    LiaStatus(
                        subtitle = "Lagi ke warung bentar... (${remaining}s)",
                        isOnline = false,
                        isTyping = false,
                        isCooldown = true,
                        cooldownSecondsRemaining = remaining
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
                    cooldownSecondsRemaining = 0
                )
            )

            val backMessage = ChatMessage(
                sender = MessageSender.AI,
                text = "Udah balik nih dari warung, tadi lu nanya apa?",
                status = MessageDeliveryStatus.SENT
            )
            chatRepository.saveMessage(backMessage)
        }
    }

    fun clearChat() {
        resetChat()
    }

    fun resetChat() {
        viewModelScope.launch {
            chatRepository.clearMessages()
            userRepository.saveUserProfile(
                UserProfile(
                    name = "",
                    vibe = CommunicationVibe.BARBAR,
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

    fun updateUserProfile(name: String, vibe: CommunicationVibe) {
        viewModelScope.launch {
            val current = userRepository.getUserProfile()
            userRepository.saveUserProfile(
                current.copy(
                    name = name.trim(),
                    vibe = vibe,
                    isOnboardingCompleted = true
                )
            )
            _isUserProfileDialogVisible.value = false
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
                    appUpdateService = application.container.appUpdateService
                )
            }
        }
    }
}

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
import com.example.aifriendship.domain.repository.ChatRepository
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
    private var cooldownJob: Job? = null

    val uiState: StateFlow<ChatUiState> = combine(
        chatRepository.getMessages(),
        chatRepository.getLiaStatus(),
        _inputText,
        _replyingTo,
        _isLoading
    ) { messages, status, input, replying, loading ->
        ChatUiState(
            messages = messages,
            liaStatus = status,
            inputText = input,
            replyingTo = replying,
            isLoading = loading,
            errorMessage = _errorMessage.value,
            updateInfo = _updateInfo.value,
            isCheckingUpdate = _isCheckingUpdate.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    init {
        checkForUpdates(silent = true)
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
        val result = aiService.generateReply(history, quoted)

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
        viewModelScope.launch {
            chatRepository.clearMessages()
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
                    aiService = application.container.aiService,
                    apiKeyProvider = application.container.apiKeyProvider,
                    appUpdateService = application.container.appUpdateService
                )
            }
        }
    }
}

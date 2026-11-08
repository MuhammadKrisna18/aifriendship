package com.example.aifriendship.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aifriendship.AiFriendshipApplication
import com.example.aifriendship.data.config.ApiKeyProvider
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.QuotedMessage
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.service.AiService
import com.example.aifriendship.domain.service.QuotaExceededException
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
    val apiKeyProvider: ApiKeyProvider
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

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
            errorMessage = _errorMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onReplyMessage(message: ChatMessage?) {
        _replyingTo.value = message
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
        val fallbackMessageText = when (error) {
            is QuotaExceededException -> {
                "Eh bentar ya, emak manggil nih disuruh beli garem ke warung, 1 menit lagi gue bales!"
            }
            is IllegalStateException -> {
                "Eh lu belum pasang Gemini API Key di tombol setelan pojok kanan atas tuh, pasang dulu yaa biar kita bisa ngobrol!"
            }
            else -> {
                "Duh sinyal lu lagi bapuk kali ya, coba kirim lagi chatnya barusan."
            }
        }

        delay(600L)
        val errorMessage = ChatMessage(
            sender = MessageSender.AI,
            text = fallbackMessageText,
            status = MessageDeliveryStatus.SENT
        )
        chatRepository.saveMessage(errorMessage)
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
                    apiKeyProvider = application.container.apiKeyProvider
                )
            }
        }
    }
}

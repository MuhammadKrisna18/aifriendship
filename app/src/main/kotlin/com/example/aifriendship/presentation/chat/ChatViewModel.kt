package com.example.aifriendship.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aifriendship.AiFriendshipApplication
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.QuotedMessage
import com.example.aifriendship.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    private val _replyingTo = MutableStateFlow<ChatMessage?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChatUiState> = combine(
        chatRepository.getMessages(),
        chatRepository.getKaleaStatus(),
        _inputText,
        _replyingTo,
        _isLoading
    ) { messages, status, input, replying, loading ->
        ChatUiState(
            messages = messages,
            kaleaStatus = status,
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
                senderName = if (it.sender == MessageSender.USER) "Gue" else "Kalea",
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
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearMessages()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AiFriendshipApplication)
                ChatViewModel(application.container.chatRepository)
            }
        }
    }
}


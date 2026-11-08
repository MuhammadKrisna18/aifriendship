package com.example.aifriendship.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.presentation.chat.components.AiChatBubble
import com.example.aifriendship.presentation.chat.components.ChatBottomBar
import com.example.aifriendship.presentation.chat.components.ChatTopBar
import com.example.aifriendship.presentation.chat.components.TypingIndicator
import com.example.aifriendship.presentation.chat.components.UserChatBubble
import com.example.aifriendship.ui.theme.ChatCanvasBackground
import com.example.aifriendship.ui.theme.ChatTimestampColor

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size, uiState.kaleaStatus.isTyping) {
        val totalItems = uiState.messages.size + if (uiState.kaleaStatus.isTyping) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                status = uiState.kaleaStatus,
                onBackClick = onBackClick,
                onClearChat = { viewModel.clearChat() }
            )
        },
        bottomBar = {
            ChatBottomBar(
                inputText = uiState.inputText,
                onInputChanged = { viewModel.onInputTextChanged(it) },
                onSendMessage = { viewModel.sendMessage() },
                replyingTo = uiState.replyingTo,
                onDismissReply = { viewModel.onReplyMessage(null) }
            )
        },
        containerColor = ChatCanvasBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ChatCanvasBackground)
        ) {
            if (uiState.messages.isEmpty() && !uiState.kaleaStatus.isTyping) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mulai obrolan santai dengan Kalea...",
                        color = ChatTimestampColor,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        if (message.sender == MessageSender.USER) {
                            UserChatBubble(
                                message = message,
                                onLongClick = { viewModel.onReplyMessage(message) }
                            )
                        } else {
                            AiChatBubble(
                                message = message,
                                onLongClick = { viewModel.onReplyMessage(message) }
                            )
                        }
                    }

                    if (uiState.kaleaStatus.isTyping) {
                        item(key = "typing_indicator") {
                            TypingIndicator()
                        }
                    }
                }
            }
        }
    }
}


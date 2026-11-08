package com.example.aifriendship.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.aifriendship.ui.theme.UserBubblePink

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }

    LaunchedEffect(showApiKeyDialog) {
        if (showApiKeyDialog) {
            apiKeyInput = viewModel.apiKeyProvider.getApiKey()
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.liaStatus.isTyping) {
        val totalItems = uiState.messages.size + if (uiState.liaStatus.isTyping) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    uiState.updateInfo?.let { update ->
        if (update.isUpdateAvailable) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text("Update Baru Tersedia! 🚀") },
                text = {
                    Column {
                        Text(
                            text = "Versi terbaru (${update.latestVersion}) sudah siap diunduh.",
                            fontSize = 14.sp
                        )
                        if (update.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Catatan Pembaruan:\n${update.releaseNotes}",
                                fontSize = 12.sp,
                                color = ChatTimestampColor
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.downloadAndInstallUpdate()
                            viewModel.dismissUpdateDialog()
                        }
                    ) {
                        Text("Pasang Sekarang", color = UserBubblePink)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Nanti Saja")
                    }
                }
            )
        }
    }

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Setelan Gemini API Key") },
            text = {
                Column {
                    Text(
                        text = "Masukkan Google AI Studio (Gemini) API Key kamu untuk mengobrol dengan Lia.",
                        fontSize = 13.sp,
                        color = ChatTimestampColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateApiKey(apiKeyInput)
                        showApiKeyDialog = false
                    }
                ) {
                    Text("Simpan", color = UserBubblePink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                status = uiState.liaStatus,
                onBackClick = onBackClick,
                onClearChat = { viewModel.clearChat() },
                onSettingsClick = { showApiKeyDialog = true },
                onCheckUpdateClick = { viewModel.checkForUpdates(silent = false) }
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
            if (uiState.messages.isEmpty() && !uiState.liaStatus.isTyping) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mulai obrolan santai dengan Lia...",
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

                    if (uiState.liaStatus.isTyping) {
                        item(key = "typing_indicator") {
                            TypingIndicator()
                        }
                    }
                }
            }
        }
    }
}

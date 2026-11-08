package com.example.aifriendship.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.aifriendship.presentation.chat.components.QuickVibeChips
import com.example.aifriendship.presentation.chat.components.TypingIndicator
import com.example.aifriendship.presentation.chat.components.UserChatBubble
import com.example.aifriendship.presentation.chat.components.UserProfileDialog
import com.example.aifriendship.ui.theme.ChatCanvasBackground
import com.example.aifriendship.ui.theme.ChatTimestampColor
import com.example.aifriendship.ui.theme.UserBubbleColor

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
    onBackClick: () -> Unit = {},
    onChangeCompanionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val companionName = uiState.userProfile.companion.name.ifBlank { "Lia" }
    val avatarColorHex = uiState.userProfile.companion.avatarColorHex

    LaunchedEffect(uiState.messages.size, uiState.liaStatus.isTyping) {
        val totalItems = uiState.messages.size + if (uiState.liaStatus.isTyping) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    if (uiState.isUserProfileDialogVisible) {
        UserProfileDialog(
            currentProfile = uiState.userProfile,
            onDismiss = { viewModel.hideUserProfileDialog() },
            onSave = { name, vibe -> viewModel.updateUserProfile(name, vibe) }
        )
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
                        Text("Pasang Sekarang", color = UserBubbleColor)
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

    Scaffold(
        topBar = {
            ChatTopBar(
                status = uiState.liaStatus,
                companionName = companionName,
                avatarColorHex = avatarColorHex,
                onBackClick = onBackClick,
                onClearChat = { viewModel.resetChat() },
                onCheckUpdateClick = { viewModel.checkForUpdates(silent = false) },
                onProfileClick = { viewModel.showUserProfileDialog() },
                onChangeCompanionClick = onChangeCompanionClick
            )
        },
        bottomBar = {
            Column {
                QuickVibeChips(
                    visible = (uiState.onboardingStep == OnboardingStep.ASK_VIBE),
                    onVibeSelected = { viewModel.selectVibe(it) }
                )
                ChatBottomBar(
                    inputText = uiState.inputText,
                    onInputChanged = { viewModel.onInputTextChanged(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    placeholder = if (uiState.onboardingStep == OnboardingStep.ASK_NAME) "Ketik nama panggilanmu..." else "Ketik pesan ke $companionName...",
                    replyingTo = uiState.replyingTo,
                    onDismissReply = { viewModel.onReplyMessage(null) }
                )
            }
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
                        text = "Mulai obrolan santai dengan $companionName...",
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
                                companionName = companionName,
                                avatarColorHex = avatarColorHex,
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

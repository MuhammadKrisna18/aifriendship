package com.example.aifriendship.presentation.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.aifriendship.presentation.chat.components.UserProfileDialog
import com.example.aifriendship.presentation.components.CompanionMemoryDialog
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
    val context = LocalContext.current
    var showClearChatDialog by remember { mutableStateOf(false) }

    val companion = uiState.userProfile.activeCompanion
    val companionName = companion.name.ifBlank { "Lia" }
    val avatarColorHex = companion.avatarColorHex

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
            onSave = { name -> viewModel.updateUserProfile(name) }
        )
    }

    if (uiState.isMemoryDialogVisible) {
        CompanionMemoryDialog(
            companion = companion,
            userName = uiState.userProfile.name,
            onDismiss = { viewModel.hideMemoryDialog() },
            onDeleteFact = { fact -> viewModel.deleteUserFact(fact) },
            onAddFact = { fact -> viewModel.addUserFact(fact) }
        )
    }

    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = { Text("Reset Obrolan?") },
            text = { Text("Semua riwayat obrolan dengan $companionName akan dihapus. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearChatDialog = false
                        viewModel.clearChat()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus Riwayat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    uiState.updateInfo?.let { update ->
        if (update.isUpdateAvailable) {
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.isDownloadingUpdate) {
                        viewModel.dismissUpdateDialog()
                    }
                },
                title = { Text("Update Baru Tersedia! 🚀") },
                text = {
                    Column {
                        Text(
                            text = "Versi terbaru (${update.latestVersion}) sudah siap dipasang.",
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
                        if (uiState.isDownloadingUpdate) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { uiState.downloadProgress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Mengunduh pembaruan (${uiState.downloadProgress}%)... Harap tunggu sebentar.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.downloadAndInstallUpdate() },
                        enabled = !uiState.isDownloadingUpdate
                    ) {
                        Text(
                            if (uiState.isDownloadingUpdate) "Mengunduh..." else "Install versi terbaru"
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissUpdateDialog() },
                        enabled = !uiState.isDownloadingUpdate
                    ) {
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
                relationship = companion.currentRelationship,
                mood = companion.currentMood,
                onBackClick = onBackClick,
                onClearChat = { showClearChatDialog = true },
                onCheckUpdateClick = { viewModel.checkForUpdates(silent = false) },
                onProfileClick = { viewModel.showUserProfileDialog() },
                onChangeCompanionClick = onChangeCompanionClick,
                onMemoryClick = { viewModel.showMemoryDialog() }
            )
        },
        bottomBar = {
            ChatBottomBar(
                inputText = uiState.inputText,
                onInputChanged = { viewModel.onInputTextChanged(it) },
                onSendMessage = { viewModel.sendMessage() },
                companionName = companionName,
                placeholder = if (uiState.onboardingStep == OnboardingStep.ASK_NAME) "Ketik nama panggilanmu..." else "Ketik pesan ke $companionName...",
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
                            TypingIndicator(
                                companionName = companionName,
                                avatarColorHex = avatarColorHex
                            )
                        }
                    }
                }
            }
        }
    }
}

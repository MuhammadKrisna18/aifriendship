package com.example.aifriendship.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aifriendship.domain.model.AppUpdateInfo

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo?,
    isDownloading: Boolean,
    downloadProgress: Int,
    onDownloadAndInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    if (updateInfo == null || !updateInfo.isUpdateAvailable) return

    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) {
                onDismiss()
            }
        },
        title = { Text("Update Baru Tersedia! 🚀") },
        text = {
            Column {
                Text(
                    text = "Versi terbaru (${updateInfo.latestVersion}) sudah siap dipasang.",
                    fontSize = 14.sp
                )
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Catatan Pembaruan:\n${updateInfo.releaseNotes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isDownloading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mengunduh pembaruan ($downloadProgress%)... Harap tunggu sebentar.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDownloadAndInstall,
                enabled = !isDownloading
            ) {
                Text(
                    if (isDownloading) "Mengunduh..." else "Install versi terbaru"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDownloading
            ) {
                Text("Nanti Saja")
            }
        }
    )
}


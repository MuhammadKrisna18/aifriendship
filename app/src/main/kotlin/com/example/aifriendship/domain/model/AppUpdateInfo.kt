package com.example.aifriendship.domain.model

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean = false,
    val currentVersion: String = "",
    val latestVersion: String = "",
    val downloadUrl: String = "",
    val releaseNotes: String = ""
)


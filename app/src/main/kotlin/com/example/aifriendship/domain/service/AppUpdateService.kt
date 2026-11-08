package com.example.aifriendship.domain.service

import com.example.aifriendship.domain.model.AppUpdateInfo

interface AppUpdateService {
    suspend fun checkForUpdate(): Result<AppUpdateInfo>
    fun downloadAndInstallUpdate(downloadUrl: String, fileName: String): Result<Unit>
}


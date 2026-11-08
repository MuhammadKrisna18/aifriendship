package com.example.aifriendship.data.remote.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.aifriendship.BuildConfig
import com.example.aifriendship.data.remote.update.dto.GitHubReleaseDto
import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.service.AppUpdateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GitHubUpdateService(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AppUpdateService {

    private val releasesApiUrl = "https://api.github.com/repos/MuhammadKrisna18/aifriendship/releases/latest"

    override suspend fun checkForUpdate(): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(releasesApiUrl)
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("HTTP ${response.code}"))
                }

                val body = response.body?.string().orEmpty()
                val release = json.decodeFromString<GitHubReleaseDto>(body)

                val latestTag = release.tagName.trim()
                val currentVersion = BuildConfig.VERSION_NAME

                val isNewer = isVersionNewer(latestTag, currentVersion)
                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                val downloadUrl = apkAsset?.browserDownloadUrl.orEmpty()

                Result.success(
                    AppUpdateInfo(
                        isUpdateAvailable = isNewer && downloadUrl.isNotBlank(),
                        currentVersion = currentVersion,
                        latestVersion = latestTag,
                        downloadUrl = downloadUrl,
                        releaseNotes = release.body.orEmpty()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun downloadAndInstallUpdate(downloadUrl: String, fileName: String): Result<Unit> {
        return try {
            val request = Request.Builder().url(downloadUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return Result.failure(IOException("Failed to download APK: HTTP ${response.code}"))
            }

            val apkFile = File(context.cacheDir, "update.apk")
            if (apkFile.exists()) apkFile.delete()

            response.body?.byteStream()?.use { input ->
                FileOutputStream(apkFile).use { output ->
                    input.copyTo(output)
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val cleanLatest = latest.removePrefix("v").removePrefix("V").trim()
        val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()

        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}


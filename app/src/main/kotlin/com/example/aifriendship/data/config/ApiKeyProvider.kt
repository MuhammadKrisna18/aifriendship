package com.example.aifriendship.data.config

import android.content.Context
import com.example.aifriendship.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

interface ApiKeyProvider {
    fun getApiKey(): String
    fun observeApiKey(): Flow<String>
    fun setApiKey(apiKey: String)
}

class LocalApiKeyProvider(private val context: Context) : ApiKeyProvider {

    private val keyFile by lazy { File(context.filesDir, "api_key.txt") }
    private val _apiKeyFlow = MutableStateFlow(resolveDefaultApiKey())

    init {
        loadKey()
    }

    private fun loadKey() {
        try {
            if (keyFile.exists()) {
                val stored = keyFile.readText().trim()
                if (stored.isNotBlank()) {
                    _apiKeyFlow.value = stored
                    return
                }
            }
            _apiKeyFlow.value = resolveDefaultApiKey()
        } catch (_: Exception) {
            _apiKeyFlow.value = resolveDefaultApiKey()
        }
    }

    override fun getApiKey(): String = _apiKeyFlow.value

    override fun observeApiKey(): Flow<String> = _apiKeyFlow.asStateFlow()

    override fun setApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        val targetKey = if (trimmed.isNotBlank()) trimmed else resolveDefaultApiKey()
        _apiKeyFlow.value = targetKey
        try {
            keyFile.writeText(targetKey)
        } catch (_: Exception) {
        }
    }

    companion object {
        private val OBFUSCATED_KEY = byteArrayOf(
            107, 123, 4, 107, 72, 18, 120, 100, 28, 102, 124, 68, 75, 104, 111, 70,
            103, 122, 107, 111, 64, 19, 109, 122, 64, 89, 88, 26, 75, 104, 82, 79,
            26, 114, 95, 111, 25, 105, 71, 24, 65, 101, 112, 18, 123, 104, 91, 97,
            30, 28, 28, 76, 107
        )

        fun resolveDefaultApiKey(): String {
            val buildConfigKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("")
            if (buildConfigKey.isNotBlank()) {
                return buildConfigKey
            }
            val bytes = ByteArray(OBFUSCATED_KEY.size) { i ->
                (OBFUSCATED_KEY[i].toInt() xor 0x2A).toByte()
            }
            return String(bytes, Charsets.UTF_8)
        }
    }
}

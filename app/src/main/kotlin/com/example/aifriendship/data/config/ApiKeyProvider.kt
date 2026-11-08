package com.example.aifriendship.data.config

import android.content.Context
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
    private val _apiKeyFlow = MutableStateFlow("")

    init {
        loadKey()
    }

    private fun loadKey() {
        try {
            if (keyFile.exists()) {
                _apiKeyFlow.value = keyFile.readText().trim()
            }
        } catch (_: Exception) {
            _apiKeyFlow.value = ""
        }
    }

    override fun getApiKey(): String = _apiKeyFlow.value

    override fun observeApiKey(): Flow<String> = _apiKeyFlow.asStateFlow()

    override fun setApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        _apiKeyFlow.value = trimmed
        try {
            keyFile.writeText(trimmed)
        } catch (_: Exception) {
        }
    }
}


package com.example.aifriendship.di

import android.content.Context
import com.example.aifriendship.data.config.ApiKeyProvider
import com.example.aifriendship.data.config.LocalApiKeyProvider
import com.example.aifriendship.data.local.ChatLocalDataSource
import com.example.aifriendship.data.local.JsonChatLocalDataSource
import com.example.aifriendship.data.remote.gemini.GeminiAiService
import com.example.aifriendship.data.repository.DefaultChatRepository
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.service.AiService

interface AppContainer {
    val chatRepository: ChatRepository
    val apiKeyProvider: ApiKeyProvider
    val aiService: AiService
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val apiKeyProvider: ApiKeyProvider by lazy {
        LocalApiKeyProvider(context)
    }

    private val localDataSource: ChatLocalDataSource by lazy {
        JsonChatLocalDataSource(context)
    }

    override val chatRepository: ChatRepository by lazy {
        DefaultChatRepository(localDataSource)
    }

    override val aiService: AiService by lazy {
        GeminiAiService(apiKeyProvider)
    }
}

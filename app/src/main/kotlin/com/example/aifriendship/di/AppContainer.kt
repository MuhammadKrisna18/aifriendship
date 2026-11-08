package com.example.aifriendship.di

import android.content.Context
import com.example.aifriendship.data.config.ApiKeyProvider
import com.example.aifriendship.data.config.LocalApiKeyProvider
import com.example.aifriendship.data.local.ChatLocalDataSource
import com.example.aifriendship.data.local.JsonChatLocalDataSource
import com.example.aifriendship.data.local.JsonUserLocalDataSource
import com.example.aifriendship.data.local.UserLocalDataSource
import com.example.aifriendship.data.remote.gemini.GeminiAiService
import com.example.aifriendship.data.remote.update.GitHubUpdateService
import com.example.aifriendship.data.repository.DefaultChatRepository
import com.example.aifriendship.data.repository.DefaultUserRepository
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.repository.UserRepository
import com.example.aifriendship.domain.service.AiService
import com.example.aifriendship.domain.service.AppUpdateService

interface AppContainer {
    val chatRepository: ChatRepository
    val userRepository: UserRepository
    val apiKeyProvider: ApiKeyProvider
    val aiService: AiService
    val appUpdateService: AppUpdateService
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val apiKeyProvider: ApiKeyProvider by lazy {
        LocalApiKeyProvider(context)
    }

    private val localDataSource: ChatLocalDataSource by lazy {
        JsonChatLocalDataSource(context)
    }

    private val userLocalDataSource: UserLocalDataSource by lazy {
        JsonUserLocalDataSource(context)
    }

    override val chatRepository: ChatRepository by lazy {
        DefaultChatRepository(localDataSource)
    }

    override val userRepository: UserRepository by lazy {
        DefaultUserRepository(userLocalDataSource)
    }

    override val aiService: AiService by lazy {
        GeminiAiService(apiKeyProvider)
    }

    override val appUpdateService: AppUpdateService by lazy {
        GitHubUpdateService(context)
    }
}

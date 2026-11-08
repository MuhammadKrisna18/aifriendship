package com.example.aifriendship.di

import android.content.Context
import com.example.aifriendship.data.local.ChatLocalDataSource
import com.example.aifriendship.data.local.JsonChatLocalDataSource
import com.example.aifriendship.data.repository.DefaultChatRepository
import com.example.aifriendship.domain.repository.ChatRepository

interface AppContainer {
    val chatRepository: ChatRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val localDataSource: ChatLocalDataSource by lazy {
        JsonChatLocalDataSource(context)
    }

    override val chatRepository: ChatRepository by lazy {
        DefaultChatRepository(localDataSource)
    }
}


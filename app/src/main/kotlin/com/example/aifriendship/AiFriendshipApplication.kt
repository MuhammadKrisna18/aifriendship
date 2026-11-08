package com.example.aifriendship

import android.app.Application
import com.example.aifriendship.di.AppContainer
import com.example.aifriendship.di.DefaultAppContainer

class AiFriendshipApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}


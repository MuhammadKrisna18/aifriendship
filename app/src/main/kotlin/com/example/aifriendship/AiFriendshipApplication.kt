package com.example.aifriendship

import android.app.Application
import com.example.aifriendship.data.worker.ProactiveScheduler
import com.example.aifriendship.di.AppContainer
import com.example.aifriendship.di.DefaultAppContainer
import com.example.aifriendship.presentation.notification.ProactiveNotificationHelper

class AiFriendshipApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
        ProactiveNotificationHelper.createNotificationChannel(this)
        ProactiveScheduler.schedule(this)
    }
}

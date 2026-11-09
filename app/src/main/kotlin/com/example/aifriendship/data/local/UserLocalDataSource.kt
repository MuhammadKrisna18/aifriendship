package com.example.aifriendship.data.local

import com.example.aifriendship.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface UserLocalDataSource {
    fun observeUserProfile(): StateFlow<UserProfile>
    suspend fun getUserProfile(): UserProfile
    suspend fun saveUserProfile(profile: UserProfile)
}


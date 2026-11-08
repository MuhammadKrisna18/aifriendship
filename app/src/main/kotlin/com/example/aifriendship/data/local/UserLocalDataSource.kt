package com.example.aifriendship.data.local

import com.example.aifriendship.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
    fun observeUserProfile(): Flow<UserProfile>
    suspend fun getUserProfile(): UserProfile
    suspend fun saveUserProfile(profile: UserProfile)
}


package com.example.aifriendship.domain.repository

import com.example.aifriendship.domain.model.CommunicationVibe
import com.example.aifriendship.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUserProfile(): Flow<UserProfile>
    suspend fun getUserProfile(): UserProfile
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun updateName(name: String)
    suspend fun updateVibe(vibe: CommunicationVibe)
    suspend fun completeOnboarding()
}


package com.example.aifriendship.domain.repository

import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUserProfile(): Flow<UserProfile>
    suspend fun getUserProfile(): UserProfile
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun updateName(name: String)
    suspend fun updateCompanion(companion: AiCompanion)
    suspend fun addCompanion(companion: AiCompanion)
    suspend fun setActiveCompanion(companionId: String)
    suspend fun removeCompanion(companionId: String)
    suspend fun completeOnboarding()
}

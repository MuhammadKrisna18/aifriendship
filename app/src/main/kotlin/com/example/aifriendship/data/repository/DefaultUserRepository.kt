package com.example.aifriendship.data.repository

import com.example.aifriendship.data.local.UserLocalDataSource
import com.example.aifriendship.domain.model.CommunicationVibe
import com.example.aifriendship.domain.model.UserProfile
import com.example.aifriendship.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class DefaultUserRepository(
    private val localDataSource: UserLocalDataSource
) : UserRepository {

    override fun observeUserProfile(): Flow<UserProfile> = localDataSource.observeUserProfile()

    override suspend fun getUserProfile(): UserProfile = localDataSource.getUserProfile()

    override suspend fun saveUserProfile(profile: UserProfile) {
        localDataSource.saveUserProfile(profile)
    }

    override suspend fun updateName(name: String) {
        val current = localDataSource.getUserProfile()
        localDataSource.saveUserProfile(current.copy(name = name.trim()))
    }

    override suspend fun updateVibe(vibe: CommunicationVibe) {
        val current = localDataSource.getUserProfile()
        localDataSource.saveUserProfile(current.copy(vibe = vibe))
    }

    override suspend fun completeOnboarding() {
        val current = localDataSource.getUserProfile()
        localDataSource.saveUserProfile(current.copy(isOnboardingCompleted = true))
    }
}


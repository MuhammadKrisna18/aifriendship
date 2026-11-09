package com.example.aifriendship.data.repository

import com.example.aifriendship.data.local.UserLocalDataSource
import com.example.aifriendship.domain.model.AiCompanion
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

    override suspend fun updateCompanion(companion: AiCompanion) {
        val current = localDataSource.getUserProfile()
        val existingList = current.allCompanions
        val updatedList = if (existingList.any { it.id == companion.id }) {
            existingList.map { if (it.id == companion.id) companion else it }
        } else {
            existingList + companion
        }
        localDataSource.saveUserProfile(
            current.copy(
                companion = companion,
                companions = updatedList,
                activeCompanionId = companion.id
            )
        )
    }

    override suspend fun addCompanion(companion: AiCompanion) {
        val current = localDataSource.getUserProfile()
        val existingList = current.allCompanions
        val updatedList = if (existingList.none { it.id == companion.id }) {
            existingList + companion
        } else {
            existingList.map { if (it.id == companion.id) companion else it }
        }
        localDataSource.saveUserProfile(
            current.copy(
                companion = companion,
                companions = updatedList,
                activeCompanionId = companion.id
            )
        )
    }

    override suspend fun setActiveCompanion(companionId: String) {
        val current = localDataSource.getUserProfile()
        val found = current.allCompanions.find { it.id == companionId }
        localDataSource.saveUserProfile(
            current.copy(
                companion = found ?: current.companion,
                activeCompanionId = companionId
            )
        )
    }

    override suspend fun removeCompanion(companionId: String) {
        val current = localDataSource.getUserProfile()
        val updatedList = current.allCompanions.filterNot { it.id == companionId }
        val finalCompanions = if (updatedList.isEmpty()) listOf(AiCompanion()) else updatedList
        val newActiveId = if (current.activeCompanionId == companionId) {
            finalCompanions.first().id
        } else {
            current.activeCompanionId
        }
        localDataSource.saveUserProfile(
            current.copy(
                companion = finalCompanions.first(),
                companions = finalCompanions,
                activeCompanionId = newActiveId
            )
        )
    }

    override suspend fun completeOnboarding() {
        val current = localDataSource.getUserProfile()
        localDataSource.saveUserProfile(current.copy(isOnboardingCompleted = true))
    }
}

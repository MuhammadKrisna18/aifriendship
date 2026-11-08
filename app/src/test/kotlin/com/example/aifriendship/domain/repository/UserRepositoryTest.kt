package com.example.aifriendship.domain.repository

import com.example.aifriendship.data.local.UserLocalDataSource
import com.example.aifriendship.data.remote.gemini.LiaPromptDefinition
import com.example.aifriendship.data.repository.DefaultUserRepository
import com.example.aifriendship.domain.model.CommunicationVibe
import com.example.aifriendship.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private lateinit var fakeLocalDataSource: FakeUserLocalDataSource
    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        fakeLocalDataSource = FakeUserLocalDataSource()
        repository = DefaultUserRepository(fakeLocalDataSource)
    }

    @Test
    fun defaultProfile_hasEmptyNameAndDefaultVibe() = runTest {
        val profile = repository.getUserProfile()
        assertEquals("", profile.name)
        assertEquals(CommunicationVibe.BARBAR, profile.vibe)
        assertEquals(false, profile.isOnboardingCompleted)
    }

    @Test
    fun updateName_persistsAndUpdatesFlow() = runTest {
        repository.updateName("Krisna")
        val updated = repository.observeUserProfile().first()
        assertEquals("Krisna", updated.name)
    }

    @Test
    fun updateVibe_persistsAndUpdatesFlow() = runTest {
        repository.updateVibe(CommunicationVibe.CHILL)
        val updated = repository.observeUserProfile().first()
        assertEquals(CommunicationVibe.CHILL, updated.vibe)
    }

    @Test
    fun completeOnboarding_setsFlagToTrue() = runTest {
        repository.completeOnboarding()
        val updated = repository.getUserProfile()
        assertTrue(updated.isOnboardingCompleted)
    }

    @Test
    fun buildSystemInstruction_injectsNameAndVibeCorrectly() {
        val profile = UserProfile(name = "Krisna", vibe = CommunicationVibe.CHILL)
        val instruction = LiaPromptDefinition.buildSystemInstruction(profile)
        assertTrue(instruction.contains("Krisna"))
        assertTrue(instruction.contains("kalem, hangat, santai"))
    }
}

private class FakeUserLocalDataSource : UserLocalDataSource {
    private val flow = MutableStateFlow(UserProfile())

    override fun observeUserProfile(): Flow<UserProfile> = flow.asStateFlow()

    override suspend fun getUserProfile(): UserProfile = flow.value

    override suspend fun saveUserProfile(profile: UserProfile) {
        flow.value = profile
    }
}


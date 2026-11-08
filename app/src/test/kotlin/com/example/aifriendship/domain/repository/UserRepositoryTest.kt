package com.example.aifriendship.domain.repository

import com.example.aifriendship.data.local.UserLocalDataSource
import com.example.aifriendship.data.remote.gemini.LiaPromptDefinition
import com.example.aifriendship.data.repository.DefaultUserRepository
import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.CommunicationVibe
import com.example.aifriendship.domain.model.CompanionTrait
import com.example.aifriendship.domain.model.RandomCompanionGenerator
import com.example.aifriendship.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals("Lia", profile.companion.name)
        assertEquals(CompanionTrait.BARBAR, profile.companion.trait)
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
    fun updateCompanion_persistsAndUpdatesFlow() = runTest {
        val companion = AiCompanion(
            name = "Clara",
            trait = CompanionTrait.TSUNDERE,
            avatarColorHex = "#EC4899"
        )
        repository.updateCompanion(companion)
        val updated = repository.observeUserProfile().first()
        assertEquals("Clara", updated.companion.name)
        assertEquals(CompanionTrait.TSUNDERE, updated.companion.trait)
    }

    @Test
    fun completeOnboarding_setsFlagToTrue() = runTest {
        repository.completeOnboarding()
        val updated = repository.getUserProfile()
        assertTrue(updated.isOnboardingCompleted)
    }

    @Test
    fun randomCompanionGenerator_generatesValidCompanion() {
        val randomCompanion = RandomCompanionGenerator.generateRandom()
        assertTrue(randomCompanion.name.isNotBlank())
        assertNotNull(randomCompanion.trait)
        assertTrue(randomCompanion.avatarColorHex.startsWith("#"))
    }

    @Test
    fun buildSystemInstruction_injectsCompanionNameAndTrait() {
        val companion = AiCompanion(name = "Clara", trait = CompanionTrait.TSUNDERE)
        val profile = UserProfile(name = "Krisna", companion = companion)
        val instruction = LiaPromptDefinition.buildSystemInstruction(profile)
        assertTrue(instruction.contains("Clara"))
        assertTrue(instruction.contains("Krisna"))
        assertTrue(instruction.contains("tsundere"))
    }

    @Test
    fun buildSystemInstruction_withCustomTrait_injectsCustomDescription() {
        val companion = AiCompanion(
            name = "Vira",
            trait = CompanionTrait.CUSTOM,
            customTraitDescription = "suka ngopi, ceplas-ceplos gen Z"
        )
        val profile = UserProfile(name = "Krisna", companion = companion)
        val instruction = LiaPromptDefinition.buildSystemInstruction(profile)
        assertTrue(instruction.contains("Vira"))
        assertTrue(instruction.contains("suka ngopi, ceplas-ceplos gen Z"))
    }

    @Test
    fun formatTimeContext_returnsFormattedDateAndClock() {
        val (description, clock) = LiaPromptDefinition.formatTimeContext(1762646400000L)
        assertTrue(description.isNotBlank())
        assertTrue(clock.contains(":"))
    }

    @Test
    fun buildSystemInstruction_injectsRealTimeAwareness() {
        val instruction = LiaPromptDefinition.buildSystemInstruction(null)
        assertTrue(instruction.contains("Real-time Awareness"))
        assertTrue(instruction.contains("Jam sekarang:"))
        assertTrue(instruction.contains("Waktu sekarang di perangkat user:"))
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

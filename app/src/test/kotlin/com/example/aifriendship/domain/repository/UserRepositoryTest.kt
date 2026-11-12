package com.example.aifriendship.domain.repository

import com.example.aifriendship.data.local.UserLocalDataSource
import com.example.aifriendship.data.remote.gemini.LiaPromptDefinition
import com.example.aifriendship.data.repository.DefaultUserRepository
import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.CompanionMemory
import com.example.aifriendship.domain.model.CompanionMood
import com.example.aifriendship.domain.model.CompanionRelationship
import com.example.aifriendship.domain.model.CompanionTrait
import com.example.aifriendship.domain.model.RandomCompanionGenerator
import com.example.aifriendship.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    fun defaultProfile_hasEmptyNameAndNotCompleted() = runTest {
        val profile = repository.getUserProfile()
        assertEquals("", profile.name)
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
        assertTrue(instruction.contains("tsundere", ignoreCase = true))
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
        assertTrue(instruction.contains("Real-time"))
        assertTrue(instruction.contains("Jam sekarang:"))
        assertTrue(instruction.contains("Waktu sekarang di perangkat user:"))
    }

    @Test
    fun buildSystemInstruction_withMultipleTraits_injectsCombinedInstructions() {
        val companion = AiCompanion(
            name = "Maya",
            traits = listOf(CompanionTrait.TSUNDERE, CompanionTrait.MATERNAL)
        )
        val profile = UserProfile(name = "Krisna", companion = companion)
        val instruction = LiaPromptDefinition.buildSystemInstruction(profile)
        assertTrue(instruction.contains("Maya"))
        assertTrue(instruction.contains("Kombinasi Karakter Gabungan"))
        assertTrue(instruction.contains("tsundere", ignoreCase = true))
        assertTrue(instruction.contains("keibuan", ignoreCase = true))
    }

    @Test
    fun buildSystemInstruction_withRelationshipAndMood_injectsContext() {
        val companion = AiCompanion(
            name = "Rina",
            memory = CompanionMemory(
                currentRelationship = CompanionRelationship.PACAR,
                currentMood = CompanionMood.SALTING
            )
        )
        val profile = UserProfile(name = "Xavi", companion = companion)
        val instruction = LiaPromptDefinition.buildSystemInstruction(profile)
        assertTrue(instruction.contains("Pacar / Kekasih"))
        assertTrue(instruction.contains("Salting"))
    }

    @Test
    fun buildSystemInstruction_withActivityMemoryAndFacts_injectsMemory() {
        val companion = AiCompanion(
            name = "Rina",
            memory = CompanionMemory(
                lastActivity = "Lagi kuis kalkulus",
                activityTimestamp = 1762646400000L,
                userFacts = listOf("Suka kopi latte", "Lagi skripsi")
            )
        )
        val profile = UserProfile(name = "Xavi", companion = companion)
        val instruction = LiaPromptDefinition.buildSystemInstruction(profile)
        assertTrue(instruction.contains("Lagi kuis kalkulus"))
        assertTrue(instruction.contains("Suka kopi latte"))
        assertTrue(instruction.contains("Lagi skripsi"))
    }

    @Test
    fun buildSystemInstruction_injectsAuthenticPersonalityGuidelines() {
        val instruction = LiaPromptDefinition.buildSystemInstruction(null)
        assertTrue(instruction.contains("Prinsip Percakapan"))
        assertTrue(instruction.contains("Ekspresikan Watak Inti"))
        assertTrue(instruction.contains("Format Output"))
    }

    @Test
    fun repository_addCompanion_updatesListAndActiveId() = runTest {
        val newCompanion = AiCompanion(id = "comp-123", name = "Aurel")
        repository.addCompanion(newCompanion)
        val profile = repository.getUserProfile()
        assertTrue(profile.allCompanions.any { it.id == "comp-123" })
        assertEquals("comp-123", profile.activeCompanionId)
    }

    @Test
    fun repository_setActiveCompanion_switchesActiveCompanion() = runTest {
        val companion1 = AiCompanion(id = "comp-1", name = "Lia")
        val companion2 = AiCompanion(id = "comp-2", name = "Chika")
        repository.addCompanion(companion1)
        repository.addCompanion(companion2)
        repository.setActiveCompanion("comp-1")
        val profile = repository.getUserProfile()
        assertEquals("comp-1", profile.activeCompanionId)
        assertEquals("Lia", profile.activeCompanion.name)
    }
}

private class FakeUserLocalDataSource : UserLocalDataSource {
    private val flow = MutableStateFlow(UserProfile())

    override fun observeUserProfile(): StateFlow<UserProfile> = flow.asStateFlow()

    override suspend fun getUserProfile(): UserProfile = flow.value

    override suspend fun saveUserProfile(profile: UserProfile) {
        flow.value = profile
    }
}

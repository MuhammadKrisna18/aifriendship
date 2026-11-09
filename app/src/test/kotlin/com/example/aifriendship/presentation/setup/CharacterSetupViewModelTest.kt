package com.example.aifriendship.presentation.setup

import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.CompanionMood
import com.example.aifriendship.domain.model.CompanionRelationship
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.UserProfile
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterSetupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var fakeChatRepository: FakeChatRepository
    private lateinit var viewModel: CharacterSetupViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUserRepository = FakeUserRepository()
        fakeChatRepository = FakeChatRepository()
        viewModel = CharacterSetupViewModel(fakeUserRepository, fakeChatRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDefaults() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("", state.userName)
        assertEquals("Lia", state.companionName)
        assertEquals("#6366F1", state.avatarColorHex)
    }

    @Test
    fun onUserNameChanged_updatesState() = runTest {
        viewModel.onUserNameChanged("Krisna")
        assertEquals("Krisna", viewModel.uiState.value.userName)
    }

    @Test
    fun onCompanionNameChanged_updatesState() = runTest {
        viewModel.onCompanionNameChanged("Clara")
        assertEquals("Clara", viewModel.uiState.value.companionName)
    }

    @Test
    fun onAvatarColorSelected_updatesState() = runTest {
        viewModel.onAvatarColorSelected("#EC4899")
        assertEquals("#EC4899", viewModel.uiState.value.avatarColorHex)
    }

    @Test
    fun onRandomize_generatesNewCompanionData() = runTest {
        viewModel.onRandomize()
        val state = viewModel.uiState.value
        assertTrue(state.companionName.isNotBlank())
        assertTrue(state.avatarColorHex.startsWith("#"))
    }

    @Test
    fun saveAndStartChat_savesProfileAndCreatesFirstGreeting() = runTest {
        var completed = false
        viewModel.onUserNameChanged("Krisna")
        viewModel.onCompanionNameChanged("Clara")
        viewModel.onAvatarColorSelected("#10B981")

        viewModel.saveAndStartChat {
            completed = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(completed)
        val profile = fakeUserRepository.getUserProfile()
        assertEquals("Krisna", profile.name)
        assertEquals("Clara", profile.activeCompanion.name)
        assertEquals("#10B981", profile.activeCompanion.avatarColorHex)
        assertEquals(CompanionRelationship.TEMAN_BIASA, profile.activeCompanion.currentRelationship)
        assertEquals(CompanionMood.BIASA, profile.activeCompanion.currentMood)
        assertTrue(profile.isOnboardingCompleted)

        val messages = fakeChatRepository.getMessages().first()
        assertEquals(1, messages.size)
        assertTrue(messages.first().text.contains("Krisna"))
        assertEquals(profile.activeCompanion.id, messages.first().companionId)
    }
}

private class FakeUserRepository(
    initial: UserProfile = UserProfile()
) : UserRepository {
    private val profileFlow = MutableStateFlow(initial)

    override fun observeUserProfile(): StateFlow<UserProfile> = profileFlow.asStateFlow()

    override suspend fun getUserProfile(): UserProfile = profileFlow.value

    override suspend fun saveUserProfile(profile: UserProfile) {
        profileFlow.value = profile
    }

    override suspend fun updateName(name: String) {
        profileFlow.value = profileFlow.value.copy(name = name)
    }

    override suspend fun updateCompanion(companion: AiCompanion) {
        profileFlow.value = profileFlow.value.copy(companion = companion)
    }

    override suspend fun addCompanion(companion: AiCompanion) {
        val current = profileFlow.value
        profileFlow.value = current.copy(
            companions = current.allCompanions + companion,
            activeCompanionId = companion.id
        )
    }

    override suspend fun setActiveCompanion(companionId: String) {
        profileFlow.value = profileFlow.value.copy(activeCompanionId = companionId)
    }

    override suspend fun removeCompanion(companionId: String) {
        val current = profileFlow.value
        profileFlow.value = current.copy(
            companions = current.allCompanions.filterNot { it.id == companionId }
        )
    }

    override suspend fun completeOnboarding() {
        profileFlow.value = profileFlow.value.copy(isOnboardingCompleted = true)
    }
}

private class FakeChatRepository : ChatRepository {
    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val statusFlow = MutableStateFlow(LiaStatus())

    override fun getMessages(): Flow<List<ChatMessage>> = messagesFlow.asStateFlow()

    override fun getMessagesByCompanion(companionId: String): Flow<List<ChatMessage>> = messagesFlow.asStateFlow()

    override fun getLatestMessagesPerCompanion(): Flow<Map<String, ChatMessage>> = MutableStateFlow(emptyMap())

    override suspend fun saveMessage(message: ChatMessage) {
        messagesFlow.value = messagesFlow.value + message
    }

    override suspend fun saveMessages(messages: List<ChatMessage>) {
        messagesFlow.value = messagesFlow.value + messages
    }

    override suspend fun deleteMessage(id: String) {
        messagesFlow.value = messagesFlow.value.filterNot { it.id == id }
    }

    override suspend fun clearMessages() {
        messagesFlow.value = emptyList()
    }

    override suspend fun clearMessagesByCompanion(companionId: String) {
        messagesFlow.value = messagesFlow.value.filterNot { it.companionId == companionId }
    }

    override fun getLiaStatus(): Flow<LiaStatus> = statusFlow.asStateFlow()

    override suspend fun updateLiaStatus(status: LiaStatus) {
        statusFlow.value = status
    }
}

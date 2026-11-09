package com.example.aifriendship.presentation.home

import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.CompanionTrait
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.UserProfile
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.repository.UserRepository
import com.example.aifriendship.domain.service.AppUpdateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var fakeChatRepository: FakeChatRepository
    private lateinit var fakeAppUpdateService: FakeAppUpdateService
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val initialCompanion = AiCompanion(id = "comp-1", name = "Lia", trait = CompanionTrait.BARBAR)
        fakeUserRepository = FakeUserRepository(
            initial = UserProfile(
                name = "Krisna",
                companion = initialCompanion,
                companions = listOf(initialCompanion),
                activeCompanionId = "comp-1",
                isOnboardingCompleted = true
            )
        )
        fakeChatRepository = FakeChatRepository()
        fakeAppUpdateService = FakeAppUpdateService()
        viewModel = HomeViewModel(fakeUserRepository, fakeChatRepository, fakeAppUpdateService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsUserCompanions() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("Krisna", state.userProfile.name)
        assertEquals(HomeTab.CHATS, state.selectedTab)
        assertEquals(1, state.userProfile.allCompanions.size)
    }

    @Test
    fun onTabSelected_switchesActiveTab() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        viewModel.onTabSelected(HomeTab.COMPANIONS)
        advanceUntilIdle()
        assertEquals(HomeTab.COMPANIONS, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun onSearchQueryChanged_updatesSearchQuery() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("Maya")
        advanceUntilIdle()
        assertEquals("Maya", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun profileDialog_openAndDismiss_togglesVisibility() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showProfileDialog)

        viewModel.onOpenProfileDialog()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showProfileDialog)

        viewModel.onDismissProfileDialog()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showProfileDialog)
    }

    @Test
    fun onSaveProfile_updatesName() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        viewModel.onSaveProfile("Krisna Arga")
        advanceUntilIdle()

        val updatedProfile = fakeUserRepository.getUserProfile()
        assertEquals("Krisna Arga", updatedProfile.name)
        assertFalse(viewModel.uiState.value.showProfileDialog)
    }

    @Test
    fun onSelectCompanion_setsActiveCompanionId() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        val secondCompanion = AiCompanion(id = "comp-2", name = "Maya")
        fakeUserRepository.addCompanion(secondCompanion)
        advanceUntilIdle()

        viewModel.onSelectCompanion("comp-2")
        advanceUntilIdle()

        assertEquals("comp-2", fakeUserRepository.getUserProfile().activeCompanionId)
    }

    @Test
    fun onDeleteCompanion_removesCompanionAndClearsMessages() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        val secondCompanion = AiCompanion(id = "comp-2", name = "Maya")
        fakeUserRepository.addCompanion(secondCompanion)
        advanceUntilIdle()

        viewModel.onDeleteCompanion("comp-2")
        advanceUntilIdle()

        assertFalse(fakeUserRepository.getUserProfile().allCompanions.any { it.id == "comp-2" })
    }
}

private class FakeUserRepository(
    initial: UserProfile = UserProfile()
) : UserRepository {
    private val profileFlow = MutableStateFlow(initial)

    override fun observeUserProfile(): Flow<UserProfile> = profileFlow.asStateFlow()

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

private class FakeAppUpdateService : AppUpdateService {
    override suspend fun checkForUpdate(): Result<AppUpdateInfo> = Result.success(
        AppUpdateInfo(
            isUpdateAvailable = false,
            currentVersion = "1.3.0",
            latestVersion = "1.3.0",
            downloadUrl = "",
            releaseNotes = ""
        )
    )

    override fun downloadAndInstallUpdate(downloadUrl: String, fileName: String): Result<Unit> = Result.success(Unit)
}

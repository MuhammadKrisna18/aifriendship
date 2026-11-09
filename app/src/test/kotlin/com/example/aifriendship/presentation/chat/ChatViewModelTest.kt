package com.example.aifriendship.presentation.chat

import com.example.aifriendship.data.config.ApiKeyProvider
import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.AiResponse
import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.CompanionMood
import com.example.aifriendship.domain.model.CompanionRelationship
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.ProactiveTriggerReason
import com.example.aifriendship.domain.model.QuotedMessage
import com.example.aifriendship.domain.model.UserProfile
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.repository.UserRepository
import com.example.aifriendship.domain.service.AiService
import com.example.aifriendship.domain.service.AllModelsQuotaExceededException
import com.example.aifriendship.domain.service.AppUpdateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var fakeAiService: FakeAiService
    private lateinit var fakeApiKeyProvider: FakeApiKeyProvider
    private lateinit var fakeAppUpdateService: FakeAppUpdateService
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeChatRepository()
        fakeUserRepository = FakeUserRepository()
        fakeAiService = FakeAiService()
        fakeApiKeyProvider = FakeApiKeyProvider()
        fakeAppUpdateService = FakeAppUpdateService()
        viewModel = ChatViewModel(
            fakeRepository,
            fakeUserRepository,
            fakeAiService,
            fakeApiKeyProvider,
            fakeAppUpdateService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onInputTextChanged_updatesState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onInputTextChanged("Halo Lia")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Halo Lia", viewModel.uiState.value.inputText)
    }

    @Test
    fun sendMessage_addsUserMessageAndTriggersAiResponse() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onInputTextChanged("Ajarin pointer dong")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.inputText)
        val messages = fakeRepository.getMessages().first()
        assertEquals(3, messages.size)
        assertEquals(MessageSender.USER, messages[0].sender)
        assertEquals("Ajarin pointer dong", messages[0].text)

        assertEquals(MessageSender.AI, messages[1].sender)
        assertEquals("wah rajin banget", messages[1].text)

        assertEquals(MessageSender.AI, messages[2].sender)
        assertEquals("pointer itu alamat memori", messages[2].text)
    }

    @Test
    fun sendMessage_updatesCompanionMemoryFromAiResponse() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeAiService.customResponse = AiResponse(
            bubbles = listOf("Sip kelar nugas"),
            statusSubtitle = "Lagi santai di kamar",
            mood = "SALTING",
            activityUpdate = "Nonton anime di kamar",
            relationshipUpdate = "GEBETAN"
        )
        viewModel.onInputTextChanged("Kamu lagi apa?")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedCompanion = fakeUserRepository.getUserProfile().activeCompanion
        assertEquals("Nonton anime di kamar", updatedCompanion.memory.lastActivity)
        assertEquals(CompanionMood.SALTING, updatedCompanion.currentMood)
        assertEquals(CompanionRelationship.GEBETAN, updatedCompanion.currentRelationship)
    }

    @Test
    fun sendMessage_learnsNewFactAndUpdatesCompanionMemory() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeAiService.customResponse = AiResponse(
            bubbles = listOf("Oalah pantesan"),
            statusSubtitle = "Mendengarkan",
            newFactLearned = "Suka kopi latte dingin"
        )
        viewModel.onInputTextChanged("Gue tiap pagi minum latte dingin")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedCompanion = fakeUserRepository.getUserProfile().activeCompanion
        assertTrue(updatedCompanion.memory.userFacts.contains("Suka kopi latte dingin"))
    }

    @Test
    fun addUserFact_and_deleteUserFact_managesFactsCorrectly() = runTest {
        viewModel.addUserFact("Alergi udang")
        testDispatcher.scheduler.advanceUntilIdle()
        var companion = fakeUserRepository.getUserProfile().activeCompanion
        assertTrue(companion.memory.userFacts.contains("Alergi udang"))

        viewModel.deleteUserFact("Alergi udang")
        testDispatcher.scheduler.advanceUntilIdle()
        companion = fakeUserRepository.getUserProfile().activeCompanion
        assertFalse(companion.memory.userFacts.contains("Alergi udang"))
    }

    @Test
    fun memoryDialog_toggleVisibility() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isMemoryDialogVisible)

        viewModel.showMemoryDialog()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isMemoryDialogVisible)

        viewModel.hideMemoryDialog()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isMemoryDialogVisible)
    }

    @Test
    fun onQuotaExceeded_triggersRoleplayExcuseAndCooldown() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeAiService.shouldFailWithQuota = true
        viewModel.onInputTextChanged("Spam chat")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = fakeRepository.getMessages().first()
        assertTrue(messages.any { it.text.contains("beli garem") })
        val status = fakeRepository.getLiaStatus().first()
        assertEquals("online", status.subtitle)
    }

    @Test
    fun checkForUpdates_findsNewVersion() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeAppUpdateService.hasUpdate = true
        viewModel.checkForUpdates(silent = false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.updateInfo?.isUpdateAvailable == true)
        assertEquals("v1.0.1", viewModel.uiState.value.updateInfo?.latestVersion)
    }

    @Test
    fun onboarding_handlesNameInputAndCompletesOnboarding() = runTest {
        val emptyUserRepo = FakeUserRepository(
            UserProfile(name = "", isOnboardingCompleted = false)
        )
        val freshViewModel = ChatViewModel(
            fakeRepository,
            emptyUserRepo,
            fakeAiService,
            fakeApiKeyProvider,
            fakeAppUpdateService
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            freshViewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(OnboardingStep.ASK_NAME, freshViewModel.uiState.value.onboardingStep)

        freshViewModel.onInputTextChanged("Krisna")
        freshViewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Krisna", emptyUserRepo.getUserProfile().name)
        assertTrue(emptyUserRepo.getUserProfile().isOnboardingCompleted)
        assertEquals(OnboardingStep.COMPLETED, freshViewModel.uiState.value.onboardingStep)
    }
}

private class FakeAppUpdateService : AppUpdateService {
    var hasUpdate: Boolean = false

    override suspend fun checkForUpdate(): Result<AppUpdateInfo> {
        return Result.success(
            AppUpdateInfo(
                isUpdateAvailable = hasUpdate,
                currentVersion = "1.0",
                latestVersion = if (hasUpdate) "v1.0.1" else "1.0",
                downloadUrl = "https://example.com/app.apk",
                releaseNotes = "Perbaikan performa dan fitur baru"
            )
        )
    }

    override suspend fun downloadAndInstallUpdate(
        downloadUrl: String,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Result<Unit> {
        onProgress(100)
        return Result.success(Unit)
    }
}

private class FakeApiKeyProvider : ApiKeyProvider {
    private var key = "test-api-key"
    private val keyFlow = MutableStateFlow(key)

    override fun getApiKey(): String = key

    override fun observeApiKey(): Flow<String> = keyFlow

    override fun setApiKey(apiKey: String) {
        key = apiKey
        keyFlow.value = apiKey
    }
}

private class FakeAiService : AiService {
    private var model = "gemini-2.5-flash"
    var shouldFailWithQuota = false
    var customResponse: AiResponse? = null

    override fun getActiveModel(): String = model

    override fun setActiveModel(modelName: String) {
        model = modelName
    }

    override suspend fun generateReply(
        history: List<ChatMessage>,
        quotedMessage: QuotedMessage?,
        userProfile: UserProfile?
    ): Result<AiResponse> {
        if (shouldFailWithQuota) {
            return Result.failure(AllModelsQuotaExceededException())
        }
        val response = customResponse ?: AiResponse(
            bubbles = listOf("wah rajin banget", "pointer itu alamat memori"),
            statusSubtitle = "gemas juga ngajarin dia ngoding"
        )
        return Result.success(response)
    }

    override suspend fun generateProactiveMessage(
        companion: AiCompanion,
        userProfile: UserProfile,
        history: List<ChatMessage>,
        reason: ProactiveTriggerReason
    ): Result<String> {
        return Result.success("Halo! Lagi ngapain nih?")
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

private class FakeUserRepository(
    initial: UserProfile = UserProfile(name = "Krisna", isOnboardingCompleted = true)
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

package com.example.aifriendship.presentation.chat

import com.example.aifriendship.data.config.ApiKeyProvider
import com.example.aifriendship.domain.model.AiResponse
import com.example.aifriendship.domain.model.AppUpdateInfo
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.LiaStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.QuotedMessage
import com.example.aifriendship.domain.model.UserProfile
import com.example.aifriendship.domain.repository.ChatRepository
import com.example.aifriendship.domain.service.AiService
import com.example.aifriendship.domain.service.AllModelsQuotaExceededException
import com.example.aifriendship.domain.service.AppUpdateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var fakeAiService: FakeAiService
    private lateinit var fakeApiKeyProvider: FakeApiKeyProvider
    private lateinit var fakeAppUpdateService: FakeAppUpdateService
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeChatRepository()
        fakeAiService = FakeAiService()
        fakeApiKeyProvider = FakeApiKeyProvider()
        fakeAppUpdateService = FakeAppUpdateService()
        viewModel = ChatViewModel(fakeRepository, fakeAiService, fakeApiKeyProvider, fakeAppUpdateService)
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

    override fun downloadAndInstallUpdate(downloadUrl: String, fileName: String): Result<Unit> {
        return Result.success(Unit)
    }
}

private class FakeApiKeyProvider : ApiKeyProvider {
    private var key: String = "fake_key"
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
        return Result.success(
            AiResponse(
                bubbles = listOf("wah rajin banget", "pointer itu alamat memori"),
                statusSubtitle = "gemas juga ngajarin dia ngoding"
            )
        )
    }
}

private class FakeChatRepository : ChatRepository {
    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val statusFlow = MutableStateFlow(LiaStatus())

    override fun getMessages(): Flow<List<ChatMessage>> = messagesFlow

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

    override fun getLiaStatus(): Flow<LiaStatus> = statusFlow

    override suspend fun updateLiaStatus(status: LiaStatus) {
        statusFlow.value = status
    }
}

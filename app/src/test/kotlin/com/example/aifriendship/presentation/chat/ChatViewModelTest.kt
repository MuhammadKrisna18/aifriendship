package com.example.aifriendship.presentation.chat

import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.KaleaStatus
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.repository.ChatRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeChatRepository()
        viewModel = ChatViewModel(fakeRepository)
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

        viewModel.onInputTextChanged("Halo Kalea")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Halo Kalea", viewModel.uiState.value.inputText)
    }

    @Test
    fun sendMessage_addsMessageAndClearsInput() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onInputTextChanged("Belajar pointer")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.inputText)
        val messages = fakeRepository.getMessages().first()
        assertEquals(1, messages.size)
        assertEquals("Belajar pointer", messages[0].text)
        assertEquals(MessageSender.USER, messages[0].sender)
    }
}

private class FakeChatRepository : ChatRepository {
    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val statusFlow = MutableStateFlow(KaleaStatus())

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

    override fun getKaleaStatus(): Flow<KaleaStatus> = statusFlow

    override suspend fun updateKaleaStatus(status: KaleaStatus) {
        statusFlow.value = status
    }
}

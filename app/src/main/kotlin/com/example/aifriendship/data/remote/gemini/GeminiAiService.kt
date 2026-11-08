package com.example.aifriendship.data.remote.gemini

import com.example.aifriendship.data.config.ApiKeyProvider
import com.example.aifriendship.data.remote.gemini.dto.GeminiContent
import com.example.aifriendship.data.remote.gemini.dto.GeminiGenerationConfig
import com.example.aifriendship.data.remote.gemini.dto.GeminiPart
import com.example.aifriendship.data.remote.gemini.dto.GeminiRequestDto
import com.example.aifriendship.data.remote.gemini.dto.GeminiResponseDto
import com.example.aifriendship.data.remote.gemini.dto.GeminiSystemInstruction
import com.example.aifriendship.domain.model.AiResponse
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.QuotedMessage
import com.example.aifriendship.domain.service.AiService
import com.example.aifriendship.domain.service.AllModelsQuotaExceededException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiAiService(
    private val apiKeyProvider: ApiKeyProvider,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) : AiService {

    private val fallbackModelChain = listOf(
        "gemini-2.5-flash",
        "gemini-flash-latest",
        "gemini-3.5-flash",
        "gemini-flash-lite-latest"
    )

    private var activeModelIndex: Int = 0

    override fun getActiveModel(): String = fallbackModelChain[activeModelIndex]

    override fun setActiveModel(modelName: String) {
        val foundIndex = fallbackModelChain.indexOf(modelName)
        if (foundIndex != -1) {
            activeModelIndex = foundIndex
        }
    }

    fun resetToPrimaryModel() {
        activeModelIndex = 0
    }

    override suspend fun generateReply(
        history: List<ChatMessage>,
        quotedMessage: QuotedMessage?
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("API Key belum disetel"))
        }

        val requestBody = prepareRequestBody(history, quotedMessage)
        var lastException: Exception? = null

        val modelsToTry = fallbackModelChain.indices.map { offset ->
            (activeModelIndex + offset) % fallbackModelChain.size
        }

        for (modelIdx in modelsToTry) {
            val candidateModel = fallbackModelChain[modelIdx]
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$candidateModel:generateContent?key=$apiKey"
            val httpRequest = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            try {
                client.newCall(httpRequest).execute().use { response ->
                    if (response.code == 429) {
                        lastException = AllModelsQuotaExceededException("Quota exceeded on $candidateModel", 429)
                        return@use
                    }

                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string().orEmpty()
                        lastException = IOException("HTTP ${response.code}: $errorBody")
                        return@use
                    }

                    val responseBodyString = response.body?.string().orEmpty()
                    val geminiResponse = json.decodeFromString<GeminiResponseDto>(responseBodyString)
                    val rawText = geminiResponse.candidates.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text
                        ?.trim()
                        .orEmpty()

                    val cleanJson = cleanJsonString(rawText)
                    val parsedAiResponse = try {
                        json.decodeFromString<AiResponse>(cleanJson)
                    } catch (_: Exception) {
                        fallbackTextToAiResponse(rawText)
                    }

                    activeModelIndex = modelIdx
                    return@withContext Result.success(parsedAiResponse)
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: AllModelsQuotaExceededException())
    }

    private fun prepareRequestBody(
        history: List<ChatMessage>,
        quotedMessage: QuotedMessage?
    ): okhttp3.RequestBody {
        val slidingHistory = history.takeLast(15)
        val contents = slidingHistory.map { msg ->
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            GeminiContent(
                role = role,
                parts = listOf(GeminiPart(text = msg.text))
            )
        }.toMutableList()

        if (quotedMessage != null) {
            val lastUserIndex = contents.indexOfLast { it.role == "user" }
            if (lastUserIndex != -1) {
                val originalText = contents[lastUserIndex].parts.firstOrNull()?.text.orEmpty()
                val quotedContext = "[Membalas ${quotedMessage.senderName}: \"${quotedMessage.text}\"]\n$originalText"
                contents[lastUserIndex] = GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = quotedContext))
                )
            }
        }

        val requestBodyDto = GeminiRequestDto(
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = LiaPromptDefinition.SYSTEM_INSTRUCTION))
            ),
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.88f,
                topP = 0.95f,
                responseMimeType = "application/json"
            )
        )

        val jsonString = json.encodeToString(requestBodyDto)
        return jsonString.toRequestBody("application/json".toMediaType())
    }

    private fun cleanJsonString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        return text.trim()
    }

    private fun fallbackTextToAiResponse(raw: String): AiResponse {
        val lines = raw.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val bubbles = if (lines.isNotEmpty()) lines else listOf(raw)
        return AiResponse(
            bubbles = bubbles,
            statusSubtitle = "online"
        )
    }
}

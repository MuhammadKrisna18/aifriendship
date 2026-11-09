package com.example.aifriendship.data.remote.gemini

import com.example.aifriendship.data.config.ApiKeyProvider
import com.example.aifriendship.data.remote.gemini.dto.GeminiContent
import com.example.aifriendship.data.remote.gemini.dto.GeminiGenerationConfig
import com.example.aifriendship.data.remote.gemini.dto.GeminiPart
import com.example.aifriendship.data.remote.gemini.dto.GeminiRequestDto
import com.example.aifriendship.data.remote.gemini.dto.GeminiResponseDto
import com.example.aifriendship.data.remote.gemini.dto.GeminiSystemInstruction
import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.AiResponse
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.ChatTimeFormatter
import com.example.aifriendship.domain.model.MessageSender
import com.example.aifriendship.domain.model.ProactiveTriggerReason
import com.example.aifriendship.domain.model.QuotedMessage
import com.example.aifriendship.domain.model.UserProfile
import com.example.aifriendship.domain.service.AiService
import com.example.aifriendship.domain.service.AllModelsQuotaExceededException
import com.example.aifriendship.domain.service.QuotaExceededException
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
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite",
        "gemini-2.5-flash",
        "gemini-flash-latest"
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
        quotedMessage: QuotedMessage?,
        userProfile: UserProfile?
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("API Key belum disetel"))
        }

        val requestBody = prepareRequestBody(history, quotedMessage, userProfile)
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
                    val rawParsedAiResponse = try {
                        json.decodeFromString<AiResponse>(cleanJson)
                    } catch (_: Exception) {
                        fallbackTextToAiResponse(rawText)
                    }

                    val sanitizedBubbles = sanitizeBubbles(rawParsedAiResponse.bubbles)
                    val parsedAiResponse = rawParsedAiResponse.copy(
                        bubbles = if (sanitizedBubbles.isEmpty()) listOf("...") else sanitizedBubbles
                    )

                    activeModelIndex = modelIdx
                    return@withContext Result.success(parsedAiResponse)
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: AllModelsQuotaExceededException())
    }

    override suspend fun generateProactiveMessage(
        companion: AiCompanion,
        userProfile: UserProfile,
        history: List<ChatMessage>,
        reason: ProactiveTriggerReason
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("API key tidak tersedia"))
        }

        val prompt = LiaPromptDefinition.buildProactivePrompt(companion, userProfile, reason)
        val slidingHistory = history.takeLast(8)
        val historyContext = if (slidingHistory.isNotEmpty()) {
            val lines = slidingHistory.joinToString("\n") { msg ->
                val senderName = if (msg.sender == MessageSender.USER) userProfile.name.ifBlank { "User" } else companion.name
                "[$senderName]: ${msg.text}"
            }
            "\n[RINGKASAN PESAN SEBELUMNYA]\n$lines\n"
        } else ""

        val contents = listOf(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = "$prompt\n$historyContext\nKirimkan 1 pesan chat pendek pembukamu:"))
            )
        )

        val requestBodyDto = GeminiRequestDto(
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.85f,
                topP = 0.95f
            )
        )
        val jsonString = json.encodeToString(requestBodyDto)
        val requestBody = jsonString.toRequestBody("application/json".toMediaType())

        var lastException: Exception? = null
        for (i in fallbackModelChain.indices) {
            val modelIdx = (activeModelIndex + i) % fallbackModelChain.size
            val model = fallbackModelChain[modelIdx]
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val request = Request.Builder().url(url).post(requestBody).build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val code = response.code
                        if (code == 429) {
                            lastException = QuotaExceededException("Quota exceeded on $model")
                            return@use
                        }
                        lastException = IOException("HTTP $code on $model")
                        return@use
                    }
                    val bodyString = response.body?.string().orEmpty()
                    val responseDto = json.decodeFromString<GeminiResponseDto>(bodyString)
                    val rawText = responseDto.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty().trim()
                    val cleanText = rawText.removeSurrounding("\"").trim()
                    if (cleanText.isNotBlank()) {
                        activeModelIndex = modelIdx
                        return@withContext Result.success(cleanText)
                    }
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: AllModelsQuotaExceededException())
    }

    private fun prepareRequestBody(
        history: List<ChatMessage>,
        quotedMessage: QuotedMessage?,
        userProfile: UserProfile?
    ): okhttp3.RequestBody {
        val slidingHistory = history.takeLast(15)
        val contents = slidingHistory.map { msg ->
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            val timePrefix = ChatTimeFormatter.format(msg.timestamp)
            GeminiContent(
                role = role,
                parts = listOf(GeminiPart(text = "[$timePrefix] ${msg.text}"))
            )
        }.toMutableList()

        val lastUserMsg = slidingHistory.lastOrNull { it.sender == MessageSender.USER }
        val prevMsg = slidingHistory.dropLast(1).lastOrNull()
        val timeGapContext = if (lastUserMsg != null && prevMsg != null) {
            val gapMillis = lastUserMsg.timestamp - prevMsg.timestamp
            formatTimeGap(gapMillis)
        } else null

        val wordCount = lastUserMsg?.text?.trim()?.split("\\s+".toRegex())?.filter { it.isNotBlank() }?.size ?: 0
        val mirroringInstruction = when {
            wordCount in 1..5 -> "[Keseimbangan Chat: Pesan terakhir user sangat singkat ($wordCount kata). Balas HANYA 1 bubble pendek (5-12 kata), padat, santai, ceplas-ceplos, dan jangan selalu nanya balik!]"
            wordCount in 6..20 -> "[Keseimbangan Chat: Pesan user sedang. Balas 1 atau maksimal 2 bubble santai yang proporsional.]"
            wordCount > 20 -> "[Keseimbangan Chat: User sedang bercerita/curhat panjang. Tanggapi dengan 2-3 bubble hangat dan empati.]"
            else -> null
        }

        val lastUserIndex = contents.indexOfLast { it.role == "user" }
        if (lastUserIndex != -1) {
            var userText = contents[lastUserIndex].parts.firstOrNull()?.text.orEmpty()
            if (timeGapContext != null) {
                userText = "[Jeda Waktu: Obrolan berlanjut setelah jeda $timeGapContext dari pesan sebelumnya]\n$userText"
            }
            if (quotedMessage != null) {
                userText = "[Membalas ${quotedMessage.senderName}: \"${quotedMessage.text}\"]\n$userText"
            }
            if (mirroringInstruction != null) {
                userText = "$mirroringInstruction\n$userText"
            }
            contents[lastUserIndex] = GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userText))
            )
        }

        val requestBodyDto = GeminiRequestDto(
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = LiaPromptDefinition.buildSystemInstruction(userProfile)))
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

    private fun formatTimeGap(gapMillis: Long): String? {
        val minutes = gapMillis / (60 * 1000L)
        return when {
            minutes < 45 -> null
            minutes < 60 -> "sekitar 45 menit"
            minutes < 120 -> "sekitar 1 jam"
            minutes < 24 * 60 -> "sekitar ${minutes / 60} jam"
            minutes < 48 * 60 -> "kemarin (sekitar 1 hari)"
            else -> "beberapa hari"
        }
    }

    private fun sanitizeBubbles(bubbles: List<String>): List<String> {
        val timePrefixRegex = Regex("""^\[\d{1,2}:\d{2}\]\s*""")
        return bubbles
            .map { it.replace(timePrefixRegex, "").trim() }
            .filter { it.isNotBlank() }
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
        val trimmed = raw.trim()
        val bubbles = if (trimmed.isNotBlank()) listOf(trimmed) else listOf("...")
        return AiResponse(
            bubbles = bubbles,
            statusSubtitle = "online"
        )
    }
}

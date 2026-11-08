package com.example.aifriendship.data.remote.gemini.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiSystemInstruction(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float = 0.85f,
    val topP: Float = 0.95f,
    val responseMimeType: String = "application/json"
)

@Serializable
data class GeminiRequestDto(
    @SerialName("system_instruction")
    val systemInstruction: GeminiSystemInstruction? = null,
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

@Serializable
data class GeminiCandidateContent(
    val parts: List<GeminiPart> = emptyList(),
    val role: String? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiCandidateContent? = null,
    val finishReason: String? = null
)

@Serializable
data class GeminiResponseDto(
    val candidates: List<GeminiCandidate> = emptyList()
)


package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AiResponse(
    val bubbles: List<String>,
    val statusSubtitle: String
)


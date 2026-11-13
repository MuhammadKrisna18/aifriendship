package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AiResponse(
    val bubbles: List<String>,
    val statusSubtitle: String,
    val mood: String? = null,
    val activityUpdate: String? = null,
    val relationshipUpdate: String? = null,
    val newFactLearned: String? = null
) {
    val parsedMood: CompanionMood?
        get() = mood?.let { m ->
            when (m.trim().uppercase()) {
                "GEMAS" -> CompanionMood.NGAMBEK
                "PEDULI" -> CompanionMood.CEMBURU
                "TENANG" -> CompanionMood.CUEK
                "SEMANGAT" -> CompanionMood.MARAH
                else -> CompanionMood.entries.firstOrNull {
                    it.name.equals(m.trim(), ignoreCase = true) || it.label.equals(m.trim(), ignoreCase = true)
                }
            }
        }

    val parsedRelationship: CompanionRelationship?
        get() = relationshipUpdate?.let { r ->
            CompanionRelationship.entries.firstOrNull { it.name.equals(r.trim(), ignoreCase = true) || it.title.equals(r.trim(), ignoreCase = true) }
        }
}


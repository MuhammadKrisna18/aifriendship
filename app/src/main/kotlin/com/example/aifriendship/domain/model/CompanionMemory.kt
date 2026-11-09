package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CompanionMemory(
    val lastActivity: String? = null,
    val activityTimestamp: Long? = null,
    val currentRelationship: CompanionRelationship = CompanionRelationship.TEMAN_BIASA,
    val currentMood: CompanionMood = CompanionMood.BIASA,
    val affinityScore: Int = 20,
    val userFacts: List<String> = emptyList(),
    val lastProactiveMessageTimestamp: Long? = null
)

package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CompanionMemory(
    val lastActivity: String? = null,
    val activityTimestamp: Long? = null,
    val currentRelationship: CompanionRelationship = CompanionRelationship.ORANG_ASING,
    val currentMood: CompanionMood = CompanionMood.BIASA,
    val affinityScore: Int = 20,
    val userFacts: List<String> = emptyList(),
    val lastProactiveMessageTimestamp: Long? = null,
    val consecutiveProactiveCount: Int = 0,
    val pendingFarewellActivity: String? = null
)

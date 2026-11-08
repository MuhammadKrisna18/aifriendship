package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CommunicationVibe(val label: String, val description: String) {
    BARBAR("🌶️ Santai & Barbar", "santai, ceplas-ceplos, suka bercanda kocak dan gaya tongkrongan"),
    CHILL("☕ Kalem & Chill", "kalem, hangat, santai, dan penuh empati"),
    ABSURD("🤪 Campur Aduk", "kocak, absurd, random, penuh humor tak terduga tapi tetap asik")
}

@Serializable
data class UserProfile(
    val name: String = "",
    val vibe: CommunicationVibe = CommunicationVibe.BARBAR,
    val isOnboardingCompleted: Boolean = false
)

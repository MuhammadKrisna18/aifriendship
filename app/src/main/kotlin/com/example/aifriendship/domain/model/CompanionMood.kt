package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CompanionMood(
    val label: String,
    val iconEmoji: String
) {
    SENANG("Senang", "😊"),
    BIASA("Biasa", "☕"),
    SALTING("Salting", "🫣"),
    BAPER("Baper", "🥺"),
    MANJA("Manja", "🌸"),
    KANGEN("Kangen", "💭"),
    BANGGA("Bangga", "✨"),
    KHAWATIR("Khawatir", "🩺"),
    GEMAS("Gemas", "🥰"),
    PEDULI("Peduli", "💚"),
    TENANG("Tenang", "🍃"),
    MELOW("Melow", "🌙"),
    SEMANGAT("Semangat", "🔥");

    val emoji: String
        get() = iconEmoji

    val title: String
        get() = label

    val maxProactiveSpamCount: Int
        get() = when (this) {
            MANJA, KANGEN, GEMAS, PEDULI, KHAWATIR, SEMANGAT -> 3
            SALTING, SENANG, BAPER, MELOW, BANGGA -> 2
            BIASA, TENANG -> 1
        }
}

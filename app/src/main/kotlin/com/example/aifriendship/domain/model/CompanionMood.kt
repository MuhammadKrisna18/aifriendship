package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CompanionMood(
    val label: String,
    val iconEmoji: String
) {
    SENANG("Senang", "😊"),
    BIASA("Biasa", "☕"),
    KEPO("Kepo", "🧐"),
    SALTING("Salting", "🫣"),
    BAPER("Baper", "🥺"),
    MANJA("Manja", "🌸"),
    KANGEN("Kangen", "💭"),
    BANGGA("Bangga", "✨"),
    KHAWATIR("Khawatir", "🩺"),
    JAHIL("Jahil", "😜"),
    NGAMBEK("Gemas", "🥰"),
    CEMBURU("Peduli", "💚"),
    CUEK("Tenang", "🍃"),
    MELOW("Melow", "🌙"),
    MARAH("Semangat", "🔥");

    val emoji: String
        get() = iconEmoji

    val title: String
        get() = label

    val maxProactiveSpamCount: Int
        get() = when (this) {
            MANJA, KANGEN, KEPO, NGAMBEK, CEMBURU, KHAWATIR, MARAH -> 3
            JAHIL, SALTING, SENANG, BAPER, MELOW, BANGGA -> 2
            BIASA, CUEK -> 1
        }
}

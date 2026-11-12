package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CompanionTraitCategory(val label: String) {
    CASUAL("Santai & Gaul"),
    WARM("Hangat & Nyaman"),
    SPICY("Spicy & Unik"),
    SMART("Cerdas & Produktif")
}

@Serializable
enum class CompanionTrait(
    val title: String,
    val defaultName: String,
    val category: CompanionTraitCategory
) {
    BARBAR(
        title = "🌶️ Barbar",
        defaultName = "Lia",
        category = CompanionTraitCategory.CASUAL
    ),
    CHILL(
        title = "☕ Chill",
        defaultName = "Rania",
        category = CompanionTraitCategory.CASUAL
    ),
    ABSURD(
        title = "🤪 Absurd",
        defaultName = "Chika",
        category = CompanionTraitCategory.CASUAL
    ),
    SOFT(
        title = "🧸 Soft-Spoken",
        defaultName = "Naya",
        category = CompanionTraitCategory.WARM
    ),
    CHEERFUL(
        title = "🌸 Ceria",
        defaultName = "Aurel",
        category = CompanionTraitCategory.WARM
    ),
    MATERNAL(
        title = "🏡 Keibuan",
        defaultName = "Maya",
        category = CompanionTraitCategory.WARM
    ),
    TSUNDERE(
        title = "💅 Tsundere",
        defaultName = "Clara",
        category = CompanionTraitCategory.SPICY
    ),
    SAVAGE(
        title = "👑 Savage",
        defaultName = "Valerie",
        category = CompanionTraitCategory.SPICY
    ),
    INDIE(
        title = "🌙 Indie",
        defaultName = "Senja",
        category = CompanionTraitCategory.SPICY
    ),
    SMART(
        title = "🧠 Cerdas",
        defaultName = "Nadya",
        category = CompanionTraitCategory.SMART
    ),
    AMBITIOUS(
        title = "💼 Ambisius",
        defaultName = "Tasya",
        category = CompanionTraitCategory.SMART
    ),
    CUSTOM(
        title = "✏️ Kustom Sendiri",
        defaultName = "Lia",
        category = CompanionTraitCategory.CASUAL
    );

    val cleanTitle: String
        get() = title.replace(Regex("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}]+\\s*"), "").trim()
}

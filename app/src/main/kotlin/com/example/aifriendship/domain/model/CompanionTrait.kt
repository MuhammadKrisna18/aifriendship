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
        title = "🌶️ Ceplas-ceplos & Barbar",
        defaultName = "Lia",
        category = CompanionTraitCategory.CASUAL
    ),
    CHILL(
        title = "☕ Kalem & Santai",
        defaultName = "Rania",
        category = CompanionTraitCategory.CASUAL
    ),
    ABSURD(
        title = "🤪 Kocak & Absurd",
        defaultName = "Chika",
        category = CompanionTraitCategory.CASUAL
    ),
    PENDIAM(
        title = "🤫 Pendiam & Irit Bicara",
        defaultName = "Dina",
        category = CompanionTraitCategory.CASUAL
    ),
    GAMER(
        title = "🎮 Gamer & Pop Culture",
        defaultName = "Gita",
        category = CompanionTraitCategory.CASUAL
    ),
    SOFT(
        title = "🧸 Lembut & Empatis",
        defaultName = "Naya",
        category = CompanionTraitCategory.WARM
    ),
    CHEERFUL(
        title = "🌸 Ceria & Antusias",
        defaultName = "Aurel",
        category = CompanionTraitCategory.WARM
    ),
    MATERNAL(
        title = "🏡 Keibuan & Perhatian",
        defaultName = "Maya",
        category = CompanionTraitCategory.WARM
    ),
    MANJA(
        title = "🐱 Manja & Gemas",
        defaultName = "Manda",
        category = CompanionTraitCategory.WARM
    ),
    SETIA(
        title = "✨ Tulus & Suportif",
        defaultName = "Tiara",
        category = CompanionTraitCategory.WARM
    ),
    TSUNDERE(
        title = "💅 Tsundere & Gengsian",
        defaultName = "Clara",
        category = CompanionTraitCategory.SPICY
    ),
    SAVAGE(
        title = "👑 Savage & Dingin",
        defaultName = "Valerie",
        category = CompanionTraitCategory.SPICY
    ),
    INDIE(
        title = "🌙 Puitis & Filosofis",
        defaultName = "Senja",
        category = CompanionTraitCategory.SPICY
    ),
    JAHIL(
        title = "😈 Jahil & Penggoda",
        defaultName = "Zoya",
        category = CompanionTraitCategory.SPICY
    ),
    MISTERIUS(
        title = "🔮 Misterius & Tenang",
        defaultName = "Luna",
        category = CompanionTraitCategory.SPICY
    ),
    SMART(
        title = "🧠 Cerdas & Analitis",
        defaultName = "Nadya",
        category = CompanionTraitCategory.SMART
    ),
    AMBITIOUS(
        title = "💼 Tegas & Ambisius",
        defaultName = "Tasya",
        category = CompanionTraitCategory.SMART
    ),
    BIJAK(
        title = "📚 Bijaksana & Dewasa",
        defaultName = "Hana",
        category = CompanionTraitCategory.SMART
    ),
    REALISTIS(
        title = "📐 Kritis & Realistis",
        defaultName = "Sofi",
        category = CompanionTraitCategory.SMART
    ),
    CUSTOM(
        title = "✏️ Kustom Sendiri",
        defaultName = "Lia",
        category = CompanionTraitCategory.CASUAL
    );

    val cleanTitle: String
        get() = title.replace(Regex("^[^\\p{L}\\p{N}]+\\s*"), "").trim()
}

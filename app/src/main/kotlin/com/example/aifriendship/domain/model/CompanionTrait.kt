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
    PENDIAM(
        title = "🤫 Pendiam",
        defaultName = "Dina",
        category = CompanionTraitCategory.CASUAL
    ),
    GAMER(
        title = "🎮 Gamer",
        defaultName = "Gita",
        category = CompanionTraitCategory.CASUAL
    ),
    SOFT(
        title = "🧸 Lembut",
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
    MANJA(
        title = "🐱 Manja",
        defaultName = "Manda",
        category = CompanionTraitCategory.WARM
    ),
    SETIA(
        title = "✨ Tulus",
        defaultName = "Tiara",
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
        title = "🌙 Puitis",
        defaultName = "Senja",
        category = CompanionTraitCategory.SPICY
    ),
    JAHIL(
        title = "😈 Jahil",
        defaultName = "Zoya",
        category = CompanionTraitCategory.SPICY
    ),
    MISTERIUS(
        title = "🔮 Misterius",
        defaultName = "Luna",
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
    BIJAK(
        title = "📚 Bijak",
        defaultName = "Hana",
        category = CompanionTraitCategory.SMART
    ),
    REALISTIS(
        title = "📐 Kritis",
        defaultName = "Sofi",
        category = CompanionTraitCategory.SMART
    ),
    CUSTOM(
        title = "✏️ Kustom",
        defaultName = "Lia",
        category = CompanionTraitCategory.CASUAL
    );

    val cleanTitle: String
        get() = title.replace(Regex("^[^\\p{L}\\p{N}]+\\s*"), "").trim()
}

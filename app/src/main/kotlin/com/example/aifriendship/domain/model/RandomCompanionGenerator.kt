package com.example.aifriendship.domain.model

import kotlin.random.Random

object RandomCompanionGenerator {

    private val FEMALE_NAMES = listOf(
        "Lia",
        "Naya",
        "Clara",
        "Nadya",
        "Chika",
        "Rania",
        "Aurel",
        "Maya",
        "Valerie",
        "Senja",
        "Tasya",
        "Tiara",
        "Ziva",
        "Fisya",
        "Vira",
        "Sherly"
    )

    private val AVATAR_COLORS = listOf(
        "#6366F1",
        "#EC4899",
        "#8B5CF6",
        "#10B981",
        "#F59E0B",
        "#06B6D4",
        "#F43F5E",
        "#3B82F6"
    )

    fun generateRandom(random: Random = Random.Default): AiCompanion {
        val selectableTraits = CompanionTrait.entries.filter { it != CompanionTrait.CUSTOM }
        val randomTrait = selectableTraits[random.nextInt(selectableTraits.size)]
        val namePool = listOf(randomTrait.defaultName) + FEMALE_NAMES
        val randomName = namePool[random.nextInt(namePool.size)]
        val randomColor = AVATAR_COLORS[random.nextInt(AVATAR_COLORS.size)]

        return AiCompanion(
            name = randomName,
            trait = randomTrait,
            customTraitDescription = "",
            avatarColorHex = randomColor
        )
    }
}


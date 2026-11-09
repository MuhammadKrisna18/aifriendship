package com.example.aifriendship.domain.model

import java.util.UUID
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
        val numTraits = if (random.nextBoolean()) 2 else 1
        val pickedTraits = selectableTraits.shuffled(random).take(numTraits)
        val primaryTrait = pickedTraits.first()
        val namePool = listOf(primaryTrait.defaultName) + FEMALE_NAMES
        val randomName = namePool[random.nextInt(namePool.size)]
        val randomColor = AVATAR_COLORS[random.nextInt(AVATAR_COLORS.size)]

        return AiCompanion(
            id = UUID.randomUUID().toString(),
            name = randomName,
            trait = primaryTrait,
            traits = pickedTraits,
            customTraitDescription = "",
            avatarColorHex = randomColor
        )
    }
}

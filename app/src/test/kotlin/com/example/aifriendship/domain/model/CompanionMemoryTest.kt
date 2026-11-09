package com.example.aifriendship.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CompanionMemoryTest {

    @Test
    fun defaultMemory_hasExpectedDefaults() {
        val memory = CompanionMemory()
        assertNull(memory.lastActivity)
        assertNull(memory.activityTimestamp)
        assertEquals(CompanionRelationship.TEMAN_BIASA, memory.currentRelationship)
        assertEquals(CompanionMood.BIASA, memory.currentMood)
        assertEquals(20, memory.affinityScore)
        assertEquals(0, memory.userFacts.size)
    }

    @Test
    fun companion_delegatesCurrentRelationshipAndMoodFromMemory() {
        val customMemory = CompanionMemory(
            lastActivity = "Lagi kuis kalkulus",
            activityTimestamp = 1000L,
            currentRelationship = CompanionRelationship.PACAR,
            currentMood = CompanionMood.SALTING,
            affinityScore = 80,
            userFacts = listOf("Suka kopi latte")
        )
        val companion = AiCompanion(
            name = "Rina",
            memory = customMemory
        )

        assertEquals(CompanionRelationship.PACAR, companion.currentRelationship)
        assertEquals(CompanionMood.SALTING, companion.currentMood)
        assertEquals("Lagi kuis kalkulus", companion.memory.lastActivity)
        assertEquals(1, companion.memory.userFacts.size)
    }

    @Test
    fun relationshipEnum_hasAllCategoriesAndInstructions() {
        assertEquals(12, CompanionRelationship.entries.size)
        CompanionRelationship.entries.forEach { rel ->
            assertNotNull(rel.title)
            assertNotNull(rel.iconEmoji)
            assertNotNull(rel.description)
            assertNotNull(rel.defaultToneInstruction)
        }
    }

    @Test
    fun moodEnum_hasExpectedMoodsAndSubtitles() {
        assertEquals(15, CompanionMood.entries.size)
        CompanionMood.entries.forEach { mood ->
            assertNotNull(mood.label)
            assertNotNull(mood.iconEmoji)
            assertNotNull(mood.defaultSubtitle)
        }
    }
}


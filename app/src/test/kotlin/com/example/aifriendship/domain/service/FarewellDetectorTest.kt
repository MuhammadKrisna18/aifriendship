package com.example.aifriendship.domain.service

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FarewellDetectorTest {

    @Test
    fun detectFarewell_returnsNull_deferringToAiContext() {
        val result = FarewellDetector.detectFarewell("Aku mau mandi dulu ya")
        assertNull(result)
    }

    @Test
    fun suggestDelayMinutes_returnsBetween15And45() {
        for (i in 1..20) {
            val delay = FarewellDetector.suggestDelayMinutes("mandi")
            assertTrue(delay in 15..45)
        }
    }

    @Test
    fun suggestRapidDelayMinutes_returnsBetween2And5() {
        for (i in 1..20) {
            val delay = FarewellDetector.suggestRapidDelayMinutes()
            assertTrue(delay in 2..5)
        }
    }
}


package com.example.aifriendship.domain.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FarewellDetectorTest {

    @Test
    fun detectFarewell_mandi_returnsMandi() {
        val result = FarewellDetector.detectFarewell("Aku mau mandi dulu ya")
        assertEquals("mandi", result)
    }

    @Test
    fun detectFarewell_makan_returnsMakan() {
        val result = FarewellDetector.detectFarewell("Lagi makan siang bentar")
        assertEquals("makan", result)
    }

    @Test
    fun detectFarewell_otw_returnsPerjalanan() {
        val result = FarewellDetector.detectFarewell("Gue otw ya")
        assertEquals("perjalanan", result)
    }

    @Test
    fun detectFarewell_nugas_returnsNugas() {
        val result = FarewellDetector.detectFarewell("Mau nugas dulu nih banyak banget")
        assertEquals("nugas", result)
    }

    @Test
    fun detectFarewell_regularChat_returnsNull() {
        val result = FarewellDetector.detectFarewell("Hari ini seru banget ya ngobrol sama kamu")
        assertNull(result)
    }

    @Test
    fun suggestDelayMinutes_mandi_returnsBetween10And25() {
        for (i in 1..20) {
            val delay = FarewellDetector.suggestDelayMinutes("mandi")
            assertTrue(delay in 10..25)
        }
    }

    @Test
    fun suggestRapidDelayMinutes_returnsBetween1And5() {
        for (i in 1..20) {
            val delay = FarewellDetector.suggestRapidDelayMinutes()
            assertTrue(delay in 1..5)
        }
    }
}

package com.example.aifriendship.domain.service

import kotlin.random.Random

object FarewellDetector {

    fun detectFarewell(text: String): String? = null

    fun suggestDelayMinutes(activity: String? = null): Long {
        return Random.nextLong(15, 46)
    }

    fun suggestRapidDelayMinutes(): Long {
        return Random.nextLong(2, 6)
    }
}


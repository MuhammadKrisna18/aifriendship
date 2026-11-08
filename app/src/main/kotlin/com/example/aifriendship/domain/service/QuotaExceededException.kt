package com.example.aifriendship.domain.service

class QuotaExceededException(
    override val message: String = "Quota limit reached",
    val httpCode: Int = 429
) : Exception(message)


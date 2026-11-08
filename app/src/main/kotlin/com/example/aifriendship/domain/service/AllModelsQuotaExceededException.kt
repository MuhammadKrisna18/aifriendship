package com.example.aifriendship.domain.service

class AllModelsQuotaExceededException(
    override val message: String = "All fallback models quota limit reached",
    val httpCode: Int = 429
) : Exception(message)


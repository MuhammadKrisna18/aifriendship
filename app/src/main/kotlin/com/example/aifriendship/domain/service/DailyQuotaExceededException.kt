package com.example.aifriendship.domain.service

class DailyQuotaExceededException(
    message: String = "Jatah kuota harian Gemini telah habis",
    val statusCode: Int = 429
) : Exception(message)

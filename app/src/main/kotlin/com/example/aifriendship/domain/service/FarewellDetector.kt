package com.example.aifriendship.domain.service

import java.util.Locale
import kotlin.random.Random

object FarewellDetector {

    fun detectFarewell(text: String): String? {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            lower.contains("mandi") -> "mandi"
            lower.contains("makan") || lower.contains("sarapan") || lower.contains("lunch") || lower.contains("dinner") -> "makan"
            lower.contains("otw") || lower.contains("jalan") || lower.contains("nyetir") || lower.contains("berangkat") -> "perjalanan"
            lower.contains("nugas") || lower.contains("tugas") || lower.contains("belajar") || lower.contains("kuliah") || lower.contains("sekolah") -> "nugas"
            lower.contains("kerja") || lower.contains("ngantor") || lower.contains("meeting") || lower.contains("rapat") -> "kerja"
            lower.contains("tidur") || lower.contains("bobo") || lower.contains("merem") -> "tidur"
            lower.contains("sholat") || lower.contains("ibadah") -> "ibadah"
            lower.contains("dulu ya") || lower.contains("duluan ya") || lower.contains("bentar ya") ||
                    lower.contains("bentar ada") || lower.contains("cabut dulu") || lower.contains("pergi dulu") -> "aktivitas"
            else -> null
        }
    }

    fun suggestDelayMinutes(activity: String?): Long {
        return when (activity) {
            "mandi", "makan", "ibadah" -> Random.nextLong(10, 26)
            "perjalanan", "nugas", "kerja" -> Random.nextLong(25, 61)
            "tidur" -> Random.nextLong(60, 181)
            "aktivitas" -> Random.nextLong(10, 31)
            else -> Random.nextLong(10, 61)
        }
    }

    fun suggestRapidDelayMinutes(): Long {
        return Random.nextLong(1, 6)
    }
}

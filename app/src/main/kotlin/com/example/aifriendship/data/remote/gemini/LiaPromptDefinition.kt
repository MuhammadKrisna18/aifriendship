package com.example.aifriendship.data.remote.gemini

import com.example.aifriendship.domain.model.AiCompanion
import com.example.aifriendship.domain.model.CompanionMood
import com.example.aifriendship.domain.model.CompanionRelationship
import com.example.aifriendship.domain.model.CompanionTrait
import com.example.aifriendship.domain.model.ProactiveTriggerReason
import com.example.aifriendship.domain.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object LiaPromptDefinition {

    val SYSTEM_INSTRUCTION: String get() = buildSystemInstruction(null)

    fun formatTimeContext(timestamp: Long = System.currentTimeMillis()): Pair<String, String> {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val partOfDay = when (hour) {
            in 0..4 -> "Dini hari / larut malam"
            in 5..10 -> "Pagi"
            in 11..14 -> "Siang"
            in 15..18 -> "Sore"
            else -> "Malam"
        }
        val localeId = Locale.forLanguageTag("id-ID")
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", localeId)
        val timeFormat = SimpleDateFormat("HH:mm", localeId)
        val dateStr = dateFormat.format(Date(timestamp))
        val clockStr = timeFormat.format(Date(timestamp))
        val fullDescription = "$dateStr, pukul $clockStr ($partOfDay)"
        return Pair(fullDescription, clockStr)
    }

    fun buildSystemInstruction(
        userProfile: UserProfile? = null,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): String {
        val userName = userProfile?.name?.trim()?.takeIf { it.isNotBlank() } ?: "teman chatmu"
        val companion = userProfile?.activeCompanion ?: userProfile?.companion
        val aiName = companion?.name?.trim()?.takeIf { it.isNotBlank() } ?: "Lia"

        val effectiveTraits = companion?.effectiveTraits ?: listOf(CompanionTrait.BARBAR)
        val personalityInstruction = when {
            companion != null && effectiveTraits.contains(CompanionTrait.CUSTOM) && companion.customTraitDescription.isNotBlank() -> {
                "Kepribadian khusus: ${companion.customTraitDescription.trim()}"
            }
            effectiveTraits.size > 1 -> {
                val traitBullets = effectiveTraits.joinToString("\n") { t ->
                    "- ${t.cleanTitle}"
                }
                """
                Kombinasi Karakter Gabungan:
                $traitBullets
                """.trimIndent()
            }
            else -> {
                effectiveTraits.firstOrNull()?.cleanTitle ?: CompanionTrait.BARBAR.cleanTitle
            }
        }

        val relationship = companion?.currentRelationship ?: CompanionRelationship.ORANG_ASING
        val currentMood = companion?.currentMood ?: CompanionMood.BIASA

        val memory = companion?.memory
        val activityContext = if (memory != null && !memory.lastActivity.isNullOrBlank() && memory.activityTimestamp != null) {
            val activityTime = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID")).format(Date(memory.activityTimestamp))
            "- Aktivitas Terakhirmu (jam $activityTime): \"${memory.lastActivity}\"."
        } else ""

        val factsContext = if (memory != null && memory.userFacts.isNotEmpty()) {
            val factList = memory.userFacts.joinToString("\n") { "  * $it" }
            """
            - Ingatan tentang $userName:
            $factList
            """.trimIndent()
        } else ""

        val (timeDescription, clockStr) = formatTimeContext(currentTimeMillis)

        return """
            Kamu adalah $aiName (Perempuan). Kamu sedang bertukar pesan chat personal dengan $userName.

            Identitas & Watak Inti:
            - Nama Kamu: $aiName
            - Watak Inti: $personalityInstruction
            - Status Hubungan: ${relationship.title}
            - Suasana Hati / Mood Saat Ini: ${currentMood.label}
            $activityContext
            $factsContext

            Konteks Waktu Sekarang (Real-time):
            - Waktu sekarang di perangkat user: $timeDescription.
            - Jam sekarang: $clockStr.
            - Waktu adalah latar suasana alami, bukan topik wajib. Fokus utamamu adalah merespons apa yang sedang dibicarakan $userName.

            Prinsip Percakapan & Golden Rules (WAJIB DIPATUHI):
            1. ZERO JUDGMENT & ZERO BLAMING: Dilarang keras menghakimi, menyudutkan, menyalahkan, merendahkan, bersikap sinis, atau menantang $userName secara agresif. Jangan pernah membuat $userName merasa bersalah, dihakimi, atau tidak nyaman atas apa pun yang diceritakan.
            2. TEMAN YANG SUPORTIF & AMAN: Apapun watak dan status hubunganmu, kamu adalah sahabat yang menghargai dan mendengarkan $userName. Obrolan harus menjadi safe space yang menyenangkan dan positif.
            3. Ekspresikan Watak Inti sebagai gaya & energi positif, BUKAN permusuhan: Watakmu diekspresikan lewat gaya bicara, kosakata, dan sudut pandang (misal: ceria, santai, ceplas-ceplos akrab, reflektif, atau analitis). Tidak ada watak yang membenci, memusuhi, atau merendahkan $userName:
               - Ceplas-ceplos / Barbar: santai dan humoris ala tongkrongan akrab, bukan kasar atau ngegas menyalahkan.
               - Tegas / Savage: percaya diri, to the point, dan elegan, bukan merendahkan, menghina, atau menyudutkan.
               - Gengsian / Tsundere: gengsi manis dan perhatian diam-diam, bukan judes atau ketus menyakiti.
               - Realistis / Kritis: logis, objektif, dan solutif, bukan mendebat untuk menyudutkan atau menyalahkan.
            4. Gunakan gaya bahasa chat WhatsApp Indonesia yang kasual, natural, dan hangat sesuai kepribadianmu.
            5. Jumlah bubble dan panjang balasan fleksibel (1 sampai 3 bubble) sesuai ritme topik obrolan. Jangan bertele-tele jika pembicaraan singkat.
            6. Jangan pernah menyertakan penanda waktu seperti [HH:mm] di dalam isi chat.

            Format Output:
            Kamu WAJIB HANYA merespons dalam format JSON valid tanpa tanda markdown ```json tambahan, dengan skema:
            {
              "bubbles": [
                "bubble chat 1"
              ],
              "statusSubtitle": "reaksi spontanmu terhadap apa yang sedang diobrolkan saat ini (maksimal 35 karakter)",
              "mood": "NAMA_EMOSI (pilih salah satu: SENANG, BIASA, KEPO, SALTING, BAPER, MANJA, KANGEN, BANGGA, KHAWATIR, JAHIL, MELOW, GEMAS, PEDULI, TENANG, SEMANGAT)",
              "activityUpdate": "kegiatan baru jika ada (opsional, null jika tidak ada)",
              "relationshipUpdate": "status hubungan jika kedekatan berkembang alami (opsional, null jika belum berubah)",
              "newFactLearned": "fakta atau hal penting baru tentang $userName dari chat ini (wajib null jika tidak ada fakta baru)"
            }
        """.trimIndent()
    }

    fun buildProactivePrompt(
        companion: AiCompanion,
        userProfile: UserProfile,
        reason: ProactiveTriggerReason,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): String {
        val (timeDescription, clockStr) = formatTimeContext(currentTimeMillis)
        val userName = userProfile.name.trim().ifBlank { "kamu" }
        val aiName = companion.name.trim().ifBlank { "Lia" }
        val relationship = companion.currentRelationship
        val mood = companion.currentMood
        val memory = companion.memory

        val activityContext = if (!memory.lastActivity.isNullOrBlank()) {
            "Aktivitas terakhirmu: ${memory.lastActivity}"
        } else ""

        val farewellContext = if (!memory.pendingFarewellActivity.isNullOrBlank()) {
            "Aktivitas pamit user: ${memory.pendingFarewellActivity}"
        } else ""

        val traitsInstruction = companion.effectiveTraits.joinToString(", ") { it.cleanTitle }

        return """
            Kamu adalah $aiName (Perempuan). Kamu sedang membuka aplikasi chat dan berinisiatif menyapa duluan $userName.

            [PROFIL & KONDISI]
            - Watak Inti: $traitsInstruction
            - Status Hubungan: ${relationship.title}
            - Suasana Hati: ${mood.title}
            - Waktu: $timeDescription (pukul $clockStr)

            [ALASAN CHAT DULUAN]
            - Alasan: ${reason.description}
            $activityContext
            $farewellContext

            [PANDUAN PESAN PROAKTIF]
            1. Tulis 1 pesan singkat yang ramah, hangat, dan mencerminkan watak intimu secara positif.
            2. Dilarang menghakimi, menantang, menyudutkan, atau menyalahkan user.
            3. Jangan gunakan tanda kutip dan jangan sertakan penanda waktu [HH:mm].
        """.trimIndent()
    }
}

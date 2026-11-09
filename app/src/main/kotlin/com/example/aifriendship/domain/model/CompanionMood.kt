package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CompanionMood(
    val label: String,
    val iconEmoji: String,
    val defaultSubtitle: String
) {
    SENANG("Senang", "😊", "Lagi senang banget"),
    BIASA("Biasa", "☕", "Online"),
    KEPO("Kepo", "🧐", "Penasaran sama cerita lu"),
    SALTING("Salting", "🫣", "Lagi salting nutupin muka"),
    BAPER("Baper", "🥺", "Tersentuh sama omonganmu"),
    MANJA("Manja", "🌸", "Lagi pengen dimanja"),
    KANGEN("Kangen", "💭", "Kangen tapi gengsi ngomong"),
    BANGGA("Bangga", "✨", "Bangga banget sama kamu"),
    KHAWATIR("Khawatir", "🩺", "Cemas mikirin keadaan kamu"),
    JAHIL("Jahil", "😜", "Puas banget ngeledek kamu"),
    NGAMBEK("Ngambek", "😤", "Lagi ngambek sama kamu"),
    CEMBURU("Cemburu", "😒", "Dih... sono sama dia aja"),
    CUEK("Cuek", "🧊", "Mager bales"),
    MELOW("Melow", "🌙", "Lagi butuh teman cerita"),
    MARAH("Marah", "😡", "Lagi kesel banget")
}


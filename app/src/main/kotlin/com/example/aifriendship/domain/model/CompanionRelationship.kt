package com.example.aifriendship.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CompanionRelationship(val title: String) {
    ORANG_ASING("Orang Asing / Baru Kenal"),
    TEMAN_BIASA("Teman Biasa"),
    TEMAN_AKRAB("Teman Akrab"),
    SAHABAT("Sahabat Dekat"),
    GEBETAN("Gebetan / PDKT"),
    PACAR("Pacar / Kekasih"),
    HTS("Teman Tapi Mesra"),
    MANTAN("Mantan Pacar"),
    RIVAL("Rival / Musuh Akrab"),
    TEMAN_CURHAT("Teman Curhat"),
    TEMAN_MABAR("Teman Mabar"),
    SENIOR("Senior / Kakak Tingkat"),
    JUNIOR("Junior / Adik Tingkat")
}

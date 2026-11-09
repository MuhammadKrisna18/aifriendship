package com.example.aifriendship.domain.model

enum class ProactiveTriggerReason(val description: String) {
    UNANSWERED_PING("User belum membalas pesan terakhir dari AI setelah beberapa jam"),
    FOLLOW_UP_TOPIC("AI menanyakan kelanjutan hal penting atau kegiatan yang sebelumnya diceritakan oleh user"),
    ACTIVITY_RETURN("AI kembali menyapa setelah menyelesaikan aktivitas pribadinya"),
    RANDOM_THOUGHT("AI sedang senggang atau melamun dan ingin menyapa serta membagikan obrolan santai secara spontan")
}


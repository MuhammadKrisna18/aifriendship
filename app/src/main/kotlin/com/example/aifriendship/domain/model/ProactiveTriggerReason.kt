package com.example.aifriendship.domain.model

enum class ProactiveTriggerReason(val description: String) {
    UNANSWERED_PING("User belum membalas pesan terakhir dari AI setelah beberapa jam"),
    RAPID_FOLLOW_UP("User tiba-tiba menghilang atau belum membalas chat dalam beberapa menit saat sedang mengobrol"),
    USER_FAREWELL_FOLLOWUP("AI mengecek kembali kabar user setelah user sebelumnya pamit beraktivitas seperti mandi, makan, otw, atau nugas"),
    MOOD_SPAM("AI mengirim pesan susulan atau spam chat beruntun karena gemas, kangen, ngambek, atau manja chatnya belum dibalas"),
    FOLLOW_UP_TOPIC("AI menanyakan kelanjutan hal penting atau kegiatan yang sebelumnya diceritakan oleh user"),
    ACTIVITY_RETURN("AI kembali menyapa setelah menyelesaikan aktivitas pribadinya"),
    RANDOM_THOUGHT("AI sedang senggang atau melamun dan ingin menyapa serta membagikan obrolan santai secara spontan")
}


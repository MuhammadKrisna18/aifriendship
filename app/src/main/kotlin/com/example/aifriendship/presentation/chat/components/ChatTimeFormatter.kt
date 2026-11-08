package com.example.aifriendship.presentation.chat.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChatTimeFormatter {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun format(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
}


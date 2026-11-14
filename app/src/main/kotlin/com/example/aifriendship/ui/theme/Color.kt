package com.example.aifriendship.ui.theme

import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xFF6366F1)
val primaryContainerLight = Color(0xFF818CF8)
val secondaryLight = Color(0xFF64748B)
val tertiaryLight = Color(0xFF06B6D4)

val primaryDark = Color(0xFF818CF8)
val primaryContainerDark = Color(0xFF3730A3)
val secondaryDark = Color(0xFF94A3B8)
val tertiaryDark = Color(0xFF67E8F9)

val UserBubbleColor = Color(0xFF6366F1)
val UserBubbleGradientEnd = Color(0xFF06B6D4)
val UserBubbleText = Color(0xFFFFFFFF)

val AiBubbleBackground = Color(0xF2FFFFFF)
val AiBubbleBorder = Color(0x33CBD5E1)
val AiBubbleText = Color(0xFF0F172A)

val ChatCanvasBackground = Color.Transparent
val ChatTimestampColor = Color(0xFF64748B)
val ChatInputBackground = Color(0xF2FFFFFF)
val ChatInputBorder = Color(0x4DCBD5E1)

val OnlineBadgeGreen = Color(0xFF10B981)
val OnlineBadgeBackground = Color(0xFFD1FAE5)

fun parseHexColor(hex: String, fallback: Color = primaryLight): Color {
    if (hex.isBlank()) return fallback
    return runCatching {
        val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(cleanHex))
    }.getOrDefault(fallback)
}

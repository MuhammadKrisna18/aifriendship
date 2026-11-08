package com.example.aifriendship.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aifriendship.domain.model.QuotedMessage

@Composable
fun QuotedMessageView(
    quotedMessage: QuotedMessage,
    isUserBubble: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isUserBubble) {
        Color(0x33000000)
    } else {
        Color(0xFFF1F1F1)
    }

    val barColor = if (isUserBubble) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF8E8E93)
    }

    val textColor = if (isUserBubble) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF555555)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(barColor)
        )

        Text(
            text = "${quotedMessage.senderName} : ${quotedMessage.text}",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}


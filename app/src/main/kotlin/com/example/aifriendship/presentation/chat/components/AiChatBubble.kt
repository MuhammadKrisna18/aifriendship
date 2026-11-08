package com.example.aifriendship.presentation.chat.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.ui.theme.AiBubbleBackground
import com.example.aifriendship.ui.theme.AiBubbleBorder
import com.example.aifriendship.ui.theme.AiBubbleText
import com.example.aifriendship.ui.theme.ChatTimestampColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AiChatBubble(
    message: ChatMessage,
    companionName: String = "Lia",
    avatarColorHex: String = "#6366F1",
    avatarUrl: String = "",
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        LiaAvatar(
            name = companionName,
            avatarColorHex = avatarColorHex,
            imageUrl = avatarUrl,
            size = 34.dp,
            modifier = Modifier.align(Alignment.Bottom)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 265.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                .background(AiBubbleBackground)
                .border(1.dp, AiBubbleBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (message.quotedMessage != null) {
                    QuotedMessageView(
                        quotedMessage = message.quotedMessage,
                        isUserBubble = false
                    )
                }

                Text(
                    text = message.text,
                    color = AiBubbleText,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ChatTimeFormatter.format(message.timestamp),
                        color = ChatTimestampColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

package com.example.aifriendship.presentation.chat.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aifriendship.domain.model.ChatMessage
import com.example.aifriendship.domain.model.MessageDeliveryStatus
import com.example.aifriendship.ui.theme.ChatTimestampColor
import com.example.aifriendship.ui.theme.UserBubbleColor
import com.example.aifriendship.ui.theme.UserBubbleGradientEnd
import com.example.aifriendship.ui.theme.UserBubbleText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserChatBubble(
    message: ChatMessage,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        val maxBubbleWidth = minOf((LocalConfiguration.current.screenWidthDp * 0.80f).dp, 400.dp)

        Box(
            modifier = Modifier
                .widthIn(max = maxBubbleWidth)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(UserBubbleColor, UserBubbleGradientEnd)
                    )
                )
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                message.quotedMessage?.let {
                    QuotedMessageView(
                        quotedMessage = it,
                        isUserBubble = true,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Text(
                    text = message.text,
                    color = UserBubbleText,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ChatTimeFormatter.format(message.timestamp),
                        color = UserBubbleText.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                    if (message.status == MessageDeliveryStatus.READ || message.status == MessageDeliveryStatus.SENT) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = UserBubbleText.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        UserAvatar(
            size = 32.dp,
            modifier = Modifier.align(Alignment.Bottom)
        )
    }
}

package com.example.aifriendship.presentation.chat.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatar(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = size.toPx() / 2f
        val center = Offset(radius, radius)

        drawCircle(
            color = Color(0xFFE0E7FF),
            radius = radius,
            center = center
        )

        val eyeWidth = radius * 0.22f
        val eyeHeight = radius * 0.15f
        val eyeY = radius * 0.95f
        val leftEyeX = radius * 0.72f
        val rightEyeX = radius * 1.28f

        drawArc(
            color = Color(0xFF4338CA),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(leftEyeX - eyeWidth / 2f, eyeY - eyeHeight / 2f),
            size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        drawArc(
            color = Color(0xFF4338CA),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(rightEyeX - eyeWidth / 2f, eyeY - eyeHeight / 2f),
            size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        drawCircle(
            color = Color(0xFFA5B4FC),
            radius = radius * 0.14f,
            center = Offset(radius * 0.55f, radius * 1.15f)
        )
        drawCircle(
            color = Color(0xFFA5B4FC),
            radius = radius * 0.14f,
            center = Offset(radius * 1.45f, radius * 1.15f)
        )
    }
}

package com.example.aifriendship.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.aifriendship.R

@Composable
fun AnimeThemeBackground(
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0.80f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_anime_theme),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A).copy(alpha = 0.12f),
                            Color(0xFFF8FAFC).copy(alpha = scrimAlpha),
                            Color(0xFFF1F5F9).copy(alpha = (scrimAlpha + 0.06f).coerceAtMost(0.95f))
                        )
                    )
                )
        )
        content()
    }
}

package com.example.aifriendship

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey
@Serializable data object Setup : NavKey
@Serializable data class Chat(val companionId: String = "") : NavKey

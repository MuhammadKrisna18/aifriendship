package com.example.aifriendship

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aifriendship.presentation.chat.ChatScreen
import com.example.aifriendship.presentation.home.HomeScreen
import com.example.aifriendship.presentation.setup.CharacterSetupScreen

@Composable
fun MainNavigation(initialCompanionId: String? = null) {
    val context = LocalContext.current
    val container = (context.applicationContext as AiFriendshipApplication).container

    val initialKey = Home
    val backStack = rememberNavBackStack(initialKey)

    LaunchedEffect(initialCompanionId) {
        if (!initialCompanionId.isNullOrBlank()) {
            container.userRepository.setActiveCompanion(initialCompanionId)
            backStack.add(Chat(initialCompanionId))
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onOpenChat = { companionId ->
                        backStack.add(Chat(companionId))
                    },
                    onCreateCompanion = {
                        backStack.add(Setup)
                    }
                )
            }
            entry<Setup> {
                CharacterSetupScreen(
                    onComplete = {
                        backStack.clear()
                        backStack.add(Home)
                    },
                    onBackClick = { backStack.removeLastOrNull() }
                )
            }
            entry<Chat> {
                ChatScreen(
                    onBackClick = { backStack.removeLastOrNull() },
                    onChangeCompanionClick = {
                        backStack.add(Setup)
                    }
                )
            }
        }
    )
}

package com.example.aifriendship

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aifriendship.presentation.chat.ChatScreen
import com.example.aifriendship.presentation.home.HomeScreen
import com.example.aifriendship.presentation.setup.CharacterSetupScreen

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val container = (context.applicationContext as AiFriendshipApplication).container
    val profileStateFlow = container.userRepository.observeUserProfile()
    val userProfile by profileStateFlow.collectAsStateWithLifecycle()

    val hasCreatedCharacter = profileStateFlow.value.isOnboardingCompleted ||
            profileStateFlow.value.companions.isNotEmpty() ||
            profileStateFlow.value.name.isNotBlank()

    val initialKey = if (hasCreatedCharacter) Home else Setup
    val backStack = rememberNavBackStack(initialKey)

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
                    onBackClick = if (hasCreatedCharacter && backStack.size > 1) {
                        { backStack.removeLastOrNull() }
                    } else null
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

package com.example.aifriendship

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aifriendship.domain.model.UserProfile
import com.example.aifriendship.presentation.chat.ChatScreen
import com.example.aifriendship.presentation.home.HomeScreen
import com.example.aifriendship.presentation.setup.CharacterSetupScreen

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val container = (context.applicationContext as AiFriendshipApplication).container
    val userProfile by container.userRepository.observeUserProfile().collectAsStateWithLifecycle(
        initialValue = UserProfile()
    )

    val initialKey = if (userProfile.isOnboardingCompleted) Home else Setup
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
                    }
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

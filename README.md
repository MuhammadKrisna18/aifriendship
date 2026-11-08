# AiFriendship 💬✨

A modern Native Android AI Companion app built with **Kotlin** and **Jetpack Compose**. Designed to deliver a natural, emotionally expressive, and truly human-like conversation experience inspired by everyday messaging apps.

---

## 📱 Features

- **Human-like Chat Cadence:** Messages are broken down into natural, successive chat bubbles with realistic typing delays instead of monolithic text walls.
- **Dynamic Persona Status:** Real-time subtitle updates reflecting Lia's mood, activities, and reactions (e.g., *"gemas juga ngajarin dia ngoding"*, *"capek banget punya teman suka lupa"*).
- **Quoted / Reply Message:** Long-press to quote and reply to specific messages with visual parent-message previews.
- **Two-Layer Anti-Stall System:**
  - *Layer 1 (Silent Failover):* Seamlessly falls back to secondary models (e.g., Gemini 2.0 Flash / Flash-8B) if the primary model reaches rate limits (HTTP 429).
  - *Layer 2 (In-Character Pause):* Emits a humorous, roleplayed human excuse (e.g., *"Bentar ya woy, emak nyuruh beli garem ke warung..."*) with header status updates and an automatic retry timer if all limits are exhausted.
- **Offline-First Persistence:** Thread-safe, non-blocking local storage ensuring complete message histories persist across application restarts.
- **In-App Auto-Update:** Checks GitHub Releases on launch or via menu, downloading and installing APK updates directly on the phone without manual PC flashing.

---

## 🏛️ Architecture & Tech Stack

This project strictly adheres to **Clean Architecture** and **SOLID principles** while maintaining clean, self-explanatory code without superfluous comments:

```
com.example.aifriendship/
├── domain/
│   ├── model/           # ChatMessage, LiaProfile, DeliveryStatus, QuotedMessage, AiResponse, AppUpdateInfo
│   ├── service/         # AiService, AppUpdateService, QuotaExceededException, AllModelsQuotaExceededException
│   └── repository/      # ChatRepository interface
├── data/
│   ├── config/          # ApiKeyProvider & LocalApiKeyProvider
│   ├── local/           # ChatLocalDataSource & JsonChatLocalDataSource
│   ├── remote/
│   │   ├── gemini/      # GeminiAiService, DTOs & LiaPromptDefinition
│   │   └── update/      # GitHubUpdateService & GitHubReleaseDto
│   └── repository/      # DefaultChatRepository implementation
├── presentation/
│   └── chat/            # ChatScreen, ChatViewModel, ChatUiState
│       └── components/  # ChatTopBar, UserChatBubble, AiChatBubble,
│                        # ChatBottomBar, TypingIndicator, UserAvatar, LiaAvatar
├── di/                  # AppContainer (Dependency Injection without overengineering)
└── ui/theme/            # Theme, Color, Type definitions
```

### Technologies

- **Language:** Kotlin 2.3+
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture Pattern:** MVVM with reactive `StateFlow`
- **Asynchronous:** Kotlin Coroutines & Flow
- **Navigation:** AndroidX Navigation 3
- **Network & Serialization:** OkHttp, Kotlinx Serialization JSON
- **Image Loading:** Coil Compose
- **Build System:** Gradle 9.x with Android Gradle Plugin 9.x

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug / Meerkat or later
- JDK 17
- Android SDK Platform 34+ (target SDK 37)

### Build & Run

Clone the repository and build using Gradle:

```bash
git clone https://github.com/MuhammadKrisna18/aifriendship.git
cd aifriendship
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

Install directly to a connected Android device or emulator:
```bash
./gradlew installDebug
```

Run unit tests:

```bash
./gradlew test
```

### Initial Setup in App

1. Launch the app on your Android device.
2. Tap the **Settings icon** (⚙️) on the top-right corner.
3. Select **Pengaturan API Key** and paste your Google AI Studio (Gemini) API Key.
4. Start chatting naturally with Lia!

---

## 📄 License

This project is personal and open-source under the MIT License.


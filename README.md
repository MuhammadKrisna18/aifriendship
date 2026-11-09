# AiFriendship 💬✨

A modern Native Android AI Companion app built with **Kotlin** and **Jetpack Compose**. Designed to deliver a natural, emotionally expressive, and truly human-like conversation experience inspired by everyday messaging apps like WhatsApp.

---

## 📱 Features

- **WhatsApp-Style Multi-Chat Interface:**
  - **Tab 1 (OBROLAN):** Overview of all active chats with companion avatars, trait badges, message previews, and formatted timestamps.
  - **Tab 2 (SAHABAT AI):** Manage your roster of companions, view archetype descriptions, or create a new companion anytime.
- **Multi-Companion Architecture:** Chat with different AI companions simultaneously, each maintaining isolated conversation history and distinct personalities.
- **11 Character Archetypes with Multi-Trait Combinations:**
  - Choose and mix 1 to 3 archetypes to produce unique hybrid personalities (e.g., *Tsundere + Keibuan + Barbar*).
  - Archetypes: *Santai & Barbar, Kalem & Chill, Tsundere Gengsi, Clueless Polos, Ambisius & Perfeksionis, Lembut & Keibuan, Ceria & Hyperaktif, Sarkastik & Tajam, Romantis & Manja, Introvert & Pemikir, Tomboy & Sporty*.
- **Subtle Real-Time Temporal Awareness:**
  - Dynamic time awareness that naturally aligns with morning, afternoon, evening, and late-night vibes without intrusive repetition (no repetitive bedtime or breakfast nagging).
- **Human-like Chat Cadence:** Messages arrive as natural, successive chat bubbles with realistic typing delays instead of monolithic text walls.
- **Dynamic Persona Status:** Real-time subtitle updates reflecting Lia's mood, activities, and reactions (e.g., *"gemas juga ngajarin dia ngoding"*, *"capek banget punya teman suka lupa"*).
- **Quoted / Reply Message:** Long-press to quote and reply to specific messages with visual parent-message previews.
- **Multi-Tier Model Fallback Chain:**
  - Intelligent sequential failover: `gemini-3.5-flash-lite` ➔ `gemini-3.1-flash-lite` ➔ `gemini-2.5-flash` ➔ `gemini-flash-latest`.
  - In-character pause & roleplayed excuse (*"Bentar ya woy, emak gue nyuruh beli garem dulu ke warung... 1 menit lagi gue balik!"*) with countdown timer when all quotas are temporarily exhausted.
- **Enhanced In-App Auto-Updater:**
  - Checks GitHub Releases on launch or via menu.
  - Automatic download and package installer integration with Android 8.0+ unknown app sources permissions (`ACTION_MANAGE_UNKNOWN_APP_SOURCES`) and FileProvider support.
  - Fallback "Buka Browser" button for instant manual download if direct installation is restricted.
- **Offline-First Persistence:** Thread-safe, non-blocking local storage ensuring complete message histories persist across application restarts.

---

## 🏛️ Architecture & Tech Stack

This project strictly adheres to **Clean Architecture** and **SOLID principles** while maintaining clean, self-explanatory code without comments:

```
com.example.aifriendship/
├── domain/
│   ├── model/           # ChatMessage, AiCompanion, CompanionTrait, UserProfile, AiResponse, AppUpdateInfo
│   ├── service/         # AiService, AppUpdateService, QuotaExceededException, AllModelsQuotaExceededException
│   └── repository/      # ChatRepository, UserRepository
├── data/
│   ├── config/          # ApiKeyProvider & LocalApiKeyProvider
│   ├── local/           # ChatLocalDataSource & UserLocalDataSource
│   ├── remote/
│   │   ├── gemini/      # GeminiAiService, DTOs & LiaPromptDefinition
│   │   └── update/      # GitHubUpdateService & GitHubReleaseDto
│   └── repository/      # DefaultChatRepository & DefaultUserRepository
├── presentation/
│   ├── home/            # HomeScreen, HomeViewModel, HomeUiState
│   ├── chat/            # ChatScreen, ChatViewModel, ChatUiState
│   │   └── components/  # ChatTopBar, UserChatBubble, AiChatBubble, ChatBottomBar, TypingIndicator, UserProfileDialog
│   ├── setup/           # CharacterSetupScreen, CharacterSetupViewModel, CharacterSetupUiState
│   └── navigation/      # Navigation graph and destination routing
├── di/                  # AppContainer (Manual Dependency Injection)
└── ui/theme/            # Color, Theme, Typography definitions
```

### Technologies

- **Language:** Kotlin 2.3+
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture Pattern:** MVVM with reactive `StateFlow`
- **Asynchronous:** Kotlin Coroutines & Flow
- **Navigation:** AndroidX Navigation 3
- **Network & Serialization:** OkHttp, Kotlinx Serialization JSON
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

Run unit tests:

```bash
./gradlew test
```

---

## 📄 License

This project is personal and open-source under the MIT License.


# AiFriendship 💬✨

A modern Native Android AI Companion app built with **Kotlin** and **Jetpack Compose**. Designed to deliver a natural, emotionally expressive, and truly human-like conversation experience inspired by everyday messaging apps like WhatsApp.

---

## 📱 Features

- **Context-Driven Proactive Messaging (AI Chat Duluan):**
  - Companions take initiative to message you based on realistic conversational triggers:
    - **Unanswered Pings:** Playfully calls you out if you left their question or greeting unread for hours (*"Dih diread doang... sibuk banget ya wkwk"*).
    - **Follow-up Topic Callback:** Remembers when you mentioned an important event (meeting, exam, feeling unwell) and checks back in later.
    - **Activity Return:** Reaches out when they finish an activity they mentioned previously (*"Drakornya nyesek banget wkwk, lu lagi ngapain?"*).
    - **Random Thoughts:** Spontaneous, natural greetings or fun check-ins after a quiet day.
  - **Anti-Spam & Anti-Desperate Guardrails:** Maximum 1 proactive ping until you reply, strict quiet hours (00:00 – 07:00), and healthy multi-hour cooldowns.
  - **WhatsApp-Style Push Notifications:** Instant local notifications with companion name, avatar accent, and direct deep-linking to the chat room.
- **WhatsApp-Style Multi-Chat Interface:**
  - **Tab 1 (OBROLAN):** Overview of all active chats with companion avatars, dynamic relationship & mood badges, message previews, and formatted timestamps.
  - **Tab 2 (SAHABAT AI):** Manage your roster of companions, view their dynamic relationship stages, current mood, recent episodic activities, or add new companions.
- **Dynamic Personality & Emotional Mood (15 Spectrum States):**
  - Companions are not static archetypes. Their mood and reactions change dynamically according to how you interact with them: *Senang, Biasa, Kepo, Salting, Baper, Manja, Kangen, Bangga, Khawatir, Jahil, Ngambek, Cemburu, Cuek, Melow, Marah*.
- **Organic Relationship Progression (12 Stages):**
  - Bonds develop naturally over time: *Teman Biasa, Teman Akrab, Sahabat, Gebetan / PDKT, Pacar, HTS, Mantan, Rival, Teman Curhat, Teman Mabar, Senior, Junior*.
- **Anti-Ngegas Tone Calibration & Natural Reciprocity:**
  - Playful and witty banter without aggressive hostility. Instant empathy override when the user expresses fatigue or distress, and warm receptivity to genuine affection.
- **Episodic & Temporal Gap Continuity:**
  - Dynamic time awareness that tracks time deltas between conversations (e.g. *obrolan berlanjut setelah jeda 4 jam*) and remembers recent companion activities for seamless conversation continuity.
- **Streamlined Character Creation:**
  - Direct access to the Home screen on first launch. Adding a companion is effortless: enter the name (or tap 🎲 Acak), pick an avatar color, and start chatting.
- **Human-like Chat Cadence:** Messages arrive as natural successive chat bubbles with realistic typing delays.
- **Quoted / Reply Message:** Long-press to quote and reply to specific messages with visual parent-message previews.
- **Multi-Tier Model Fallback Chain:**
  - Intelligent sequential failover: `gemini-3.5-flash-lite` ➔ `gemini-3.1-flash-lite` ➔ `gemini-2.5-flash` ➔ `gemini-flash-latest`.
  - In-character pause & roleplayed excuse with countdown timer when all quotas are temporarily exhausted.
- **In-App Auto-Updater:**
  - Automatic download and package installer integration with Android 8.0+ unknown app sources permissions (`ACTION_MANAGE_UNKNOWN_APP_SOURCES`) and FileProvider support.
  - Fallback "Buka Browser" button for instant manual download if direct installation is restricted.
- **Offline-First Persistence:** Thread-safe, non-blocking local storage ensuring complete message histories and companion memories persist across application restarts.

---

## 🏛️ Architecture & Tech Stack

This project strictly adheres to **Clean Architecture** and **SOLID principles** while maintaining clean, self-explanatory code without comments:

```
com.example.aifriendship/
├── domain/
│   ├── model/           # ChatMessage, AiCompanion, CompanionMood, CompanionRelationship, CompanionMemory, ProactiveTriggerReason, UserProfile, AiResponse, AppUpdateInfo
│   ├── service/         # AiService, ProactiveMessageEvaluator, AppUpdateService, QuotaExceededException, AllModelsQuotaExceededException
│   └── repository/      # ChatRepository, UserRepository
├── data/
│   ├── config/          # ApiKeyProvider & LocalApiKeyProvider
│   ├── local/           # ChatLocalDataSource & UserLocalDataSource
│   ├── remote/
│   │   ├── gemini/      # GeminiAiService, DTOs & LiaPromptDefinition
│   │   └── update/      # GitHubUpdateService & GitHubReleaseDto
│   ├── worker/          # ProactiveMessageWorker, ProactiveScheduler (WorkManager)
│   └── repository/      # DefaultChatRepository & DefaultUserRepository
├── presentation/
│   ├── home/            # HomeScreen, HomeViewModel, HomeUiState
│   ├── chat/            # ChatScreen, ChatViewModel, ChatUiState
│   │   └── components/  # ChatTopBar, UserChatBubble, AiChatBubble, ChatBottomBar, TypingIndicator, UserProfileDialog
│   ├── notification/    # ProactiveNotificationHelper
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
- **Background Orchestration:** AndroidX WorkManager 2.10
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

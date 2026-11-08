package com.example.aifriendship.data.local

import android.content.Context
import com.example.aifriendship.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class JsonUserLocalDataSource(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }
) : UserLocalDataSource {

    private val mutex = Mutex()
    private val profileFile by lazy { File(context.filesDir, "user_profile.json") }
    private val _userProfileFlow = MutableStateFlow(UserProfile())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val loaded = readProfileFromFile()
        _userProfileFlow.value = loaded
    }

    override fun observeUserProfile(): Flow<UserProfile> = _userProfileFlow.asStateFlow()

    override suspend fun getUserProfile(): UserProfile = mutex.withLock {
        _userProfileFlow.value
    }

    override suspend fun saveUserProfile(profile: UserProfile): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            _userProfileFlow.value = profile
            try {
                val content = json.encodeToString(profile)
                profileFile.writeText(content)
            } catch (_: Exception) {
            }
        }
    }

    private fun readProfileFromFile(): UserProfile {
        return try {
            if (profileFile.exists()) {
                val content = profileFile.readText()
                if (content.isNotBlank()) {
                    json.decodeFromString<UserProfile>(content)
                } else {
                    UserProfile()
                }
            } else {
                UserProfile()
            }
        } catch (_: Exception) {
            UserProfile()
        }
    }
}


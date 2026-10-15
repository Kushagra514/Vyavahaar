package org.vyavahaar.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vyavahaar_preferences")

class PreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        val KEY_PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        val KEY_VOICE_SPEED = floatPreferencesKey("voice_speed")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[KEY_AUTH_TOKEN] }
    val userName: Flow<String> = context.dataStore.data.map { it[KEY_USER_NAME] ?: "Sadasya" }
    val userPhone: Flow<String> = context.dataStore.data.map { it[KEY_USER_PHONE] ?: "" }
    val preferredLanguage: Flow<String> = context.dataStore.data.map { it[KEY_PREFERRED_LANGUAGE] ?: "hi" }
    val fontScale: Flow<Float> = context.dataStore.data.map { it[KEY_FONT_SCALE] ?: 1.2f }
    val voiceSpeed: Flow<Float> = context.dataStore.data.map { it[KEY_VOICE_SPEED] ?: 0.9f }
    val themeMode: Flow<String> = context.dataStore.data.map { it[KEY_THEME_MODE] ?: "light" }
    val isOnboarded: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_ONBOARDED] ?: false }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    suspend fun saveUserProfile(name: String, phone: String, isOnboarded: Boolean) {
        context.dataStore.edit {
            it[KEY_USER_NAME] = name
            it[KEY_USER_PHONE] = phone
            it[KEY_IS_ONBOARDED] = isOnboarded
        }
    }

    suspend fun saveAccessibilitySettings(language: String, fontScale: Float, voiceSpeed: Float, themeMode: String) {
        context.dataStore.edit {
            it[KEY_PREFERRED_LANGUAGE] = language
            it[KEY_FONT_SCALE] = fontScale
            it[KEY_VOICE_SPEED] = voiceSpeed
            it[KEY_THEME_MODE] = themeMode
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(KEY_AUTH_TOKEN)
            it[KEY_IS_ONBOARDED] = false
        }
    }
}

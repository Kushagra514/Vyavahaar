package org.vyavahaar.app.data.repository

import kotlinx.coroutines.flow.firstOrNull
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.ApiClient
import org.vyavahaar.app.data.remote.VoiceCommandRequestDto
import org.vyavahaar.app.data.remote.VoiceCommandResponseDto
import org.vyavahaar.app.data.remote.VyavahaarApiService

class AssistantRepository(
    private val api: VyavahaarApiService,
    private val preferences: PreferencesDataStore
) {
    suspend fun processVoiceCommand(transcript: String): Result<VoiceCommandResponseDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val lang = preferences.preferredLanguage.firstOrNull() ?: "hi"
            val response = api.processCommand(
                "Bearer $token",
                VoiceCommandRequestDto(transcript, lang)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to process voice command"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package org.vyavahaar.app.data.repository

import kotlinx.coroutines.flow.firstOrNull
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.EventDto
import org.vyavahaar.app.data.remote.EventRegistrationResponseDto
import org.vyavahaar.app.data.remote.VyavahaarApiService

class EventsRepository(
    private val api: VyavahaarApiService,
    private val preferences: PreferencesDataStore
) {
    suspend fun getEvents(category: String? = null): Result<List<EventDto>> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.getEvents("Bearer $token", category)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load events"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerForEvent(eventId: Int): Result<EventRegistrationResponseDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.registerForEvent("Bearer $token", eventId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to register for event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unregisterFromEvent(eventId: Int): Result<Unit> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.unregisterFromEvent("Bearer $token", eventId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to cancel event registration"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

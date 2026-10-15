package org.vyavahaar.app.data.repository

import kotlinx.coroutines.flow.firstOrNull
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.ContactDto
import org.vyavahaar.app.data.remote.VyavahaarApiService

class ContactsRepository(
    private val api: VyavahaarApiService,
    private val preferences: PreferencesDataStore
) {
    suspend fun getContacts(): Result<List<ContactDto>> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.getContacts("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load contacts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

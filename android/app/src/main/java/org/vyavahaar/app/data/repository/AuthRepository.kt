package org.vyavahaar.app.data.repository

import kotlinx.coroutines.flow.firstOrNull
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.*

class AuthRepository(
    private val api: VyavahaarApiService,
    private val preferences: PreferencesDataStore
) {
    suspend fun requestOtp(phone: String): Result<PhoneOtpResponseDto> {
        return try {
            val response = api.requestOtp(PhoneOtpRequestDto(phone))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to request OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<TokenResponseDto> {
        return try {
            val response = api.verifyOtp(OtpVerifyRequestDto(phone, otp))
            if (response.isSuccessful && response.body() != null) {
                val tokenDto = response.body()!!
                preferences.saveAuthToken(tokenDto.accessToken)
                preferences.saveUserProfile(
                    name = "Sadasya",
                    phone = tokenDto.phoneNumber,
                    isOnboarded = tokenDto.isOnboarded
                )
                Result.success(tokenDto)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "OTP verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String, language: String, fontScale: Float, themeMode: String): Result<ProfileDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: return Result.failure(Exception("Not logged in"))
            val response = api.updateProfile(
                "Bearer $token",
                ProfileUpdateDto(
                    name = name,
                    preferredLanguage = language,
                    fontScale = fontScale,
                    voiceSpeed = 0.9f,
                    themeMode = themeMode,
                    isOnboarded = true
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val p = response.body()!!
                preferences.saveUserProfile(name = p.name ?: name, phone = p.phoneNumber, isOnboarded = true)
                preferences.saveAccessibilitySettings(language, fontScale, 0.9f, themeMode)
                Result.success(p)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Profile update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        preferences.clearSession()
    }
}

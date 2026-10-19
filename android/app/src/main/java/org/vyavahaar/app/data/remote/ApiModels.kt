package org.vyavahaar.app.data.remote

import com.google.gson.annotations.SerializedName

data class PhoneOtpRequestDto(
    @SerializedName("phone_number") val phoneNumber: String
)

data class PhoneOtpResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("cooldown_seconds") val cooldownSeconds: Int,
    @SerializedName("dev_otp") val devOtp: String?
)

data class OtpVerifyRequestDto(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("otp_code") val otpCode: String
)

data class TokenResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("is_onboarded") val isOnboarded: Boolean,
    @SerializedName("phone_number") val phoneNumber: String
)

data class ProfileDto(
    @SerializedName("id") val id: Int,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("name") val name: String?,
    @SerializedName("preferred_language") val preferredLanguage: String,
    @SerializedName("font_scale") val fontScale: Float,
    @SerializedName("voice_speed") val voiceSpeed: Float,
    @SerializedName("theme_mode") val themeMode: String,
    @SerializedName("city") val city: String,
    @SerializedName("is_onboarded") val isOnboarded: Boolean
)

data class ProfileUpdateDto(
    @SerializedName("name") val name: String?,
    @SerializedName("preferred_language") val preferredLanguage: String?,
    @SerializedName("font_scale") val fontScale: Float?,
    @SerializedName("voice_speed") val voiceSpeed: Float?,
    @SerializedName("theme_mode") val themeMode: String?,
    @SerializedName("is_onboarded") val isOnboarded: Boolean?
)

data class VoiceCommandRequestDto(
    @SerializedName("transcript") val transcript: String,
    @SerializedName("detected_language") val detectedLanguage: String? = "hi"
)

data class AllowlistedToolCallDto(
    @SerializedName("tool_name") val toolName: String,
    @SerializedName("parameters") val parameters: Map<String, Any> = emptyMap(),
    @SerializedName("requires_confirmation") val requiresConfirmation: Boolean = false,
    @SerializedName("confirmation_prompt") val confirmationPrompt: String? = null
)

data class VoiceCommandResponseDto(
    @SerializedName("recognized_text") val recognizedText: String,
    @SerializedName("spoken_feedback") val spokenFeedback: String,
    @SerializedName("intent") val intent: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("tool_call") val toolCall: AllowlistedToolCallDto?,
    @SerializedName("needs_clarification") val needsClarification: Boolean,
    @SerializedName("clarification_options") val clarificationOptions: List<String> = emptyList()
)

data class EventDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("venue_name") val venueName: String,
    @SerializedName("address") val address: String?,
    @SerializedName("date_time") val dateTime: String,
    @SerializedName("is_online") val isOnline: Boolean,
    @SerializedName("online_meeting_url") val onlineMeetingUrl: String?,
    @SerializedName("organizer_name") val organizerName: String,
    @SerializedName("is_registered") val isRegistered: Boolean
)

data class EventRegistrationResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class TransportRequestCreateDto(
    @SerializedName("request_type") val requestType: String,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("destination_address") val destinationAddress: String,
    @SerializedName("notes") val notes: String? = null
)

data class TransportDto(
    @SerializedName("id") val id: Int,
    @SerializedName("request_type") val requestType: String,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("destination_address") val destinationAddress: String,
    @SerializedName("status") val status: String,
    @SerializedName("fare_estimate") val fareEstimate: Float?,
    @SerializedName("driver_name") val driverName: String?,
    @SerializedName("driver_phone") val driverPhone: String?,
    @SerializedName("vehicle_model") val vehicleModel: String?,
    @SerializedName("vehicle_number") val vehicleNumber: String?,
    @SerializedName("emergency_direct_call_number") val emergencyDirectCallNumber: String?
)

data class AppointmentCreateDto(
    @SerializedName("doctor_name") val doctorName: String,
    @SerializedName("specialty") val specialty: String,
    @SerializedName("clinic_or_hospital") val clinicOrHospital: String,
    @SerializedName("appointment_time") val appointmentTime: String,
    @SerializedName("recurrence_rule") val recurrenceRule: String
)

data class AppointmentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("doctor_name") val doctorName: String,
    @SerializedName("specialty") val specialty: String,
    @SerializedName("clinic_or_hospital") val clinicOrHospital: String,
    @SerializedName("appointment_time") val appointmentTime: String,
    @SerializedName("recurrence_rule") val recurrenceRule: String,
    @SerializedName("status") val status: String
)

data class MedicineItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("strength") val strength: String?,
    @SerializedName("formulation") val formulation: String?,
    @SerializedName("instructions") val instructions: String?,
    @SerializedName("confidence") val confidence: Float
)

data class PrescriptionScanDto(
    @SerializedName("image_base64_or_url") val imageBase64OrUrl: String
)

data class PrescriptionResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("extracted_raw_text") val extractedRawText: String,
    @SerializedName("extracted_medicines") val extractedMedicines: List<MedicineItemDto>,
    @SerializedName("confidence_score") val confidenceScore: Float,
    @SerializedName("medical_safety_warning") val medicalSafetyWarning: String
)

data class MedicineAlternativeDto(
    @SerializedName("brand_name") val brandName: String,
    @SerializedName("active_ingredient") val activeIngredient: String,
    @SerializedName("strength") val strength: String,
    @SerializedName("dosage_form") val dosageForm: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("approximate_price_inr") val approximatePriceInr: Float,
    @SerializedName("clinical_note") val clinicalNote: String
)

data class MedicineSearchDto(
    @SerializedName("searched_query") val searchedQuery: String,
    @SerializedName("matched_salt") val matchedSalt: String,
    @SerializedName("active_ingredients") val activeIngredients: List<String>,
    @SerializedName("primary_uses") val primaryUses: List<String>,
    @SerializedName("verified_equivalents") val verifiedEquivalents: List<MedicineAlternativeDto>,
    @SerializedName("safety_disclaimer") val safetyDisclaimer: String
)

data class ContactDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("avatar_color") val avatarColor: String,
    @SerializedName("is_favorite") val isFavorite: Boolean
)

data class SosAlertRequestDto(
    @SerializedName("is_test_mode") val isTestMode: Boolean = false,
    @SerializedName("emergency_type") val emergencyType: String = "GENERAL_SOS",
    @SerializedName("location_latitude") val locationLatitude: Double = 12.9716,
    @SerializedName("location_longitude") val locationLongitude: Double = 77.5946,
    @SerializedName("location_name") val locationName: String = "Bengaluru"
)

data class SosAlertResponseDto(
    @SerializedName("alert_id") val alertId: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("emergency_type") val emergencyType: String,
    @SerializedName("is_test_mode") val isTestMode: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("direct_call_target") val directCallTarget: String,
    @SerializedName("notified_contacts_count") val notifiedContactsCount: Int,
    @SerializedName("notified_contacts") val notifiedContacts: List<String>,
    @SerializedName("alert_message_payload") val alertMessagePayload: String,
    @SerializedName("timestamp") val timestamp: String
)


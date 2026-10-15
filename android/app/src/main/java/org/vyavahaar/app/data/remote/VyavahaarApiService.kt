package org.vyavahaar.app.data.remote

import retrofit2.Response
import retrofit2.http.*

interface VyavahaarApiService {

    // Auth
    @POST("api/v1/auth/request-otp")
    suspend fun requestOtp(@Body req: PhoneOtpRequestDto): Response<PhoneOtpResponseDto>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body req: OtpVerifyRequestDto): Response<TokenResponseDto>

    // Profile & Settings
    @GET("api/v1/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileDto>

    @PATCH("api/v1/profile/")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body req: ProfileUpdateDto
    ): Response<ProfileDto>

    // Assistant NLU & Tool Execution
    @POST("api/v1/assistant/process-command")
    suspend fun processCommand(
        @Header("Authorization") token: String,
        @Body req: VoiceCommandRequestDto
    ): Response<VoiceCommandResponseDto>

    // Events
    @GET("api/v1/events/")
    suspend fun getEvents(
        @Header("Authorization") token: String,
        @Query("category") category: String? = null
    ): Response<List<EventDto>>

    @POST("api/v1/events/{id}/register")
    suspend fun registerForEvent(
        @Header("Authorization") token: String,
        @Path("id") eventId: Int
    ): Response<EventRegistrationResponseDto>

    // Transport & SOS
    @POST("api/v1/transport/request")
    suspend fun createTransportRequest(
        @Header("Authorization") token: String,
        @Body req: TransportRequestCreateDto
    ): Response<TransportDto>

    @POST("api/v1/transport/{id}/confirm")
    suspend fun confirmTransportBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<TransportDto>

    @POST("api/v1/transport/{id}/cancel")
    suspend fun cancelTransportBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<TransportDto>

    // Doctor Appointments
    @GET("api/v1/appointments/")
    suspend fun getAppointments(@Header("Authorization") token: String): Response<List<AppointmentDto>>

    @POST("api/v1/appointments/")
    suspend fun createAppointment(
        @Header("Authorization") token: String,
        @Body req: AppointmentCreateDto
    ): Response<AppointmentDto>

    // Prescriptions & Medicine Search
    @POST("api/v1/prescriptions/scan")
    suspend fun scanPrescription(
        @Header("Authorization") token: String,
        @Body req: PrescriptionScanDto
    ): Response<PrescriptionResponseDto>

    @GET("api/v1/prescriptions/medicines/search")
    suspend fun searchMedicine(@Query("q") query: String): Response<MedicineSearchDto>

    // Contacts
    @GET("api/v1/contacts/")
    suspend fun getContacts(@Header("Authorization") token: String): Response<List<ContactDto>>
}

package com.example.progr3ssapp.network

import com.example.progr3ssapp.model.AuthProfile
import com.example.progr3ssapp.model.AuthRequest
import com.example.progr3ssapp.model.AuthResponse
import com.example.progr3ssapp.model.AuthTokens
import com.example.progr3ssapp.model.ResetPasswordRequest
import com.example.progr3ssapp.model.UpdateProfileRequest
import com.example.progr3ssapp.network.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // =====================================
    // AUTH
    // =====================================

    @POST("auth/local/signin")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @Multipart
    @POST("auth/local/signup")
    suspend fun register(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part profileImage: MultipartBody.Part? = null
    ): Response<AuthResponse>

    // A refresh tokent a Authorization fejlécben kell elküldeni.
    @POST("auth/local/refresh")
    suspend fun refresh(
        @Header("Authorization") bearerRefreshToken: String
    ): Response<AuthTokens>

    // Ugyanez szinkron módon – az OkHttp Authenticator használja.
    @POST("auth/local/refresh")
    fun refreshSync(
        @Header("Authorization") bearerRefreshToken: String
    ): Call<AuthTokens>

    @POST("auth/local/logout")
    suspend fun logout(): Response<ResponseBody>

    @POST("auth/reset-password-via-email")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResponseBody>

    // =====================================
    // PROFILE
    // =====================================

    @GET("profile")
    suspend fun getProfile(): Response<AuthProfile>

    @PATCH("profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<AuthProfile>

    @Multipart
    @POST("profile/upload-profile-image")
    suspend fun uploadProfileImage(
        @Part profileImage: MultipartBody.Part
    ): Response<AuthProfile>

    // =====================================
    // HABIT
    // =====================================

    @GET("habit/categories")
    suspend fun getHabitCategories(): Response<List<HabitCategory>>

    @GET("habit")
    suspend fun getHabits(): Response<List<HabitResponse>>

    @GET("habit/user/{userId}")
    suspend fun getHabitsByUser(
        @Path("userId") userId: Long
    ): Response<List<HabitResponse>>

    @POST("habit")
    suspend fun createHabit(
        @Body request: CreateHabitRequest
    ): Response<HabitResponse>

    // =====================================
    // SCHEDULE
    // =====================================

    @GET("schedule/day")
    suspend fun getScheduleByDay(
        @Query("date") date: String
    ): Response<List<ScheduleResponse>>

    @GET("schedule/{id}")
    suspend fun getScheduleById(
        @Path("id") id: Long
    ): Response<ScheduleResponse>

    @POST("schedule/custom")
    suspend fun createCustomSchedule(
        @Body request: CreateCustomScheduleRequest
    ): Response<ScheduleResponse>

    @POST("schedule/recurring")
    suspend fun createRecurringSchedule(
        @Body request: CreateRecurringScheduleRequest
    ): Response<List<ScheduleResponse>>

    @POST("schedule/recurring/weekdays")
    suspend fun createWeekdaySchedule(
        @Body request: CreateWeekdayScheduleRequest
    ): Response<List<ScheduleResponse>>

    @PATCH("schedule/{id}")
    suspend fun updateSchedule(
        @Path("id") id: Long,
        @Body request: UpdateScheduleRequest
    ): Response<ScheduleResponse>

    @DELETE("schedule/{id}")
    suspend fun deleteSchedule(
        @Path("id") id: Long
    ): Response<Unit>

    // =====================================
    // PROGRESS
    // =====================================

    @POST("progress")
    suspend fun createProgress(
        @Body request: CreateProgressRequest
    ): Response<ProgressResponseDto>
}

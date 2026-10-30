
package com.example.progr3ssapp.network

import com.example.progr3ssapp.model.AuthRequest
import com.example.progr3ssapp.model.AuthResponse
import com.example.progr3ssapp.network.models.*

import okhttp3.MultipartBody
import okhttp3.RequestBody

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    // =====================================
    // 6. LABOR - AUTENTIKÁCIÓ
    // =====================================

    // Bejelentkezés
    @POST("auth/local/signin")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    // Regisztráció
    @Multipart
    @POST("auth/local/signup")
    suspend fun register(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part profileImage: MultipartBody.Part? = null
    ): Response<AuthResponse>


    // =====================================
    // 7. LABOR - HABIT
    // =====================================

    // A hét szokáskategória lekérése
    @GET("habit/categories")
    suspend fun getHabitCategories():
            Response<List<HabitCategory>>

    // A bejelentkezett felhasználó szokásai
    @GET("habit")
    suspend fun getHabits():
            Response<List<HabitResponse>>

    // Új szokás létrehozása
    @POST("habit")
    suspend fun createHabit(
        @Body request: CreateHabitRequest
    ): Response<HabitResponse>


    // =====================================
    // 7. LABOR - SCHEDULE
    // =====================================

    // Adott nap ütemezéseinek lekérése
    @GET("schedule/day")
    suspend fun getScheduleByDay(
        @Query("date") date: String
    ): Response<List<ScheduleResponse>>

    // Ismétlődő ütemezés létrehozása
    @POST("schedule/recurring")
    suspend fun createRecurringSchedule(
        @Body request: CreateRecurringScheduleRequest
    ): Response<List<ScheduleResponse>>

    // Egyszeri ütemezés létrehozása
    @POST("schedule/custom")
    suspend fun createCustomSchedule(
        @Body request: CreateCustomScheduleRequest
    ): Response<ScheduleResponse>
}

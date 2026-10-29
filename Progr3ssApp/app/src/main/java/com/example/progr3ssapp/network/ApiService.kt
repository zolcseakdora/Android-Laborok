
package com.example.progr3ssapp.network

import com.example.progr3ssapp.model.AuthRequest
import com.example.progr3ssapp.model.AuthResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    // BEJELENTKEZÉS
    // POST /auth/local/signin
    // A backend JSON-t vár
    @POST("auth/local/signin")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    // REGISZTRÁCIÓ
    // POST /auth/local/signup
    // A backend multipart/form-data-t vár
    @Multipart
    @POST("auth/local/signup")
    suspend fun register(
        @Part("username")
        username: RequestBody,

        @Part("email")
        email: RequestBody,

        @Part("password")
        password: RequestBody,

        // A profilkép nem kötelező
        @Part
        profileImage: MultipartBody.Part? = null
    ): Response<AuthResponse>
}

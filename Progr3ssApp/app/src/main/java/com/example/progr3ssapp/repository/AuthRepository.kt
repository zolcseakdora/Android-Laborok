
package com.example.progr3ssapp.repository

import com.example.progr3ssapp.model.AuthRequest
import com.example.progr3ssapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository {

    // Kapcsolat a backend API-val
    private val api = RetrofitClient.api

    // BEJELENTKEZÉS
    suspend fun login(
        email: String,
        password: String
    ) = api.login(
        AuthRequest(
            email = email,
            password = password
        )
    )

    // REGISZTRÁCIÓ
    suspend fun register(
        username: String,
        email: String,
        password: String
    ) = api.register(
        username = username.toRequestBody(
            "text/plain".toMediaType()
        ),

        email = email.toRequestBody(
            "text/plain".toMediaType()
        ),

        password = password.toRequestBody(
            "text/plain".toMediaType()
        )
    )
}

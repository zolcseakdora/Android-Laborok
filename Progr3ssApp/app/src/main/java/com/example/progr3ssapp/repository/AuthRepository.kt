package com.example.progr3ssapp.repository

import com.example.progr3ssapp.model.AuthProfile
import com.example.progr3ssapp.model.AuthRequest
import com.example.progr3ssapp.model.AuthResponse
import com.example.progr3ssapp.model.AuthTokens
import com.example.progr3ssapp.model.ResetPasswordRequest
import com.example.progr3ssapp.model.UpdateProfileRequest
import com.example.progr3ssapp.network.ApiService
import com.example.progr3ssapp.network.RetrofitClient
import com.example.progr3ssapp.network.bodyOrThrow
import com.example.progr3ssapp.network.ensureSuccess
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// ==========================================
// AUTH + PROFILE REPOSITORY
// ==========================================

class AuthRepository(
    private val api: ApiService = RetrofitClient.api
) {

    private val textType = "text/plain".toMediaType()

    // POST /auth/local/signin
    suspend fun login(email: String, password: String): Result<AuthResponse> =
        runCatching {
            api.login(AuthRequest(email = email, password = password))
                .bodyOrThrow("Sikertelen bejelentkezés")
        }

    // POST /auth/local/signup (multipart)
    suspend fun register(
        username: String,
        email: String,
        password: String
    ): Result<AuthResponse> =
        runCatching {
            api.register(
                username = username.toRequestBody(textType),
                email = email.toRequestBody(textType),
                password = password.toRequestBody(textType)
            ).bodyOrThrow("Sikertelen regisztráció")
        }

    // POST /auth/local/refresh
    suspend fun refresh(refreshToken: String): Result<AuthTokens> =
        runCatching {
            api.refresh("Bearer $refreshToken")
                .bodyOrThrow("Nem sikerült megújítani a munkamenetet")
        }

    // POST /auth/local/logout
    suspend fun logout(): Result<Unit> =
        runCatching {
            api.logout().ensureSuccess("Sikertelen kijelentkezés")
        }

    // POST /auth/reset-password-via-email
    suspend fun resetPassword(email: String): Result<Unit> =
        runCatching {
            api.resetPassword(ResetPasswordRequest(email))
                .ensureSuccess("Nem sikerült új jelszót kérni")
        }
}

class ProfileRepository(
    private val api: ApiService = RetrofitClient.api
) {

    // GET /profile
    suspend fun getProfile(): Result<AuthProfile> =
        runCatching {
            api.getProfile().bodyOrThrow("Nem sikerült lekérni a profilt")
        }

    // PATCH /profile
    suspend fun updateUsername(username: String): Result<AuthProfile> =
        runCatching {
            api.updateProfile(UpdateProfileRequest(username = username))
                .bodyOrThrow("Nem sikerült menteni a profilt")
        }

    // POST /profile/upload-profile-image
    suspend fun uploadProfileImage(
        bytes: ByteArray,
        mimeType: String,
        fileName: String
    ): Result<AuthProfile> =
        runCatching {
            val body = bytes.toRequestBody(mimeType.toMediaType())
            val part = MultipartBody.Part.createFormData(
                "profileImage",
                fileName,
                body
            )
            api.uploadProfileImage(part)
                .bodyOrThrow("Nem sikerült feltölteni a képet")
        }
}

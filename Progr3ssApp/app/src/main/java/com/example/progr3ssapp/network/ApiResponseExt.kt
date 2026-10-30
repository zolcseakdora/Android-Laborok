package com.example.progr3ssapp.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Response

data class ApiError(
    @SerializedName("message") val message: String?,
    @SerializedName("statusCode") val statusCode: Int?
)

internal fun <T> Response<T>.bodyOrThrow(fallbackMessage: String): T {
    if (isSuccessful) {
        return body() ?: error("Empty response from server")
    }
    error(parseErrorMessage(this, fallbackMessage))
}

private fun parseErrorMessage(response: Response<*>, fallbackMessage: String): String {
    val raw = response.errorBody()?.string()
    if (!raw.isNullOrBlank()) {
        val parsed = try {
            Gson().fromJson(raw, ApiError::class.java)
        } catch (e: Exception) {
            null
        }
        if (parsed?.message != null) {
            return parsed.message
        }
    }
    val message = response.message()
    return if (message.isNullOrBlank()) "$fallbackMessage (${response.code()})" else message
}

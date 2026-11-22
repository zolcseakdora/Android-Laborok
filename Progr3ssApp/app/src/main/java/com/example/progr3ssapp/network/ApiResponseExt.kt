package com.example.progr3ssapp.network

import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import java.io.IOException

data class ApiError(
    @SerializedName("message") val message: Any?,
    @SerializedName("statusCode") val statusCode: Int?
)

// Sikeres válasz esetén a törzs, különben érthető hibaüzenet.
internal fun <T> Response<T>.bodyOrThrow(fallbackMessage: String): T {
    if (isSuccessful) {
        return body() ?: error("Üres válasz a szervertől")
    }
    error(parseErrorMessage(this, fallbackMessage))
}

// Ha a törzs nem érdekes (pl. 204 No Content), csak a státuszt ellenőrizzük.
internal fun Response<*>.ensureSuccess(fallbackMessage: String) {
    if (!isSuccessful) {
        error(parseErrorMessage(this, fallbackMessage))
    }
}

internal fun parseErrorMessage(response: Response<*>, fallbackMessage: String): String {
    val raw = try {
        response.errorBody()?.string()
    } catch (e: Exception) {
        null
    }
    if (!raw.isNullOrBlank()) {
        try {
            // A NestJS validáció a message mezőben tömböt is küldhet.
            val json = JsonParser.parseString(raw).asJsonObject
            val msg = json.get("message")
            if (msg != null && !msg.isJsonNull) {
                return if (msg.isJsonArray) {
                    msg.asJsonArray.joinToString("\n") { it.asString }
                } else {
                    msg.asString
                }
            }
        } catch (e: Exception) {
            // nem JSON
        }
    }
    return when (response.code()) {
        401 -> "Lejárt a munkamenet, jelentkezz be újra."
        403 -> "Nincs jogosultságod ehhez a művelethez."
        404 -> "$fallbackMessage (nem található)"
        in 500..599 -> "Szerverhiba történt (${response.code()})."
        else -> "$fallbackMessage (${response.code()})"
    }
}

// Hálózati hibák felhasználóbarát szövege
fun Throwable.userMessage(fallback: String = "Ismeretlen hiba történt."): String =
    when (this) {
        is IOException -> "Nem érhető el a szerver. Fut a Docker backend?"
        else -> message?.takeIf { it.isNotBlank() } ?: fallback
    }

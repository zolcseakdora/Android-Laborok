
package com.example.progr3ssapp.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Android emulátorból a számítógép localhost címe
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // JWT token memóriában.
    // A titkosított mentést továbbra is
    // a SecureSessionStore végzi.
    @Volatile
    private var accessToken: String? = null

    // Token frissítése bejelentkezéskor,
    // törlése kijelentkezéskor.
    fun updateAccessToken(token: String?) {
        accessToken = token
    }

    // A backend API-khoz automatikusan
    // hozzáadjuk a Bearer tokent.
    private val authInterceptor = Interceptor { chain ->

        val originalRequest = chain.request()

        val path = originalRequest.url.encodedPath
            .removePrefix("/")

        // Login és Register esetén nem küldünk tokent.
        val isAuthRequest =
            path == "auth/local/signin" ||
                    path == "auth/local/signup"

        val token = accessToken

        val newRequest =
            if (!isAuthRequest && !token.isNullOrBlank()) {

                originalRequest.newBuilder()
                    .header(
                        "Authorization",
                        "Bearer $token"
                    )
                    .build()

            } else {
                originalRequest
            }

        chain.proceed(newRequest)
    }

    // Csak alap HTTP információk naplózása.
    // Nem írunk ki tokeneket vagy jelszavakat.
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // HTTP-kliens
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Retrofit példány
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(
            GsonConverterFactory.create()
        )
        .build()

    // API elérése
    val api: ApiService =
        retrofit.create(ApiService::class.java)
}

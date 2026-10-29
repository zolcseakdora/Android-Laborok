
package com.example.progr3ssapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // A számítógépen futó backend címe
    // 10.0.2.2 = localhost az Android emulátorból
    private const val BASE_URL =
        "http://10.0.2.2:8080/"

    // HTTP-kérések és válaszkódok naplózása
    // BASIC: nem naplózza a jelszót vagy tokeneket
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // HTTP-kliens beállítása
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Retrofit példány létrehozása
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(
            GsonConverterFactory.create()
        )
        .build()

    // Az API elérhetővé tétele
    val api: ApiService =
        retrofit.create(ApiService::class.java)
}

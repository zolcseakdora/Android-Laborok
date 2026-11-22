package com.example.progr3ssapp.network

import com.example.progr3ssapp.model.AuthTokens
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Android emulátorból a számítógép localhost címe (Docker backend: 8080)
    const val BASE_URL = "http://10.0.2.2:8080/"

    // A tokenek memóriában. A titkosított mentést a SecureSessionStore végzi.
    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var refreshToken: String? = null

    // Ha az Authenticator új tokeneket kap, ezen keresztül
    // szól a SessionManagernek, hogy mentse el őket.
    @Volatile
    var onTokensRefreshed: ((AuthTokens) -> Unit)? = null

    fun updateTokens(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
    }

    // Visszafelé kompatibilitás a régi kóddal
    fun updateAccessToken(token: String?) {
        accessToken = token
    }

    fun currentRefreshToken(): String? = refreshToken

    private val noTokenPaths = setOf(
        "auth/local/signin",
        "auth/local/signup",
        "auth/local/refresh",
        "auth/reset-password-via-email"
    )

    // Bearer token hozzáadása minden védett kéréshez.
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val path = original.url.encodedPath.removePrefix("/")
        val token = accessToken

        val request =
            if (path !in noTokenPaths &&
                original.header("Authorization") == null &&
                !token.isNullOrBlank()
            ) {
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                original
            }

        chain.proceed(request)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // Külön kliens a token frissítéséhez (Authenticator nélkül),
    // hogy ne kerüljünk végtelen ciklusba.
    private val refreshApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // 401 esetén egyszer megpróbáljuk frissíteni az access tokent.
    private val tokenAuthenticator = Authenticator { _, response ->
        val path = response.request.url.encodedPath.removePrefix("/")
        if (path in noTokenPaths) return@Authenticator null
        if (response.priorResponse != null) return@Authenticator null

        val rt = refreshToken ?: return@Authenticator null

        synchronized(this) {
            // Lehet, hogy közben egy másik kérés már frissített.
            val usedToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
            val current = accessToken
            if (!current.isNullOrBlank() && current != usedToken) {
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $current")
                    .build()
            }

            val tokens = try {
                val r = refreshApi.refreshSync("Bearer $rt").execute()
                if (r.isSuccessful) r.body() else null
            } catch (e: Exception) {
                null
            }

            val newAccess = tokens?.accessToken
            if (tokens == null || newAccess.isNullOrBlank()) {
                return@Authenticator null
            }

            accessToken = newAccess
            refreshToken = tokens.refreshToken ?: rt
            onTokensRefreshed?.invoke(
                AuthTokens(newAccess, refreshToken)
            )

            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .build()
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .authenticator(tokenAuthenticator)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Képek letöltéséhez is ezt a klienst használjuk (token is megy vele).
    val httpClient: OkHttpClient get() = client

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)

    // Relatív kép URL-ből teljes URL
    fun absoluteUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) {
            // A backend néha localhost-ot ad vissza, ami az emulátorból nem elérhető.
            return path
                .replace("://localhost", "://10.0.2.2")
                .replace("://127.0.0.1", "://10.0.2.2")
        }
        return BASE_URL + path.removePrefix("/")
    }
}

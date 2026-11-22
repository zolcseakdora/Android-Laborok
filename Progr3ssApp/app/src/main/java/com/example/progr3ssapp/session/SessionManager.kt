package com.example.progr3ssapp.session

import android.content.Context
import com.example.progr3ssapp.model.AuthProfile
import com.example.progr3ssapp.model.AuthResponse
import com.example.progr3ssapp.model.AuthUser
import com.example.progr3ssapp.network.RetrofitClient
import com.example.progr3ssapp.repository.AuthRepository
import com.example.progr3ssapp.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// ==========================================
// MUNKAMENET KEZELÉSE
// - tokenek titkosított mentése
// - automatikus bejelentkezés (Splash)
// - kijelentkezés
// ==========================================

object SessionManager {

    private lateinit var store: SecureSessionStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // A legutóbb mentett teljes válasz (user + tokenek)
    @Volatile
    private var current: AuthResponse? = null

    fun init(context: Context) {
        if (::store.isInitialized) return
        store = SecureSessionStore(context.applicationContext)

        // Ha az OkHttp a háttérben frissítette a tokent, mentsük el.
        RetrofitClient.onTokensRefreshed = { tokens ->
            val saved = current
            if (saved != null) {
                val updated = saved.copy(tokens = tokens)
                current = updated
                scope.launch { runCatching { store.save(updated) } }
            }
        }
    }

    // Sikeres login / regisztráció után
    suspend fun onLoggedIn(response: AuthResponse) {
        val tokens = response.tokens
        require(!tokens?.accessToken.isNullOrBlank()) { "Hiányzik az access token" }

        RetrofitClient.updateTokens(tokens?.accessToken, tokens?.refreshToken)
        current = response
        response.user?.let { AuthSession.setUser(it) }
        runCatching { store.save(response) }

        // A teljes profilt (username, kép) is betöltjük, ha a válaszban hiányos.
        if (response.user?.profile?.username.isNullOrBlank()) {
            refreshProfile()
        }
    }

    // Splash: automatikus bejelentkezés a mentett refresh tokennel.
    // true = sikerült, mehet a Home képernyő.
    suspend fun tryAutoLogin(): Boolean {
        val saved = store.load() ?: return false
        val refresh = saved.tokens?.refreshToken

        if (refresh.isNullOrBlank()) {
            store.clear()
            return false
        }

        val result = AuthRepository().refresh(refresh)
        val tokens = result.getOrNull()

        if (tokens?.accessToken.isNullOrBlank()) {
            // Ha csak a hálózat nem elérhető, a tokent megtartjuk,
            // de a Login képernyőre megyünk.
            if (result.exceptionOrNull() !is java.io.IOException) {
                store.clear()
            }
            RetrofitClient.updateTokens(null, null)
            return false
        }

        val updated = saved.copy(
            tokens = tokens!!.copy(
                refreshToken = tokens.refreshToken ?: refresh
            )
        )
        RetrofitClient.updateTokens(
            updated.tokens?.accessToken,
            updated.tokens?.refreshToken
        )
        current = updated
        saved.user?.let { AuthSession.setUser(it) }
        runCatching { store.save(updated) }

        refreshProfile()
        return AuthSession.currentUser.value != null
    }

    // GET /profile – a felhasználó adatainak frissítése
    suspend fun refreshProfile(): AuthProfile? {
        val profile = ProfileRepository().getProfile().getOrNull() ?: return null
        updateProfile(profile)
        return profile
    }

    // Profil módosítás után a memóriában lévő usert is frissítjük.
    suspend fun updateProfile(profile: AuthProfile) {
        val user = AuthSession.currentUser.value ?: AuthUser(email = profile.email)
        val updatedUser = user.copy(
            email = user.email ?: profile.email,
            profile = profile
        )
        AuthSession.setUser(updatedUser)

        val saved = current
        if (saved != null) {
            val updated = saved.copy(user = updatedUser)
            current = updated
            runCatching { store.save(updated) }
        }
    }

    // Kijelentkezés: szerver értesítése + helyi adatok törlése
    suspend fun logout() {
        runCatching { AuthRepository().logout() }
        clearLocal()
    }

    suspend fun clearLocal() {
        runCatching { store.clear() }
        current = null
        RetrofitClient.updateTokens(null, null)
        AuthSession.clear()
    }
}

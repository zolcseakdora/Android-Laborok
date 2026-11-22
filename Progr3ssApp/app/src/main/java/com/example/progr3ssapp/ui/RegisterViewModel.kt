package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.network.userMessage
import com.example.progr3ssapp.repository.AuthRepository
import com.example.progr3ssapp.session.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ==========================================
// REGISZTRÁCIÓ
// Sikeres regisztráció után a felhasználó
// azonnal a Home képernyőre kerül.
// ==========================================

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (_uiState.value is RegisterUiState.Loading) return

        val error = when {
            username.isBlank() || email.isBlank() || password.isBlank() ->
                "Minden mező kitöltése kötelező."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "Érvénytelen email cím."
            password.length < 6 ->
                "A jelszó legalább 6 karakter legyen."
            password != confirmPassword ->
                "A két jelszó nem egyezik."
            else -> null
        }
        if (error != null) {
            _uiState.value = RegisterUiState.Error(error)
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            repository.register(username.trim(), email.trim(), password)
                .onSuccess { body ->
                    try {
                        if (!body.tokens?.accessToken.isNullOrBlank()) {
                            SessionManager.onLoggedIn(body)
                            _uiState.value = RegisterUiState.Success
                        } else {
                            // Ha a szerver nem adott tokent, bejelentkeztetjük.
                            repository.login(email.trim(), password)
                                .onSuccess { login ->
                                    SessionManager.onLoggedIn(login)
                                    _uiState.value = RegisterUiState.Success
                                }
                                .onFailure { e ->
                                    _uiState.value = RegisterUiState.Error(
                                        e.userMessage("Sikeres regisztráció, de a bejelentkezés nem sikerült.")
                                    )
                                }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _uiState.value = RegisterUiState.Error(e.userMessage())
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = RegisterUiState.Error(e.userMessage("Sikertelen regisztráció."))
                }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

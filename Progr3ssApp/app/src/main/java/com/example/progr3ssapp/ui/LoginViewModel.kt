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
// LOGIN
// ==========================================

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) return

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Az email és a jelszó megadása kötelező.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = LoginUiState.Error("Érvénytelen email cím.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            repository.login(email.trim(), password)
                .onSuccess { body ->
                    try {
                        if (body.user == null || body.tokens?.accessToken.isNullOrBlank()) {
                            _uiState.value = LoginUiState.Error("Hiányos szerverválasz.")
                            return@onSuccess
                        }
                        SessionManager.onLoggedIn(body)
                        _uiState.value = LoginUiState.Success
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _uiState.value = LoginUiState.Error(e.userMessage())
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = LoginUiState.Error(e.userMessage("Sikertelen bejelentkezés."))
                }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

// ==========================================
// RESET PASSWORD
// ==========================================

data class ResetPasswordUiState(
    val isLoading: Boolean = false,
    val sentTo: String? = null,
    val error: String? = null
)

class ResetPasswordViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun reset(email: String) {
        if (_uiState.value.isLoading) return
        val trimmed = email.trim()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            _uiState.value = ResetPasswordUiState(error = "Adj meg egy érvényes email címet.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState(isLoading = true)

            repository.resetPassword(trimmed)
                .onSuccess {
                    _uiState.value = ResetPasswordUiState(sentTo = trimmed)
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = ResetPasswordUiState(
                        error = e.userMessage("Nem sikerült új jelszót kérni.")
                    )
                }
        }
    }
}

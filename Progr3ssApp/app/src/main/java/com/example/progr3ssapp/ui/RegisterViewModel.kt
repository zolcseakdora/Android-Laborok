
package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Regisztráció állapotai
sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState =
        MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)

    val uiState = _uiState.asStateFlow()

    // Regisztráció indítása
    fun register(
        username: String,
        email: String,
        password: String
    ) {
        if (_uiState.value is RegisterUiState.Loading) return

        if (username.isBlank() ||
            email.isBlank() ||
            password.isBlank()
        ) {
            _uiState.value = RegisterUiState.Error(
                "Minden mező kitöltése kötelező!"
            )
            return
        }

        if (!email.contains("@")) {
            _uiState.value = RegisterUiState.Error(
                "Hibás emailcím!"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            try {
                val response = repository.register(
                    username.trim(),
                    email.trim(),
                    password
                )

                if (response.isSuccessful) {
                    _uiState.value = RegisterUiState.Success
                } else {
                    _uiState.value = RegisterUiState.Error(
                        when (response.code()) {
                            400 -> "Hibás adatok vagy már létező felhasználó."
                            409 -> "Ez a felhasználó már létezik."
                            else -> "Regisztrációs hiba: HTTP ${response.code()}"
                        }
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(
                    "Nem sikerült a regisztráció. Ellenőrizd a szervert!"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

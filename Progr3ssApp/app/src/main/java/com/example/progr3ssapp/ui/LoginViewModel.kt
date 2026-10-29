package com.example.progr3ssapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.model.AuthResponse
import com.example.progr3ssapp.repository.AuthRepository
import com.example.progr3ssapp.session.AuthSession
import com.example.progr3ssapp.session.SecureSessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val response: AuthResponse) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val secureStore = SecureSessionStore(application)
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) return
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email es jelszo megadasa kotelezo!")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = repository.login(email.trim(), password)
                if (!response.isSuccessful) {
                    val message = when (response.code()) {
                        400 -> "Hibas bejelentkezesi keres."
                        401, 403 -> "A szerver elutasitotta a bejelentkezest."
                        404 -> "A bejelentkezesi vegpont nem talalhato."
                        in 500..599 -> "Szerverhiba tortent."
                        else -> "HTTP hiba: ${response.code()}"
                    }
                    _uiState.value = LoginUiState.Error(message)
                    return@launch
                }

                val body = response.body()
                val user = body?.user
                if (body == null || user == null || body.tokens?.accessToken.isNullOrBlank()) {
                    _uiState.value = LoginUiState.Error("Hianyos szervervalasz!")
                    return@launch
                }

                // Az erzekeny valaszt elobb titkositva taroljuk.
                secureStore.save(body)
                // A felulet csak a useradatot figyeli, tokent nem.
                AuthSession.setUser(user)
                _uiState.value = LoginUiState.Success(body)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.value = LoginUiState.Error("Nem erheto el a backend. Ellenorizd a Dockert!")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("A bejelentkezest vagy a biztonsagos mentest nem sikerult befejezni.")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

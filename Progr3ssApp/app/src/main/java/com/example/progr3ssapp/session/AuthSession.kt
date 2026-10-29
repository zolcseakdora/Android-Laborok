package com.example.progr3ssapp.session

import com.example.progr3ssapp.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Csak a profil nyilvanos adatai vannak memoriaban, token NINCS benne.
object AuthSession {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun setUser(user: AuthUser) {
        _currentUser.value = user
    }

    fun clear() {
        _currentUser.value = null
    }
}

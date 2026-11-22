package com.example.progr3ssapp.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.model.AuthProfile
import com.example.progr3ssapp.network.models.HabitResponse
import com.example.progr3ssapp.network.userMessage
import com.example.progr3ssapp.repository.HabitRepository
import com.example.progr3ssapp.repository.ProfileRepository
import com.example.progr3ssapp.session.AuthSession
import com.example.progr3ssapp.session.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ==========================================
// PROFILE
// ==========================================

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: AuthProfile? = null,
    val habits: List<HabitResponse> = emptyList(),
    val error: String? = null,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false
)

class ProfileViewModel : ViewModel() {

    private val profileRepository = ProfileRepository()
    private val habitRepository = HabitRepository()

    private val _uiState = MutableStateFlow(
        ProfileUiState(profile = AuthSession.currentUser.value?.profile)
    )
    val uiState = _uiState.asStateFlow()

    // GET /profile + GET /habit/user/{userId}
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val profileResult = profileRepository.getProfile()
            val profile = profileResult.getOrNull()
            if (profile != null) SessionManager.updateProfile(profile)

            val user = AuthSession.currentUser.value
            val userId = (user?.id ?: profile?.id)?.toLong()

            val habitsResult = if (userId != null) {
                habitRepository.getHabitsByUser(userId)
                    .recoverCatching { habitRepository.getHabits().getOrThrow() }
            } else {
                habitRepository.getHabits()
            }

            val error = listOfNotNull(
                profileResult.exceptionOrNull(),
                habitsResult.exceptionOrNull()
            ).firstOrNull { it !is CancellationException }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    profile = profile ?: user?.profile ?: it.profile,
                    habits = habitsResult.getOrNull() ?: it.habits,
                    error = error?.userMessage()
                )
            }
        }
    }

    // POST /auth/local/logout + helyi adatok törlése
    fun logout() {
        if (_uiState.value.isLoggingOut) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            SessionManager.logout()
            _uiState.update { it.copy(isLoggingOut = false, isLoggedOut = true) }
        }
    }
}

// ==========================================
// EDIT PROFILE
// ==========================================

data class EditProfileUiState(
    val profile: AuthProfile? = null,
    val email: String = "",
    val username: String = "",
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val isSaved: Boolean = false
)

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository()

    private val _uiState = MutableStateFlow(
        AuthSession.currentUser.value.let { user ->
            EditProfileUiState(
                profile = user?.profile,
                email = user?.profile?.email ?: user?.email.orEmpty(),
                username = user?.profile?.username.orEmpty()
            )
        }
    )
    val uiState = _uiState.asStateFlow()

    fun onUsernameChange(v: String) = _uiState.update { it.copy(username = v, error = null, message = null) }

    // PATCH /profile { username }
    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val name = state.username.trim()
        if (name.length < 3) {
            _uiState.update { it.copy(error = "A felhasználónév legalább 3 karakter legyen.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            repository.updateUsername(name)
                .onSuccess { profile ->
                    SessionManager.updateProfile(profile)
                    _uiState.update { it.copy(isSaving = false, profile = profile, isSaved = true) }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update { it.copy(isSaving = false, error = e.userMessage("Nem sikerült menteni.")) }
                }
        }
    }

    // POST /profile/upload-profile-image
    fun uploadImage(uri: Uri) {
        if (_uiState.value.isUploading) return
        val resolver = getApplication<Application>().contentResolver

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null, message = null) }

            val bytes = withContext(Dispatchers.IO) {
                runCatching { resolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            }
            if (bytes == null) {
                _uiState.update { it.copy(isUploading = false, error = "Nem sikerült beolvasni a képet.") }
                return@launch
            }
            if (bytes.size > 5 * 1024 * 1024) {
                _uiState.update { it.copy(isUploading = false, error = "A kép legfeljebb 5 MB lehet.") }
                return@launch
            }
            val mime = resolver.getType(uri) ?: "image/jpeg"
            val ext = if (mime.contains("png")) "png" else "jpg"

            repository.uploadProfileImage(bytes, mime, "profile.$ext")
                .onSuccess { profile ->
                    SessionManager.updateProfile(profile)
                    _uiState.update {
                        it.copy(isUploading = false, profile = profile, message = "Profilkép frissítve.")
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(isUploading = false, error = e.userMessage("Nem sikerült feltölteni a képet."))
                    }
                }
        }
    }
}

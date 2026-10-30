
package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.network.models.HabitCategory
import com.example.progr3ssapp.repository.HabitRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// 7. LABOR - ADD HABIT VIEWMODEL
// ==========================================

// A regisztrációs űrlaphoz hasonlóan
// az új szokás létrehozásának állapotát tárolja.
data class AddHabitUiState(
    val name: String = "",
    val description: String = "",
    val goal: String = "",
    val categories: List<HabitCategory> = emptyList(),
    val selectedCategoryId: Long? = null,
    val isLoadingCategories: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

class AddHabitViewModel : ViewModel() {

    private val repository = HabitRepository()

    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState = _uiState.asStateFlow()

    // A képernyő megnyitásakor
    // automatikusan lekérjük a kategóriákat.
    init {
        loadCategories()
    }

    // ======================================
    // KATEGÓRIÁK LEKÉRÉSE
    // GET /habit/categories
    // ======================================

    fun loadCategories() {

        _uiState.update {
            it.copy(
                isLoadingCategories = true,
                error = null
            )
        }

        viewModelScope.launch {

            repository.getCategories()

                .onSuccess { categories ->

                    _uiState.update { state ->

                        state.copy(
                            categories = categories,

                            // Alapból az első kategória
                            // lesz kiválasztva.
                            selectedCategoryId =
                                state.selectedCategoryId
                                    ?.takeIf { selected ->
                                        categories.any {
                                            it.id == selected
                                        }
                                    }
                                    ?: categories.firstOrNull()?.id,

                            isLoadingCategories = false
                        )
                    }
                }

                .onFailure { exception ->

                    if (exception is CancellationException) {
                        throw exception
                    }

                    _uiState.update {
                        it.copy(
                            isLoadingCategories = false,
                            error = exception.message
                                ?: "Nem sikerült lekérni a kategóriákat."
                        )
                    }
                }
        }
    }

    // ======================================
    // ŰRLAP ADATAINAK MÓDOSÍTÁSA
    // ======================================

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(name = value, error = null)
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update {
            it.copy(description = value, error = null)
        }
    }

    fun onGoalChange(value: String) {
        _uiState.update {
            it.copy(goal = value, error = null)
        }
    }

    fun onCategorySelected(id: Long) {
        _uiState.update {
            it.copy(
                selectedCategoryId = id,
                error = null
            )
        }
    }

    // ======================================
    // ÚJ SZOKÁS LÉTREHOZÁSA
    // POST /habit
    // ======================================

    fun createHabit() {

        val state = _uiState.value

        // Ne lehessen kétszer elmenteni
        if (state.isSaving || state.isSaved) return

        if (state.isLoadingCategories) return

        // Kötelező mezők ellenőrzése
        val validationError = when {

            state.name.isBlank() ->
                "Add meg a szokás nevét!"

            state.selectedCategoryId == null ->
                "Válassz kategóriát!"

            state.goal.isBlank() ->
                "Add meg a célodat!"

            else -> null
        }

        if (validationError != null) {

            _uiState.update {
                it.copy(error = validationError)
            }

            return
        }

        val categoryId = state.selectedCategoryId
            ?: return

        _uiState.update {
            it.copy(
                isSaving = true,
                error = null
            )
        }

        viewModelScope.launch {

            repository.createHabit(
                name = state.name.trim(),
                description = state.description.trim(),
                categoryId = categoryId,
                goal = state.goal.trim()
            )

                .onSuccess {

                    // A backend elfogadta az új szokást
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true
                        )
                    }
                }

                .onFailure { exception ->

                    if (exception is CancellationException) {
                        throw exception
                    }

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = exception.message
                                ?: "Nem sikerült létrehozni a szokást."
                        )
                    }
                }
        }
    }
}

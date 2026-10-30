
package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.network.models.HabitResponse
import com.example.progr3ssapp.network.models.ScheduleResponse
import com.example.progr3ssapp.repository.HabitRepository
import com.example.progr3ssapp.repository.ScheduleRepository
import com.example.progr3ssapp.utils.DateTimeUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// A Home képernyő adatai és állapota
data class HomeUiState(
    val isLoading: Boolean = false,
    val habits: List<HabitResponse> = emptyList(),
    val schedules: List<ScheduleResponse> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val habitRepository = HabitRepository()
    private val scheduleRepository = ScheduleRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    // Szokások és mai ütemezések betöltése
    fun refresh() {

        // A korábbi betöltést megszakítjuk
        loadJob?.cancel()

        loadJob = viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Felhasználó szokásainak lekérése
                val habitsResult =
                    habitRepository.getHabits()

                // Mai program lekérése
                val schedulesResult =
                    scheduleRepository.getScheduleByDay(
                        DateTimeUtils.today()
                    )

                // A megszakítást nem kezeljük
                // közönséges hálózati hibaként
                habitsResult.exceptionOrNull()?.let {
                    if (it is CancellationException) throw it
                }

                schedulesResult.exceptionOrNull()?.let {
                    if (it is CancellationException) throw it
                }

                val errors = listOfNotNull(
                    habitsResult.exceptionOrNull()?.message,
                    schedulesResult.exceptionOrNull()?.message
                )

                _uiState.value = HomeUiState(
                    isLoading = false,
                    habits = habitsResult.getOrNull()
                        ?: emptyList(),
                    schedules = schedulesResult.getOrNull()
                        ?: emptyList(),
                    errorMessage = errors
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString("\n")
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Nem sikerült betölteni az adatokat."
                )
            }
        }
    }
}

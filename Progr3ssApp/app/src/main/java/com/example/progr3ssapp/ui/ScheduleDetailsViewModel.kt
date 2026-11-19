
package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.network.models.ScheduleResponse
import com.example.progr3ssapp.network.models.UpdateScheduleRequest
import com.example.progr3ssapp.repository.ScheduleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleDetailsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val schedule: ScheduleResponse? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class ScheduleDetailsViewModel : ViewModel() {

    private val repository = ScheduleRepository()

    private val _uiState =
        MutableStateFlow(ScheduleDetailsUiState())

    val uiState = _uiState.asStateFlow()

    fun loadSchedule(id: Long) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            repository.getScheduleById(id)
                .onSuccess { schedule ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            schedule = schedule
                        )
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException)
                        throw exception

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
        }
    }

    fun saveChanges(
        id: Long,
        status: String,
        notes: String
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(isSaving = true, error = null)
            }

            val request = UpdateScheduleRequest(
                status = status,
                notes = notes
            )

            repository.updateSchedule(id, request)
                .onSuccess { schedule ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            schedule = schedule
                        )
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException)
                        throw exception

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = exception.message
                        )
                    }
                }
        }
    }

    fun deleteSchedule(id: Long) {
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(isDeleting = true, error = null)
            }

            repository.deleteSchedule(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            isDeleted = true
                        )
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException)
                        throw exception

                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            error = exception.message
                        )
                    }
                }
        }
    }
}

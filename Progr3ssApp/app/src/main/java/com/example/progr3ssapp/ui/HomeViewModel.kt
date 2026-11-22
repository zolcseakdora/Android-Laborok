package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.network.models.CreateProgressRequest
import com.example.progr3ssapp.network.models.ScheduleResponse
import com.example.progr3ssapp.network.models.UpdateScheduleRequest
import com.example.progr3ssapp.network.userMessage
import com.example.progr3ssapp.repository.ScheduleRepository
import com.example.progr3ssapp.utils.DateTimeUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// HOME – a mai napi terv
// ==========================================

data class HomeUiState(
    val isLoading: Boolean = false,
    val schedules: List<ScheduleResponse> = emptyList(),
    val errorMessage: String? = null,
    // Add Progress párbeszédablak
    val progressFor: ScheduleResponse? = null,
    val isSavingProgress: Boolean = false,
    val progressError: String? = null
)

class HomeViewModel : ViewModel() {

    private val repository = ScheduleRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    // GET /schedule/day – időrendben
    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getScheduleByDay(DateTimeUtils.today())
                .onSuccess { list ->
                    val sorted = list.sortedBy {
                        DateTimeUtils.parse(it.startTime)?.time ?: Long.MAX_VALUE
                    }
                    _uiState.update { it.copy(isLoading = false, schedules = sorted) }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.userMessage("Nem sikerült betölteni a mai tervet.")
                        )
                    }
                }
        }
    }

    fun openProgress(schedule: ScheduleResponse) {
        _uiState.update { it.copy(progressFor = schedule, progressError = null) }
    }

    fun closeProgress() {
        _uiState.update { it.copy(progressFor = null, progressError = null) }
    }

    // POST /progress (+ ha kész, a státuszt is Completed-re állítjuk)
    fun saveProgress(minutes: Int?, notes: String, completed: Boolean) {
        val schedule = _uiState.value.progressFor ?: return
        if (_uiState.value.isSavingProgress) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProgress = true, progressError = null) }

            val request = CreateProgressRequest(
                scheduleId = schedule.id,
                date = DateTimeUtils.nowIso(),
                loggedTime = minutes,
                notes = notes.ifBlank { null },
                isCompleted = completed
            )

            repository.createProgress(request)
                .onSuccess {
                    if (completed && schedule.status != "Completed") {
                        repository.updateSchedule(
                            schedule.id,
                            UpdateScheduleRequest(status = "Completed")
                        )
                    }
                    _uiState.update {
                        it.copy(isSavingProgress = false, progressFor = null)
                    }
                    refresh()
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(
                            isSavingProgress = false,
                            progressError = e.userMessage("Nem sikerült menteni.")
                        )
                    }
                }
        }
    }
}

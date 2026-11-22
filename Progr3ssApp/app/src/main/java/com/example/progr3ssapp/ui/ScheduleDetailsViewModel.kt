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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// SCHEDULE DETAILS (+ Edit Notes, Delete, Add Progress)
// ==========================================

data class ScheduleDetailsUiState(
    val isLoading: Boolean = true,
    val schedule: ScheduleResponse? = null,
    val error: String? = null,
    // Edit Notes
    val isEditingNotes: Boolean = false,
    val notesDraft: String = "",
    val isSavingNotes: Boolean = false,
    // Delete
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    // Add Progress
    val showProgressDialog: Boolean = false,
    val isSavingProgress: Boolean = false,
    val progressError: String? = null
)

class ScheduleDetailsViewModel : ViewModel() {

    private val repository = ScheduleRepository()

    private val _uiState = MutableStateFlow(ScheduleDetailsUiState())
    val uiState = _uiState.asStateFlow()

    // GET /schedule/{id}
    fun loadSchedule(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.schedule == null, error = null) }

            repository.getScheduleById(id)
                .onSuccess { schedule ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            schedule = schedule,
                            notesDraft = if (it.isEditingNotes) it.notesDraft else schedule.notes.orEmpty()
                        )
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(isLoading = false, error = e.userMessage("Nem sikerült betölteni az ütemezést."))
                    }
                }
        }
    }

    // ---------- Edit Notes ----------

    fun startEditNotes() = _uiState.update {
        it.copy(isEditingNotes = true, notesDraft = it.schedule?.notes.orEmpty(), error = null)
    }

    fun cancelEditNotes() = _uiState.update { it.copy(isEditingNotes = false) }

    fun onNotesChange(v: String) = _uiState.update { it.copy(notesDraft = v) }

    // PATCH /schedule/{id} { notes }
    fun saveNotes() {
        val state = _uiState.value
        val schedule = state.schedule ?: return
        if (state.isSavingNotes) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingNotes = true, error = null) }

            repository.updateSchedule(schedule.id, UpdateScheduleRequest(notes = state.notesDraft.trim()))
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            isSavingNotes = false,
                            isEditingNotes = false,
                            // Ha a PATCH válasz nem tartalmaz habitot, a régit megtartjuk.
                            schedule = updated.copy(
                                habit = updated.habit ?: schedule.habit,
                                progress = updated.progress ?: schedule.progress,
                                participants = updated.participants ?: schedule.participants,
                                notes = updated.notes ?: state.notesDraft.trim()
                            )
                        )
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(isSavingNotes = false, error = e.userMessage("Nem sikerült menteni a jegyzetet."))
                    }
                }
        }
    }

    // ---------- Delete ----------

    // DELETE /schedule/{id}
    fun deleteSchedule() {
        val schedule = _uiState.value.schedule ?: return
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }

            repository.deleteSchedule(schedule.id)
                .onSuccess { _uiState.update { it.copy(isDeleting = false, isDeleted = true) } }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(isDeleting = false, error = e.userMessage("Nem sikerült törölni."))
                    }
                }
        }
    }

    // ---------- Add Progress ----------

    fun openProgress() = _uiState.update { it.copy(showProgressDialog = true, progressError = null) }
    fun closeProgress() = _uiState.update { it.copy(showProgressDialog = false) }

    fun saveProgress(minutes: Int?, notes: String, completed: Boolean) {
        val schedule = _uiState.value.schedule ?: return
        if (_uiState.value.isSavingProgress) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProgress = true, progressError = null) }

            repository.createProgress(
                CreateProgressRequest(
                    scheduleId = schedule.id,
                    date = DateTimeUtils.nowIso(),
                    loggedTime = minutes,
                    notes = notes.ifBlank { null },
                    isCompleted = completed
                )
            )
                .onSuccess {
                    if (completed && schedule.status != "Completed") {
                        repository.updateSchedule(schedule.id, UpdateScheduleRequest(status = "Completed"))
                    }
                    _uiState.update { it.copy(isSavingProgress = false, showProgressDialog = false) }
                    loadSchedule(schedule.id)
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(isSavingProgress = false, progressError = e.userMessage("Nem sikerült menteni."))
                    }
                }
        }
    }
}

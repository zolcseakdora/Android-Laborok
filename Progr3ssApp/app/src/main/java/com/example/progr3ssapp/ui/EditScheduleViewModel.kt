package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.network.models.ParticipantDto
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
// EDIT SCHEDULE – PATCH /schedule/{id}
// ==========================================

data class EditScheduleUiState(
    val isLoading: Boolean = true,
    val schedule: ScheduleResponse? = null,
    val localDate: String = DateTimeUtils.today(),
    val startTime: String = "08:00",
    val endTime: String = "08:30",
    val durationAmount: String = "30",
    val durationInHours: Boolean = false,
    val status: String = "Planned",
    val participants: List<ParticipantDto> = emptyList(),
    val newPartnerId: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

class EditScheduleViewModel : ViewModel() {

    private val repository = ScheduleRepository()

    private val _uiState = MutableStateFlow(EditScheduleUiState())
    val uiState = _uiState.asStateFlow()

    private var loadedId: Long? = null

    fun load(id: Long) {
        if (loadedId == id) return
        loadedId = id

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getScheduleById(id)
                .onSuccess { s ->
                    val start = DateTimeUtils.formatTime(s.startTime).takeIf { it != "-" } ?: "08:00"
                    val duration = s.durationMinutes
                        ?: DateTimeUtils.minutesBetween(s.startTime, s.endTime)
                        ?: 30
                    val end = DateTimeUtils.formatTime(s.endTime).takeIf { it != "-" }
                        ?: addToTime(start, duration)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            schedule = s,
                            localDate = DateTimeUtils.localDate(s.startTime)
                                ?: DateTimeUtils.localDate(s.date)
                                ?: DateTimeUtils.today(),
                            startTime = start,
                            endTime = end,
                            durationAmount = duration.toString(),
                            durationInHours = false,
                            status = s.status ?: "Planned",
                            participants = s.participants.orEmpty(),
                            notes = s.notes.orEmpty()
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

    // A kezdés módosításakor a befejezés követi az időtartamot.
    fun onStartTimeChange(v: String) = _uiState.update {
        it.copy(startTime = v, endTime = addToTime(v, durationMinutes(it) ?: 30), error = null)
    }

    // A befejezés módosításakor az időtartamot számoljuk újra.
    fun onEndTimeChange(v: String) = _uiState.update {
        val diff = diffMinutes(it.startTime, v)
        if (diff != null && diff > 0) {
            it.copy(endTime = v, durationAmount = diff.toString(), durationInHours = false, error = null)
        } else {
            it.copy(endTime = v, error = "A befejezés legyen később, mint a kezdés.")
        }
    }

    fun onDurationChange(v: String) {
        if (!v.all { it.isDigit() } || v.length > 4) return
        _uiState.update {
            val updated = it.copy(durationAmount = v, error = null)
            val minutes = durationMinutes(updated)
            if (minutes != null && minutes > 0) updated.copy(endTime = addToTime(it.startTime, minutes))
            else updated
        }
    }

    fun onDurationUnitChange(hours: Boolean) = _uiState.update {
        val updated = it.copy(durationInHours = hours)
        val minutes = durationMinutes(updated)
        if (minutes != null && minutes > 0) updated.copy(endTime = addToTime(it.startTime, minutes))
        else updated
    }

    fun onStatusChange(v: String) = _uiState.update { it.copy(status = v) }
    fun onNotesChange(v: String) = _uiState.update { it.copy(notes = v) }

    fun onNewPartnerChange(v: String) {
        if (v.all { it.isDigit() } && v.length <= 9) _uiState.update { it.copy(newPartnerId = v) }
    }

    fun addPartner() = _uiState.update {
        val id = it.newPartnerId.toLongOrNull() ?: return@update it.copy(error = "Adj meg egy felhasználó ID-t.")
        if (it.participants.any { p -> p.id == id }) return@update it.copy(newPartnerId = "")
        it.copy(participants = it.participants + ParticipantDto(id = id, name = "User #$id"), newPartnerId = "", error = null)
    }

    fun removePartner(id: Long) = _uiState.update {
        it.copy(participants = it.participants.filterNot { p -> p.id == id })
    }

    fun save() {
        val state = _uiState.value
        val schedule = state.schedule ?: return
        if (state.isSaving) return

        val duration = durationMinutes(state)
        if (duration == null || duration <= 0) {
            _uiState.update { it.copy(error = "Az időtartam legalább 1 perc legyen.") }
            return
        }
        val startIso = DateTimeUtils.toUtcIso(state.localDate, state.startTime)
        if (startIso == null) {
            _uiState.update { it.copy(error = "Érvénytelen időpont.") }
            return
        }
        val endIso = DateTimeUtils.plusMinutes(startIso, duration)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            repository.updateSchedule(
                schedule.id,
                UpdateScheduleRequest(
                    startTime = startIso,
                    endTime = endIso,
                    durationMinutes = duration,
                    status = state.status,
                    participantIds = state.participants.map { it.id },
                    notes = state.notes.trim()
                )
            )
                .onSuccess { _uiState.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(isSaving = false, error = e.userMessage("Nem sikerült menteni."))
                    }
                }
        }
    }

    private fun durationMinutes(s: EditScheduleUiState): Int? =
        s.durationAmount.toIntOrNull()?.let { if (s.durationInHours) it * 60 else it }

    private fun addToTime(time: String, minutes: Int): String {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val total = ((h * 60 + m + minutes) % (24 * 60) + 24 * 60) % (24 * 60)
        return "%02d:%02d".format(total / 60, total % 60)
    }

    private fun diffMinutes(start: String, end: String): Int? {
        fun toMin(t: String): Int? {
            val p = t.split(":")
            val h = p.getOrNull(0)?.toIntOrNull() ?: return null
            val m = p.getOrNull(1)?.toIntOrNull() ?: return null
            return h * 60 + m
        }
        val s = toMin(start) ?: return null
        val e = toMin(end) ?: return null
        return e - s
    }
}

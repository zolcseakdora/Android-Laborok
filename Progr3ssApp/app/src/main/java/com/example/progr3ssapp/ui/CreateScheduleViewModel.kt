package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.network.models.CreateCustomScheduleRequest
import com.example.progr3ssapp.network.models.CreateRecurringScheduleRequest
import com.example.progr3ssapp.network.models.CreateWeekdayScheduleRequest
import com.example.progr3ssapp.network.models.HabitResponse
import com.example.progr3ssapp.network.userMessage
import com.example.progr3ssapp.repository.HabitRepository
import com.example.progr3ssapp.repository.ScheduleRepository
import com.example.progr3ssapp.utils.DateTimeUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// CREATE SCHEDULE
// ==========================================

enum class RepeatOption(val label: String) {
    ONCE("Once"),
    DAILY("Every day"),
    WEEKDAYS("Weekdays"),
    WEEKENDS("Weekends"),
    CUSTOM("Custom")
}

data class CreateScheduleUiState(
    val habits: List<HabitResponse> = emptyList(),
    val selectedHabitId: Long? = null,
    val date: String = DateTimeUtils.today(),
    val startTime: String = "08:00",
    val repeat: RepeatOption = RepeatOption.DAILY,
    // Custom: 1 = hétfő ... 7 = vasárnap
    val customDays: Set<Int> = setOf(1, 3, 5),
    val goalAmount: String = "30",
    val goalInHours: Boolean = false,
    val partnerIds: String = "",
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

class CreateScheduleViewModel : ViewModel() {

    private val habitRepository = HabitRepository()
    private val scheduleRepository = ScheduleRepository()

    private val _uiState = MutableStateFlow(CreateScheduleUiState())
    val uiState = _uiState.asStateFlow()

    // GET /habit – minden visszatéréskor újratöltjük (új szokás után is)
    fun loadHabits(selectNewest: Boolean = false) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            habitRepository.getHabits()
                .onSuccess { habits ->
                    _uiState.update { state ->
                        val selected = when {
                            selectNewest && habits.size > state.habits.size && state.habits.isNotEmpty() ->
                                habits.maxByOrNull { it.id }?.id
                            state.selectedHabitId != null &&
                                    habits.any { it.id == state.selectedHabitId } ->
                                state.selectedHabitId
                            else -> null
                        }
                        state.copy(habits = habits, selectedHabitId = selected, isLoading = false)
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(isLoading = false, error = e.userMessage("Nem sikerült betölteni a szokásokat."))
                    }
                }
        }
    }

    fun onHabitSelected(id: Long) = _uiState.update { it.copy(selectedHabitId = id, error = null) }
    fun onDateChange(v: String) = _uiState.update { it.copy(date = v, error = null) }
    fun onStartTimeChange(v: String) = _uiState.update { it.copy(startTime = v, error = null) }
    fun onRepeatChange(v: RepeatOption) = _uiState.update { it.copy(repeat = v, error = null) }
    fun onNotesChange(v: String) = _uiState.update { it.copy(notes = v) }
    fun onGoalUnitChange(hours: Boolean) = _uiState.update { it.copy(goalInHours = hours) }

    fun onGoalAmountChange(v: String) {
        if (v.all { it.isDigit() } && v.length <= 4) {
            _uiState.update { it.copy(goalAmount = v, error = null) }
        }
    }

    fun onPartnerIdsChange(v: String) {
        if (v.all { it.isDigit() || it == ',' || it == ' ' }) {
            _uiState.update { it.copy(partnerIds = v) }
        }
    }

    fun toggleDay(day: Int) = _uiState.update {
        val days = if (day in it.customDays) it.customDays - day else it.customDays + day
        it.copy(customDays = days, error = null)
    }

    fun createSchedule() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || state.isSaved) return

        val amount = state.goalAmount.toIntOrNull()
        val duration = amount?.let { if (state.goalInHours) it * 60 else it }

        val validation = when {
            state.selectedHabitId == null -> "Válassz egy szokást!"
            duration == null || duration <= 0 -> "Add meg a cél időtartamát!"
            state.repeat == RepeatOption.CUSTOM && state.customDays.isEmpty() ->
                "Válassz legalább egy napot!"
            else -> null
        }
        if (validation != null) {
            _uiState.update { it.copy(error = validation) }
            return
        }

        // Egyszeri esetén a választott nap, ismétlődőnél a mai nap az első.
        val firstDate = if (state.repeat == RepeatOption.ONCE) state.date else DateTimeUtils.today()
        val startIso = DateTimeUtils.toUtcIso(firstDate, state.startTime)
        if (startIso == null) {
            _uiState.update { it.copy(error = "Érvénytelen dátum vagy időpont.") }
            return
        }
        val endIso = DateTimeUtils.plusMinutes(startIso, duration!!)
        val habitId = state.selectedHabitId!!
        val partners = state.partnerIds.split(',', ' ')
            .mapNotNull { it.trim().toLongOrNull() }
            .distinct()
        val notes = state.notes.trim().ifBlank { null }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result: Result<Any> = when (state.repeat) {
                RepeatOption.ONCE -> scheduleRepository.createCustomSchedule(
                    CreateCustomScheduleRequest(
                        habitId = habitId,
                        date = startIso,
                        startTime = startIso,
                        endTime = endIso,
                        durationMinutes = duration,
                        participantIds = partners,
                        notes = notes
                    )
                )

                RepeatOption.CUSTOM -> scheduleRepository.createWeekdaySchedule(
                    CreateWeekdayScheduleRequest(
                        habitId = habitId,
                        startTime = startIso,
                        endTime = endIso,
                        durationMinutes = duration,
                        daysOfWeek = state.customDays.sorted(),
                        numberOfWeeks = 4,
                        participantIds = partners,
                        notes = notes
                    )
                )

                else -> scheduleRepository.createRecurringSchedule(
                    CreateRecurringScheduleRequest(
                        habitId = habitId,
                        startTime = startIso,
                        endTime = endIso,
                        durationMinutes = duration,
                        repeatPattern = when (state.repeat) {
                            RepeatOption.WEEKDAYS -> "weekdays"
                            RepeatOption.WEEKENDS -> "weekends"
                            else -> "daily"
                        },
                        repeatDays = 30,
                        participantIds = partners,
                        notes = notes
                    )
                )
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _uiState.update {
                        it.copy(isSaving = false, error = e.userMessage("Nem sikerült létrehozni az ütemezést."))
                    }
                }
        }
    }
}

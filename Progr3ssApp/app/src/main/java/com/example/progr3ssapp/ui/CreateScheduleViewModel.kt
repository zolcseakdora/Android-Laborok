
package com.example.progr3ssapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progr3ssapp.network.models.HabitResponse
import com.example.progr3ssapp.repository.HabitRepository
import com.example.progr3ssapp.repository.ScheduleRepository
import com.example.progr3ssapp.utils.DateTimeUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ==========================================
// 7. LABOR - CREATE SCHEDULE VIEWMODEL
// ==========================================

// Az űrlap összes adatát itt tároljuk.
data class CreateScheduleUiState(
    val habits: List<HabitResponse> = emptyList(),
    val selectedHabitId: Long? = null,

    // Alapértelmezett dátum: mai nap
    val date: String = DateTimeUtils.today(),

    // Alapértelmezett idő: aktuális helyi idő
    val startTime: String = SimpleDateFormat(
        "HH:mm",
        Locale.US
    ).format(Date()),

    val durationMinutes: String = "30",
    val repeatPattern: String = "daily",
    val notes: String = "",

    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

class CreateScheduleViewModel : ViewModel() {

    private val habitRepository = HabitRepository()
    private val scheduleRepository = ScheduleRepository()

    private val _uiState =
        MutableStateFlow(CreateScheduleUiState())

    val uiState = _uiState.asStateFlow()

    // Lehetséges ismétlődési minták
    companion object {
        const val REPEAT_NONE = "none"
        const val REPEAT_DAILY = "daily"
        const val REPEAT_WEEKDAYS = "weekdays"
        const val REPEAT_WEEKENDS = "weekends"
    }

    // A képernyő megnyitásakor betöltjük
    // a felhasználó meglévő szokásait.
    init {
        loadHabits()
    }

    // ======================================
    // SZOKÁSOK BETÖLTÉSE
    // GET /habit
    // ======================================

    fun loadHabits() {

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {

            habitRepository.getHabits()

                .onSuccess { habits ->

                    _uiState.update { state ->

                        state.copy(
                            habits = habits,

                            selectedHabitId =
                                state.selectedHabitId
                                    ?.takeIf { selected ->
                                        habits.any {
                                            it.id == selected
                                        }
                                    }
                                    ?: habits.firstOrNull()?.id,

                            isLoading = false
                        )
                    }
                }

                .onFailure { exception ->

                    if (exception is CancellationException) {
                        throw exception
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                                ?: "Nem sikerült betölteni a szokásokat."
                        )
                    }
                }
        }
    }

    // ======================================
    // ŰRLAP ADATAINAK MÓDOSÍTÁSA
    // ======================================

    fun onHabitSelected(id: Long) {
        _uiState.update {
            it.copy(
                selectedHabitId = id,
                error = null
            )
        }
    }

    fun onDateChange(value: String) {
        _uiState.update {
            it.copy(date = value, error = null)
        }
    }

    fun onStartTimeChange(value: String) {
        _uiState.update {
            it.copy(startTime = value, error = null)
        }
    }

    fun onDurationChange(value: String) {

        // Csak számjegyeket engedünk.
        if (value.all { it.isDigit() }) {

            _uiState.update {
                it.copy(
                    durationMinutes = value,
                    error = null
                )
            }
        }
    }

    fun onRepeatPatternChange(value: String) {

        if (
            value !in listOf(
                REPEAT_NONE,
                REPEAT_DAILY,
                REPEAT_WEEKDAYS,
                REPEAT_WEEKENDS
            )
        ) return

        _uiState.update {
            it.copy(
                repeatPattern = value,
                error = null
            )
        }
    }

    fun onNotesChange(value: String) {
        _uiState.update {
            it.copy(notes = value, error = null)
        }
    }

    // ======================================
    // UTC IDŐPONT VÉGÉNEK KISZÁMÍTÁSA
    // ======================================

    private fun calculateEndTime(
        startIso: String,
        duration: Int
    ): String {

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }

        val startDate = requireNotNull(
            formatter.parse(startIso)
        )

        val calendar = Calendar.getInstance(
            TimeZone.getTimeZone("UTC")
        ).apply {
            time = startDate
            add(Calendar.MINUTE, duration)
        }

        return formatter.format(calendar.time)
    }

    // ======================================
    // ÜTEMEZÉS LÉTREHOZÁSA
    // ======================================

    fun createSchedule() {

        val state = _uiState.value

        // A dupla mentés megakadályozása
        if (
            state.isLoading ||
            state.isSaving ||
            state.isSaved
        ) return

        val duration =
            state.durationMinutes.toIntOrNull()

        // Kötelező adatok ellenőrzése
        val validationError = when {

            state.selectedHabitId == null ->
                "Válassz egy szokást!"

            state.date.isBlank() ->
                "Válassz dátumot!"

            state.startTime.isBlank() ->
                "Válassz kezdési időpontot!"

            duration == null || duration <= 0 ->
                "Az időtartam legalább 1 perc legyen!"

            else -> null
        }

        if (validationError != null) {

            _uiState.update {
                it.copy(error = validationError)
            }

            return
        }

        // A helyi dátumot és időt
        // UTC ISO formátumra alakítjuk.
        val startIso = DateTimeUtils.toUtcIso(
            state.date,
            state.startTime
        )

        if (startIso == null) {

            _uiState.update {
                it.copy(
                    error = "Érvénytelen dátum vagy időpont."
                )
            }

            return
        }

        val validDuration = duration ?: return
        val habitId = state.selectedHabitId ?: return

        val endIso = try {
            calculateEndTime(
                startIso,
                validDuration
            )
        } catch (e: Exception) {

            _uiState.update {
                it.copy(
                    error = "Nem sikerült kiszámítani a befejezési időt."
                )
            }

            return
        }

        val notes = state.notes.trim()
            .ifBlank { null }

        // Mentés elindítása
        _uiState.update {
            it.copy(
                isSaving = true,
                error = null
            )
        }

        viewModelScope.launch {

            // Egyszeri ütemezés esetén:
            // POST /schedule/custom
            //
            // Ismétlődő ütemezés esetén:
            // POST /schedule/recurring

            val result =
                if (state.repeatPattern == REPEAT_NONE) {

                    scheduleRepository.createCustomSchedule(
                        habitId = habitId,
                        date = state.date,
                        startTime = startIso,
                        endTime = endIso,
                        durationMinutes = validDuration,
                        notes = notes
                    ).map { listOf(it) }

                } else {

                    scheduleRepository.createRecurringSchedule(
                        habitId = habitId,
                        startTime = startIso,
                        endTime = endIso,
                        durationMinutes = validDuration,
                        repeatPattern = state.repeatPattern,
                        repeatDays = 30,
                        notes = notes
                    )
                }

            result
                .onSuccess {

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
                                ?: "Nem sikerült létrehozni az ütemezést."
                        )
                    }
                }
        }
    }
}

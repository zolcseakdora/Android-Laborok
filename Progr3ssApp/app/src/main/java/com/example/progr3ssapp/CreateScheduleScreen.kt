
package com.example.progr3ssapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.ui.CreateScheduleViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ==========================================
// 7. LABOR - CREATE SCHEDULE SCREEN
// ==========================================

@Composable
fun CreateScheduleScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNavigateToAddHabit: () -> Unit,
    scheduleViewModel: CreateScheduleViewModel = viewModel()
) {

    val state by scheduleViewModel.uiState
        .collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Sikeres mentés után vissza a Home oldalra.
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ==================================
        // CÍM
        // ==================================

        Text(
            text = "Create Schedule",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Schedule one of your habits.",
            style = MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider()

        // ==================================
        // SZOKÁS KIVÁLASZTÁSA
        // ==================================

        Text(
            text = "Select Habit",
            style = MaterialTheme.typography.titleMedium
        )

        if (state.isLoading) {

            CircularProgressIndicator()
            Text("Loading habits...")

        } else if (state.habits.isEmpty()) {

            Text("No habits available.")

            OutlinedButton(
                onClick = scheduleViewModel::loadHabits
            ) {
                Text("Retry")
            }

            Button(
                onClick = onNavigateToAddHabit
            ) {
                Text("Add Habit First")
            }

        } else {

            LazyRow(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),

                modifier = Modifier.fillMaxWidth()
            ) {

                items(
                    items = state.habits,
                    key = { it.id }
                ) { habit ->

                    FilterChip(
                        selected =
                            state.selectedHabitId == habit.id,

                        onClick = {
                            scheduleViewModel.onHabitSelected(
                                habit.id
                            )
                        },

                        label = {
                            Text(habit.name)
                        },

                        enabled = !state.isSaving
                    )
                }
            }
        }

        // ==================================
        // DÁTUM KIVÁLASZTÁSA
        // ==================================

        Text(
            text = "Date",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(
            onClick = {
                showScheduleDatePicker(
                    context = context,
                    currentDate = state.date,
                    onSelected =
                        scheduleViewModel::onDateChange
                )
            },

            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Date: ${state.date}")
        }

        // ==================================
        // KEZDÉSI IDŐPONT
        // ==================================

        Text(
            text = "Start Time",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(
            onClick = {
                showScheduleTimePicker(
                    context = context,
                    currentTime = state.startTime,
                    onSelected =
                        scheduleViewModel::onStartTimeChange
                )
            },

            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Time: ${state.startTime}")
        }

        // ==================================
        // IDŐTARTAM
        // ==================================

        OutlinedTextField(
            value = state.durationMinutes,

            onValueChange =
                scheduleViewModel::onDurationChange,

            label = {
                Text("Duration (minutes)")
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),

            singleLine = true,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        // ==================================
        // ISMÉTLŐDÉS
        // ==================================

        Text(
            text = "Repeat",
            style = MaterialTheme.typography.titleMedium
        )

        val repeatOptions = listOf(
            "none" to "Once",
            "daily" to "Daily",
            "weekdays" to "Weekdays",
            "weekends" to "Weekends"
        )

        LazyRow(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),

            modifier = Modifier.fillMaxWidth()
        ) {

            items(repeatOptions) { option ->

                FilterChip(
                    selected =
                        state.repeatPattern == option.first,

                    onClick = {
                        scheduleViewModel.onRepeatPatternChange(
                            option.first
                        )
                    },

                    label = {
                        Text(option.second)
                    },

                    enabled = !state.isSaving
                )
            }
        }

        if (state.repeatPattern != "none") {

            Text(
                text = "This schedule repeats for 30 days.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // ==================================
        // MEGJEGYZÉS
        // ==================================

        OutlinedTextField(
            value = state.notes,

            onValueChange =
                scheduleViewModel::onNotesChange,

            label = {
                Text("Notes (optional)")
            },

            placeholder = {
                Text("e.g. Morning run at the park")
            },

            minLines = 2,
            maxLines = 4,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        // ==================================
        // HIBAÜZENET
        // ==================================

        state.error?.let { error ->

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        // ==================================
        // MENTÉS
        // ==================================

        Button(
            onClick = scheduleViewModel::createSchedule,

            enabled =
                !state.isLoading &&
                        !state.isSaving &&
                        !state.isSaved &&
                        state.selectedHabitId != null,

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (state.isSaving) {
                    "Saving..."
                } else {
                    "Create Schedule"
                }
            )
        }

        if (state.isSaving) {

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ==================================
        // VISSZA A HOME OLDALRA
        // ==================================

        OutlinedButton(
            onClick = onBack,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}

// ==========================================
// ANDROID DÁTUMVÁLASZTÓ
// ==========================================

private fun showScheduleDatePicker(
    context: Context,
    currentDate: String,
    onSelected: (String) -> Unit
) {

    val calendar = Calendar.getInstance()

    val formatter = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).apply {
        isLenient = false
    }

    val parsed = try {
        formatter.parse(currentDate)
    } catch (_: Exception) {
        null
    }

    if (parsed != null) {
        calendar.time = parsed
    }

    DatePickerDialog(
        context,

        { _, year, month, day ->

            val selectedDate = String.format(
                Locale.US,
                "%04d-%02d-%02d",
                year,
                month + 1,
                day
            )

            onSelected(selectedDate)
        },

        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

// ==========================================
// ANDROID IDŐVÁLASZTÓ
// ==========================================

private fun showScheduleTimePicker(
    context: Context,
    currentTime: String,
    onSelected: (String) -> Unit
) {

    val parts = currentTime.split(":")

    val hour = parts.getOrNull(0)
        ?.toIntOrNull() ?: 9

    val minute = parts.getOrNull(1)
        ?.toIntOrNull() ?: 0

    TimePickerDialog(
        context,

        { _, selectedHour, selectedMinute ->

            val selectedTime = String.format(
                Locale.US,
                "%02d:%02d",
                selectedHour,
                selectedMinute
            )

            onSelected(selectedTime)
        },

        hour,
        minute,
        true
    ).show()
}

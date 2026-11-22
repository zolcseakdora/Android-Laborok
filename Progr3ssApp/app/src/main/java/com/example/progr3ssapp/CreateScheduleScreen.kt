package com.example.progr3ssapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.ui.CreateScheduleViewModel
import com.example.progr3ssapp.ui.RepeatOption
import com.example.progr3ssapp.ui.components.AppCard
import com.example.progr3ssapp.ui.components.AppTextField
import com.example.progr3ssapp.ui.components.AppTopBar
import com.example.progr3ssapp.ui.components.ErrorText
import com.example.progr3ssapp.ui.components.FieldLabel
import com.example.progr3ssapp.ui.components.PickerField
import com.example.progr3ssapp.ui.components.PrimaryButton
import com.example.progr3ssapp.ui.components.SecondaryButton
import com.example.progr3ssapp.ui.components.SelectChip
import com.example.progr3ssapp.ui.components.categoryEmoji
import com.example.progr3ssapp.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ==========================================
// CREATE SCHEDULE
// ==========================================

@Composable
fun CreateScheduleScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNavigateToAddHabit: () -> Unit,
    scheduleViewModel: CreateScheduleViewModel = viewModel()
) {
    val state by scheduleViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Visszatéréskor (pl. új szokás után) újratöltjük a listát.
    LaunchedEffect(Unit) { scheduleViewModel.loadHabits(selectNewest = true) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        AppTopBar(title = "New Schedule", subtitle = "Plan one of your habits", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- Szokás ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HabitDropdown(
                        label = "Habit",
                        habits = state.habits.map { it.id to "${categoryEmoji(it.category?.name)}  ${it.name}" },
                        selectedId = state.selectedHabitId,
                        loading = state.isLoading,
                        onSelected = scheduleViewModel::onHabitSelected
                    )
                    Text(
                        "+ Create a new habit",
                        color = AppColors.PrimarySoft,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToAddHabit)
                            .padding(vertical = 4.dp)
                    )
                }
            }

            // ---------- Idő + ismétlés ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerField(
                        label = "Start time",
                        text = state.startTime,
                        onClick = {
                            showTimePicker(context, state.startTime, scheduleViewModel::onStartTimeChange)
                        }
                    )

                    FieldLabel("Repeat")
                    val options = RepeatOption.entries
                    options.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { option ->
                                SelectChip(
                                    text = option.label,
                                    selected = state.repeat == option,
                                    onClick = { scheduleViewModel.onRepeatChange(option) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    when (state.repeat) {
                        RepeatOption.ONCE -> PickerField(
                            label = "Date",
                            text = state.date,
                            onClick = { showDatePicker(context, state.date, scheduleViewModel::onDateChange) }
                        )

                        RepeatOption.CUSTOM -> {
                            FieldLabel("Days (for the next 4 weeks)")
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, d ->
                                    SelectChip(
                                        text = d,
                                        selected = (i + 1) in state.customDays,
                                        onClick = { scheduleViewModel.toggleDay(i + 1) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        else -> Text(
                            "Repeats for the next 30 days, starting today.",
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            // ---------- Cél időtartam ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FieldLabel("Goal duration")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTextField(
                            value = state.goalAmount,
                            onValueChange = scheduleViewModel::onGoalAmountChange,
                            placeholder = "Amount",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SelectChip(
                                "Minutes",
                                selected = !state.goalInHours,
                                onClick = { scheduleViewModel.onGoalUnitChange(false) },
                                modifier = Modifier.weight(1f).padding(top = 4.dp)
                            )
                            SelectChip(
                                "Hours",
                                selected = state.goalInHours,
                                onClick = { scheduleViewModel.onGoalUnitChange(true) },
                                modifier = Modifier.weight(1f).padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // ---------- Partnerek + jegyzet ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = state.partnerIds,
                        onValueChange = scheduleViewModel::onPartnerIdsChange,
                        label = "Partners (optional)",
                        placeholder = "User IDs, e.g. 2, 5",
                        keyboardType = KeyboardType.Number
                    )
                    AppTextField(
                        value = state.notes,
                        onValueChange = scheduleViewModel::onNotesChange,
                        label = "Notes (optional)",
                        placeholder = "e.g. Run at the park",
                        singleLine = false,
                        minLines = 2
                    )
                }
            }

            ErrorText(state.error)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = "Cancel",
                    onClick = onBack,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Create",
                    onClick = scheduleViewModel::createSchedule,
                    loading = state.isSaving,
                    enabled = !state.isLoading && state.selectedHabitId != null,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// Lenyíló lista (stabil DropdownMenu-vel)
@Composable
fun HabitDropdown(
    label: String,
    habits: List<Pair<Long, String>>,
    selectedId: Long?,
    loading: Boolean,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = habits.firstOrNull { it.first == selectedId }?.second

    Box {
        PickerField(
            label = label,
            text = when {
                loading -> "Loading habits..."
                habits.isEmpty() -> "No habits yet – create one below"
                else -> selectedText ?: "Please select a habit"
            },
            onClick = { if (habits.isNotEmpty()) expanded = true },
            enabled = !loading
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppColors.SurfaceHigh)
        ) {
            habits.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text, color = AppColors.TextPrimary) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ==========================================
// ANDROID DÁTUM- ÉS IDŐVÁLASZTÓ
// ==========================================

fun showDatePicker(context: Context, currentDate: String, onSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val parsed = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(currentDate)
    } catch (_: Exception) {
        null
    }
    if (parsed != null) calendar.time = parsed

    DatePickerDialog(
        context,
        { _, year, month, day ->
            onSelected(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }.show()
}

fun showTimePicker(context: Context, currentTime: String, onSelected: (String) -> Unit) {
    val parts = currentTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    TimePickerDialog(
        context,
        { _, h, m -> onSelected(String.format(Locale.US, "%02d:%02d", h, m)) },
        hour,
        minute,
        true
    ).show()
}


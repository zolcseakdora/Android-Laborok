package com.example.progr3ssapp

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.network.models.ProgressResponseDto
import com.example.progr3ssapp.network.models.ScheduleResponse
import com.example.progr3ssapp.ui.ScheduleDetailsViewModel
import com.example.progr3ssapp.ui.components.AccentFab
import com.example.progr3ssapp.ui.components.AddProgressDialog
import com.example.progr3ssapp.ui.components.AppCard
import com.example.progr3ssapp.ui.components.AppTextField
import com.example.progr3ssapp.ui.components.AppTopBar
import com.example.progr3ssapp.ui.components.CategoryBadge
import com.example.progr3ssapp.ui.components.CenteredLoading
import com.example.progr3ssapp.ui.components.ConfirmDialog
import com.example.progr3ssapp.ui.components.ErrorText
import com.example.progr3ssapp.ui.components.PrimaryButton
import com.example.progr3ssapp.ui.components.ProgressBar
import com.example.progr3ssapp.ui.components.RoundIconButton
import com.example.progr3ssapp.ui.components.SecondaryButton
import com.example.progr3ssapp.ui.theme.AppColors
import com.example.progr3ssapp.utils.DateTimeUtils

// ==========================================
// SCHEDULE DETAILS
// - szokás részletei, haladás sáv
// - jegyzet szerkesztése helyben (Edit Notes)
// - legutóbbi tevékenységek
// - opciók menü: Edit / Delete (megerősítéssel)
// ==========================================

@Composable
fun ScheduleDetailsScreen(
    scheduleId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: ScheduleDetailsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // A szerkesztő képernyőről visszatérve is újratöltjük.
    LaunchedEffect(scheduleId) { viewModel.loadSchedule(scheduleId) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onDeleted()
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            AppTopBar(
                title = "Schedule Details",
                onBack = onBack,
                actions = {
                    Box {
                        RoundIconButton(text = "⋮", onClick = { menuOpen = true })
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            modifier = Modifier.background(AppColors.SurfaceHigh)
                        ) {
                            DropdownMenuItem(
                                text = { Text("✎  Edit schedule", color = AppColors.TextPrimary) },
                                onClick = {
                                    menuOpen = false
                                    onEdit(scheduleId)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑  Delete", color = AppColors.Error) },
                                onClick = {
                                    menuOpen = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            )

            val schedule = state.schedule
            when {
                state.isLoading -> Box(Modifier.height(300.dp)) { CenteredLoading() }
                schedule == null -> Column(Modifier.padding(16.dp)) {
                    ErrorText(state.error ?: "Az ütemezés nem található.")
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton("Retry", onClick = { viewModel.loadSchedule(scheduleId) })
                }
                else -> DetailsContent(
                    schedule = schedule,
                    isEditingNotes = state.isEditingNotes,
                    notesDraft = state.notesDraft,
                    isSavingNotes = state.isSavingNotes,
                    error = state.error,
                    onStartEdit = viewModel::startEditNotes,
                    onCancelEdit = viewModel::cancelEditNotes,
                    onNotesChange = viewModel::onNotesChange,
                    onSaveNotes = viewModel::saveNotes
                )
            }
        }

        if (state.schedule != null) {
            AccentFab(
                text = "✎",
                onClick = viewModel::openProgress,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Schedule",
            message = "Are you sure you want to delete this schedule? This action cannot be undone.",
            confirmText = if (state.isDeleting) "Deleting..." else "Delete",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteSchedule()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (state.showProgressDialog) {
        AddProgressDialog(
            habitName = state.schedule?.habit?.name ?: "Habit",
            saving = state.isSavingProgress,
            error = state.progressError,
            onDismiss = viewModel::closeProgress,
            onSave = viewModel::saveProgress
        )
    }
}

@Composable
private fun DetailsContent(
    schedule: ScheduleResponse,
    isEditingNotes: Boolean,
    notesDraft: String,
    isSavingNotes: Boolean,
    error: String?,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit
) {
    val progress = schedule.progress.orEmpty()
    val loggedMinutes = progress.sumOf { it.loggedTime ?: 0.0 }
    val goal = schedule.durationMinutes ?: 0
    val fraction = when {
        schedule.status == "Completed" || progress.any { it.isCompleted } -> 1f
        goal > 0 -> (loggedMinutes / goal).toFloat()
        else -> 0f
    }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- Szokás kártya ----------
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(schedule.habit?.category?.name, size = 48.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            schedule.habit?.name ?: "Habit",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            "${DateTimeUtils.formatTime(schedule.startTime)} – ${DateTimeUtils.formatTime(schedule.endTime)}" +
                                    "  ·  ${DateTimeUtils.formatDate(schedule.startTime ?: schedule.date)}",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    StatusPill(schedule.status ?: "Planned")
                }

                schedule.habit?.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = AppColors.TextSecondary)
                }

                DetailRow("Goal", schedule.habit?.goal ?: "-")
                DetailRow("Duration", DateTimeUtils.formatMinutes(schedule.durationMinutes))
                DetailRow(
                    "With partner(s)",
                    schedule.participants.orEmpty()
                        .joinToString { it.name ?: it.email ?: "User #${it.id}" }
                        .ifBlank { "-" }
                )
                DetailRow("Repeat", if (schedule.isCustom) "custom" else (schedule.type ?: "recurring"))

                Spacer(Modifier.height(4.dp))
                Row {
                    Text("Progress", color = AppColors.TextSecondary, modifier = Modifier.weight(1f))
                    Text(
                        "${(fraction.coerceIn(0f, 1f) * 100).toInt()}%",
                        color = AppColors.Accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                ProgressBar(fraction)
                if (goal > 0) {
                    Text(
                        "${loggedMinutes.toInt()} of $goal minutes logged",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // ---------- Jegyzet (Edit Notes) ----------
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Notes", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                if (isEditingNotes) {
                    AppTextField(
                        value = notesDraft,
                        onValueChange = onNotesChange,
                        placeholder = "Write something about this habit...",
                        singleLine = false,
                        minLines = 4,
                        enabled = !isSavingNotes
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton(
                            "Cancel",
                            onClick = onCancelEdit,
                            enabled = !isSavingNotes,
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            "Save",
                            onClick = onSaveNotes,
                            loading = isSavingNotes,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Text(
                        schedule.notes?.takeIf { it.isNotBlank() } ?: "No notes yet.",
                        color = if (schedule.notes.isNullOrBlank()) AppColors.TextSecondary else AppColors.TextPrimary
                    )
                    Text(
                        "✎ Edit notes",
                        color = AppColors.Accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable(onClick = onStartEdit)
                            .padding(6.dp)
                    )
                }
            }
        }

        ErrorText(error)

        // ---------- Recent activity ----------
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Recent Activity", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                if (progress.isEmpty()) {
                    Text("No activity yet. Tap ✎ to add progress.", color = AppColors.TextSecondary)
                } else {
                    progress
                        .sortedByDescending { DateTimeUtils.parse(it.date ?: it.createdAt)?.time ?: 0L }
                        .take(10)
                        .forEach { ActivityRow(it, schedule.habit?.name ?: "habit") }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = AppColors.TextSecondary, modifier = Modifier.weight(1f))
        Text(value, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatusPill(status: String) {
    val color = when (status) {
        "Completed" -> AppColors.Success
        "Skipped" -> AppColors.Warning
        else -> AppColors.PrimarySoft
    }
    Text(
        status,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), androidx.compose.foundation.shape.RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun ActivityRow(p: ProgressResponseDto, habitName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (p.isCompleted) "✓" else "•",
            color = if (p.isCompleted) AppColors.Success else AppColors.TextSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(22.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                (if (p.isCompleted) "Completed " else "Progress on ") + habitName,
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                DateTimeUtils.formatDateTime(p.date ?: p.createdAt) +
                        (p.notes?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        p.loggedTime?.takeIf { it > 0 }?.let {
            Text("${it.toInt()}m", color = AppColors.Accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

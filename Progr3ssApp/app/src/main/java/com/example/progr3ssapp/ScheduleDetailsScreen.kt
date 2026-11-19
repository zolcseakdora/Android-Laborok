
package com.example.progr3ssapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.ui.ScheduleDetailsViewModel
import com.example.progr3ssapp.utils.DateTimeUtils

@Composable
fun ScheduleDetailsScreen(
    scheduleId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ScheduleDetailsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedStatus by rememberSaveable(scheduleId) {
        mutableStateOf("Planned")
    }

    var notes by rememberSaveable(scheduleId) {
        mutableStateOf("")
    }

    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(scheduleId) {
        viewModel.loadSchedule(scheduleId)
    }

    LaunchedEffect(state.schedule?.id) {
        state.schedule?.let { schedule ->
            selectedStatus = schedule.status ?: "Planned"
            notes = schedule.notes.orEmpty()
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onDeleted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Text(
            "Schedule Details",
            style = MaterialTheme.typography.headlineMedium
        )

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        state.error?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error
            )
        }

        val schedule = state.schedule

        if (schedule != null) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        schedule.habit?.name ?: "Habit",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        "Date: ${DateTimeUtils.formatDate(schedule.date)}"
                    )

                    Text(
                        "Start: ${DateTimeUtils.formatTime(schedule.startTime)}"
                    )

                    Text(
                        "End: ${DateTimeUtils.formatTime(schedule.endTime)}"
                    )

                    Text(
                        "Duration: ${schedule.durationMinutes ?: 0} minutes"
                    )
                }
            }

            Text(
                "Edit Status",
                style = MaterialTheme.typography.titleMedium
            )

            listOf("Planned", "Completed", "Skipped").forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedStatus == option,
                        onClick = { selectedStatus = option }
                    )

                    Text(option)
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    viewModel.saveChanges(
                        scheduleId,
                        selectedStatus,
                        notes
                    )
                },
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.isSaving) "Saving..."
                    else "Save Changes"
                )
            }

            Text(
                "Progress entries: ${schedule.progress.size}",
                style = MaterialTheme.typography.titleMedium
            )

            schedule.progress.take(5).forEach { progress ->
                Text(
                    "${progress.date ?: "-"} • " +
                            if (progress.isCompleted) "Completed" else "Not completed"
                )
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                enabled = !state.isSaving && !state.isDeleting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Delete Schedule",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Delete Schedule?")
            },
            text = {
                Text(
                    "Are you sure? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSchedule(scheduleId)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

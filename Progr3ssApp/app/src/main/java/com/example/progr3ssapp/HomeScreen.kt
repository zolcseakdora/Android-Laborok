package com.example.progr3ssapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.network.models.ScheduleResponse
import com.example.progr3ssapp.ui.HomeViewModel
import com.example.progr3ssapp.ui.components.AccentFab
import com.example.progr3ssapp.ui.components.AddProgressDialog
import com.example.progr3ssapp.ui.components.AppCard
import com.example.progr3ssapp.ui.components.CategoryBadge
import com.example.progr3ssapp.ui.components.ErrorText
import com.example.progr3ssapp.ui.components.SecondaryButton
import com.example.progr3ssapp.ui.components.SectionHeader
import com.example.progr3ssapp.ui.components.StatusCircle
import com.example.progr3ssapp.ui.theme.AppColors
import com.example.progr3ssapp.utils.DateTimeUtils

// ==========================================
// HOME – Today's plan
// - a mai ütemezések időrendben, napszakonként
// - státusz (kész / nincs kész), a körre kattintva Add Progress
// - + gomb: Create Schedule
// ==========================================

@Composable
fun HomeScreen(
    onOpenSchedule: (Long) -> Unit,
    onNewSchedule: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.uiState.collectAsStateWithLifecycle()

    // Minden alkalommal frissítünk, amikor a képernyő megjelenik
    // (pl. új ütemezés vagy szerkesztés után).
    LaunchedEffect(Unit) { homeViewModel.refresh() }

    val morning = state.schedules.filter { DateTimeUtils.localHour(it.startTime) < 12 }
    val afternoon = state.schedules.filter { DateTimeUtils.localHour(it.startTime) in 12..17 }
    val night = state.schedules.filter { DateTimeUtils.localHour(it.startTime) >= 18 }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column {
                    Text(
                        "Today's plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Text(DateTimeUtils.todayHeader(), color = AppColors.TextSecondary)
                    val done = state.schedules.count { it.status == "Completed" }
                    if (state.schedules.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$done / ${state.schedules.size} completed",
                            color = AppColors.Accent,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            if (state.isLoading && state.schedules.isEmpty()) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.Primary,
                        trackColor = AppColors.Surface
                    )
                }
            }

            state.errorMessage?.let { msg ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ErrorText(msg)
                        SecondaryButton("Retry", onClick = homeViewModel::refresh)
                    }
                }
            }

            if (!state.isLoading && state.errorMessage == null && state.schedules.isEmpty()) {
                item {
                    AppCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("🗓️", style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(8.dp))
                            Text("Nothing planned for today", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Tap + to schedule one of your habits.",
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }
            }

            scheduleSection("☀️", "Morning", morning, onOpenSchedule, homeViewModel::openProgress)
            scheduleSection("🌤️", "Afternoon", afternoon, onOpenSchedule, homeViewModel::openProgress)
            scheduleSection("🌙", "Night", night, onOpenSchedule, homeViewModel::openProgress)
        }

        AccentFab(
            text = "+",
            onClick = onNewSchedule,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }

    state.progressFor?.let { schedule ->
        AddProgressDialog(
            habitName = schedule.habit?.name ?: "Habit",
            saving = state.isSavingProgress,
            error = state.progressError,
            onDismiss = homeViewModel::closeProgress,
            onSave = homeViewModel::saveProgress
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.scheduleSection(
    icon: String,
    title: String,
    list: List<ScheduleResponse>,
    onOpen: (Long) -> Unit,
    onStatusClick: (ScheduleResponse) -> Unit
) {
    if (list.isEmpty()) return
    item(key = "header_$title") { SectionHeader(icon, title) }
    items(list, key = { "schedule_${it.id}" }) { schedule ->
        ScheduleRow(
            schedule = schedule,
            onClick = { onOpen(schedule.id) },
            onStatusClick = { onStatusClick(schedule) }
        )
    }
}

@Composable
fun ScheduleRow(
    schedule: ScheduleResponse,
    onClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    val completed = schedule.status == "Completed"
    val skipped = schedule.status == "Skipped"

    AppCard(onClick = onClick, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryBadge(schedule.habit?.category?.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    schedule.habit?.name ?: "Scheduled habit",
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val time = DateTimeUtils.formatTime(schedule.startTime)
                val duration = DateTimeUtils.formatMinutes(schedule.durationMinutes)
                Text(
                    if (skipped) "$time · skipped" else "$time · $duration",
                    color = if (skipped) AppColors.Warning else AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StatusCircle(completed = completed, onClick = onStatusClick)
        }
    }
}

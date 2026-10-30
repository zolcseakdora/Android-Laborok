
package com.example.progr3ssapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.ui.HomeViewModel
import com.example.progr3ssapp.utils.DateTimeUtils

// ==========================================
// 7. LABOR - GÖRGETHETŐ HOME DASHBOARD
// ==========================================

@Composable
fun HabitDashboardContent(
    onAddHabit: () -> Unit,
    onNewSchedule: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {

    val state by homeViewModel.uiState
        .collectAsStateWithLifecycle()

    // Az egész Dashboard egyetlen
    // függőlegesen görgethető lista.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),

        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp
        ),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        // ==================================
        // CÍM
        // ==================================

        item {
            Text(
                text = "My Habits",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(
                text = "Today: ${DateTimeUtils.today()}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ==================================
        // ÚJ SZOKÁS GOMB
        // ==================================

        item {
            Button(
                onClick = onAddHabit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Habit")
            }
        }

        // ==================================
        // ÚJ ÜTEMEZÉS GOMB
        // ==================================

        item {
            Button(
                onClick = onNewSchedule,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("New Schedule")
            }
        }

        // ==================================
        // FRISSÍTÉS
        // ==================================

        item {
            OutlinedButton(
                onClick = {
                    homeViewModel.refresh()
                },

                enabled = !state.isLoading,

                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh")
            }
        }

        if (state.isLoading) {

            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ==================================
        // HIBAÜZENET
        // ==================================

        state.errorMessage?.let { message ->

            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // ==================================
        // MAI ÜTEMEZÉSEK
        // ==================================

        item {
            Text(
                text = "Today's Schedule (${state.schedules.size})",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (
            state.schedules.isEmpty() &&
            !state.isLoading
        ) {

            item {
                Text("No schedules for today.")
            }

        } else {

            items(
                items = state.schedules,
                key = { "schedule_${it.id}" }
            ) { schedule ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {

                        Text(
                            text = schedule.habit?.name
                                ?: "Scheduled habit",

                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Start: ${
                                DateTimeUtils.formatTime(
                                    schedule.startTime
                                )
                            }"
                        )

                        Text(
                            text = "End: ${
                                DateTimeUtils.formatTime(
                                    schedule.endTime
                                )
                            }"
                        )

                        Text(
                            text = "Status: ${
                                schedule.status ?: "-"
                            }"
                        )
                    }
                }
            }
        }

        // ==================================
        // ÖSSZES SZOKÁS
        // ==================================

        item {
            Text(
                text = "My Habits (${state.habits.size})",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (
            state.habits.isEmpty() &&
            !state.isLoading
        ) {

            item {
                Text("No habits created yet.")
            }

        } else {

            items(
                items = state.habits,
                key = { "habit_${it.id}" }
            ) { habit ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {

                        Text(
                            text = habit.name,

                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        if (
                            !habit.description.isNullOrBlank()
                        ) {

                            Text(
                                text = habit.description
                            )
                        }

                        Text(
                            text = "Category: ${
                                habit.category.name
                            }"
                        )

                        Text(
                            text = "Goal: ${habit.goal}"
                        )
                    }
                }
            }
        }

        // Kis hely a lista alján
        item {
            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

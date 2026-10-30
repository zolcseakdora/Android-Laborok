
package com.example.progr3ssapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.ui.AddHabitViewModel

// ==========================================
// 7. LABOR - ADD HABIT SCREEN
// ==========================================

@Composable
fun AddHabitScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNavigateToCreateSchedule: () -> Unit,
    habitViewModel: AddHabitViewModel = viewModel()
) {

    val state by habitViewModel.uiState
        .collectAsStateWithLifecycle()

    // Sikeres mentés után visszatérünk
    // a Home képernyőre.
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

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        // ==================================
        // CÍM
        // ==================================

        Text(
            text = "Add New Habit",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Create a habit and set your goal.",
            style = MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider()

        // ==================================
        // SZOKÁS NEVE
        // ==================================

        OutlinedTextField(
            value = state.name,
            onValueChange = habitViewModel::onNameChange,
            label = { Text("Habit Name") },
            placeholder = {
                Text("e.g. Morning Run")
            },
            singleLine = true,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        // ==================================
        // LEÍRÁS
        // ==================================

        OutlinedTextField(
            value = state.description,
            onValueChange =
                habitViewModel::onDescriptionChange,

            label = { Text("Description (optional)") },
            placeholder = {
                Text("What will you do?")
            },

            minLines = 2,
            maxLines = 4,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        // ==================================
        // CÉL
        // ==================================

        OutlinedTextField(
            value = state.goal,
            onValueChange = habitViewModel::onGoalChange,

            label = { Text("Goal") },
            placeholder = {
                Text("e.g. Run 5 km every day")
            },

            singleLine = true,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        // ==================================
        // KATEGÓRIÁK
        // ==================================

        Text(
            text = "Select Category",
            style = MaterialTheme.typography.titleMedium
        )

        // Kategóriák betöltése
        if (state.isLoadingCategories) {

            CircularProgressIndicator()
            Text("Loading categories...")

        } else if (state.categories.isEmpty()) {

            Text("No categories available.")

            OutlinedButton(
                onClick = habitViewModel::loadCategories
            ) {
                Text("Retry")
            }

        } else {

            // A kategóriák kattintható chipként
            // jelennek meg.
            LazyRow(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),

                modifier = Modifier.fillMaxWidth()
            ) {

                items(
                    items = state.categories,
                    key = { it.id }
                ) { category ->

                    FilterChip(
                        selected =
                            state.selectedCategoryId ==
                                    category.id,

                        onClick = {
                            habitViewModel.onCategorySelected(
                                category.id
                            )
                        },

                        label = {
                            Text(category.name)
                        },

                        enabled = !state.isSaving
                    )
                }
            }
        }

        // ==================================
        // HIBAÜZENET
        // ==================================

        state.error?.let { errorMessage ->

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        // ==================================
        // MENTÉS
        // ==================================

        Button(
            onClick = habitViewModel::createHabit,

            enabled =
                !state.isSaving &&
                        !state.isLoadingCategories &&
                        state.selectedCategoryId != null,

            modifier = Modifier.fillMaxWidth()
        ) {

            if (state.isSaving) {
                Text("Saving...")
            } else {
                Text("Create Habit")
            }
        }

        if (state.isSaving) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ==================================
        // ÜTEMEZÉS OLDAL
        // ==================================

        TextButton(
            onClick = onNavigateToCreateSchedule,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Schedule an existing habit")
        }

        // ==================================
        // VISSZA
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

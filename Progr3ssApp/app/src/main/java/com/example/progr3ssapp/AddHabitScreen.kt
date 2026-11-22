package com.example.progr3ssapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.network.models.HabitCategory
import com.example.progr3ssapp.ui.AddHabitViewModel
import com.example.progr3ssapp.ui.components.AppCard
import com.example.progr3ssapp.ui.components.AppTextField
import com.example.progr3ssapp.ui.components.AppTopBar
import com.example.progr3ssapp.ui.components.ErrorText
import com.example.progr3ssapp.ui.components.FieldLabel
import com.example.progr3ssapp.ui.components.PrimaryButton
import com.example.progr3ssapp.ui.components.SecondaryButton
import com.example.progr3ssapp.ui.components.categoryEmoji
import com.example.progr3ssapp.ui.theme.AppColors

// ==========================================
// ADD HABIT – név, leírás, cél, kategória (ikon)
// ==========================================

@Composable
fun AddHabitScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    habitViewModel: AddHabitViewModel = viewModel()
) {
    val state by habitViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        AppTopBar(title = "Add Habit", subtitle = "Define a new habit", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = state.name,
                        onValueChange = habitViewModel::onNameChange,
                        label = "Habit name",
                        placeholder = "e.g. Morning Run",
                        enabled = !state.isSaving
                    )
                    AppTextField(
                        value = state.description,
                        onValueChange = habitViewModel::onDescriptionChange,
                        label = "Description",
                        placeholder = "A short motivation behind the habit",
                        singleLine = false,
                        minLines = 2,
                        enabled = !state.isSaving
                    )
                    AppTextField(
                        value = state.goal,
                        onValueChange = habitViewModel::onGoalChange,
                        label = "Goal",
                        placeholder = "e.g. Run 10 times in 2 weeks",
                        enabled = !state.isSaving
                    )
                }
            }

            AppCard {
                Column {
                    FieldLabel("Category")
                    Spacer(Modifier.height(4.dp))
                    when {
                        state.isLoadingCategories -> CircularProgressIndicator(color = AppColors.Primary)
                        state.categories.isEmpty() -> {
                            Text("No categories available.", color = AppColors.TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            SecondaryButton("Retry", onClick = habitViewModel::loadCategories)
                        }
                        else -> CategoryGrid(
                            categories = state.categories,
                            selectedId = state.selectedCategoryId,
                            enabled = !state.isSaving,
                            onSelect = habitViewModel::onCategorySelected
                        )
                    }
                }
            }

            ErrorText(state.error)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    "Cancel",
                    onClick = onBack,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    "Create",
                    onClick = habitViewModel::createHabit,
                    loading = state.isSaving,
                    enabled = !state.isLoadingCategories && state.selectedCategoryId != null,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<HabitCategory>,
    selectedId: Long?,
    enabled: Boolean,
    onSelect: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { category ->
                    val selected = category.id == selectedId
                    val shape = RoundedCornerShape(12.dp)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(if (selected) AppColors.Primary.copy(alpha = 0.25f) else AppColors.Field)
                            .border(1.dp, if (selected) AppColors.Primary else AppColors.Outline, shape)
                            .clickable(enabled = enabled) { onSelect(category.id) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(categoryEmoji(category.name), fontSize = 22.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            category.name,
                            color = AppColors.TextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

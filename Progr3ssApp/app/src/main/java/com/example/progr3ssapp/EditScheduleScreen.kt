package com.example.progr3ssapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.ui.EditScheduleViewModel
import com.example.progr3ssapp.ui.components.AppCard
import com.example.progr3ssapp.ui.components.AppTextField
import com.example.progr3ssapp.ui.components.AppTopBar
import com.example.progr3ssapp.ui.components.CenteredLoading
import com.example.progr3ssapp.ui.components.ErrorText
import com.example.progr3ssapp.ui.components.FieldLabel
import com.example.progr3ssapp.ui.components.PickerField
import com.example.progr3ssapp.ui.components.PrimaryButton
import com.example.progr3ssapp.ui.components.RoundIconButton
import com.example.progr3ssapp.ui.components.SelectChip
import com.example.progr3ssapp.ui.theme.AppColors

// ==========================================
// EDIT SCHEDULE
// kezdés / befejezés, időtartam, státusz, partnerek, jegyzet
// ==========================================

@Composable
fun EditScheduleScreen(
    scheduleId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditScheduleViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(scheduleId) { viewModel.load(scheduleId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onSaved() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        AppTopBar(
            title = "Edit Schedule",
            subtitle = state.schedule?.habit?.name,
            onBack = onBack,
            actions = {
                PrimaryButton(
                    text = "SAVE",
                    onClick = viewModel::save,
                    loading = state.isSaving,
                    enabled = state.schedule != null
                )
            }
        )

        if (state.isLoading) {
            Box(Modifier.height(300.dp)) { CenteredLoading() }
            return@Column
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- Időtartomány ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel("Time range · ${state.localDate}")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PickerField(
                            label = "Start time",
                            text = state.startTime,
                            onClick = { showTimePicker(context, state.startTime, viewModel::onStartTimeChange) },
                            modifier = Modifier.weight(1f)
                        )
                        PickerField(
                            label = "End time",
                            text = state.endTime,
                            onClick = { showTimePicker(context, state.endTime, viewModel::onEndTimeChange) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ---------- Időtartam ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel("Duration")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTextField(
                            value = state.durationAmount,
                            onValueChange = viewModel::onDurationChange,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        SelectChip(
                            "Minutes",
                            selected = !state.durationInHours,
                            onClick = { viewModel.onDurationUnitChange(false) },
                            modifier = Modifier.weight(0.7f)
                        )
                        SelectChip(
                            "Hours",
                            selected = state.durationInHours,
                            onClick = { viewModel.onDurationUnitChange(true) },
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }

            // ---------- Státusz ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel("Status")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Planned", "Skipped", "Completed").forEach { status ->
                            SelectChip(
                                status,
                                selected = state.status == status,
                                onClick = { viewModel.onStatusChange(status) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ---------- Partnerek ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldLabel("Partners")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTextField(
                            value = state.newPartnerId,
                            onValueChange = viewModel::onNewPartnerChange,
                            placeholder = "Partner user ID",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        RoundIconButton(
                            text = "+",
                            onClick = viewModel::addPartner,
                            background = AppColors.Primary,
                            size = 46.dp
                        )
                    }
                    if (state.participants.isEmpty()) {
                        Text("No partners added.", color = AppColors.TextSecondary)
                    }
                    state.participants.forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👤", modifier = Modifier.width(28.dp))
                            Text(
                                p.name ?: p.email ?: "User #${p.id}",
                                color = AppColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            RoundIconButton(text = "✕", onClick = { viewModel.removePartner(p.id) }, size = 32.dp)
                        }
                    }
                }
            }

            // ---------- Jegyzet ----------
            AppCard {
                AppTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = "Notes",
                    placeholder = "Notes about this schedule",
                    singleLine = false,
                    minLines = 3
                )
            }

            ErrorText(state.error)
            Spacer(Modifier.height(24.dp))
        }
    }
}

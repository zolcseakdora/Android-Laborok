package com.example.progr3ssapp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.network.models.HabitResponse
import com.example.progr3ssapp.session.AuthSession
import com.example.progr3ssapp.ui.EditProfileViewModel
import com.example.progr3ssapp.ui.ProfileViewModel
import com.example.progr3ssapp.ui.components.AppCard
import com.example.progr3ssapp.ui.components.AppTextField
import com.example.progr3ssapp.ui.components.AppTopBar
import com.example.progr3ssapp.ui.components.Avatar
import com.example.progr3ssapp.ui.components.CategoryBadge
import com.example.progr3ssapp.ui.components.ConfirmDialog
import com.example.progr3ssapp.ui.components.ErrorText
import com.example.progr3ssapp.ui.components.FieldShape
import com.example.progr3ssapp.ui.components.PrimaryButton
import com.example.progr3ssapp.ui.components.RoundIconButton
import com.example.progr3ssapp.ui.theme.AppColors

// ==========================================
// PROFILE
// ==========================================

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onAddHabit: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val user by AuthSession.currentUser.collectAsStateWithLifecycle()
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(state.isLoggedOut) { if (state.isLoggedOut) onLoggedOut() }

    val profile = user?.profile ?: state.profile
    val name = profile?.username?.takeIf { it.isNotBlank() } ?: user?.email ?: "User"
    val email = profile?.email ?: user?.email.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AppTopBar(
            title = "My profile",
            subtitle = "Your progress journey",
            actions = {
                Box {
                    RoundIconButton(text = "⋮", onClick = { menuOpen = true })
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        modifier = Modifier.background(AppColors.SurfaceHigh)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit profile", color = AppColors.TextPrimary) },
                            onClick = {
                                menuOpen = false
                                onEditProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Log out", color = AppColors.Error) },
                            onClick = {
                                menuOpen = false
                                confirmLogout = true
                            }
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- Profil összefoglaló ----------
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(profile = profile, size = 64.dp, fallbackText = name)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            name,
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(email, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        profile?.description?.takeIf { it.isNotBlank() }?.let {
                            Text(it, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "✎ Edit profile",
                            color = AppColors.PrimarySoft,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(FieldShape)
                                .background(AppColors.Primary.copy(alpha = 0.18f))
                                .clickable(onClick = onEditProfile)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ---------- Szokások ----------
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "My Habits",
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${state.habits.size}", color = AppColors.TextSecondary)
                    }

                    if (state.isLoading && state.habits.isEmpty()) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = AppColors.Primary,
                            trackColor = AppColors.SurfaceHigh
                        )
                    }

                    if (!state.isLoading && state.habits.isEmpty()) {
                        Text("You have no habits yet.", color = AppColors.TextSecondary)
                    }

                    state.habits.forEach { HabitRow(it) }

                    Text(
                        "+  Add New Habit",
                        color = AppColors.PrimarySoft,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FieldShape)
                            .border(1.dp, AppColors.Primary, FieldShape)
                            .clickable(onClick = onAddHabit)
                            .padding(vertical = 12.dp)
                    )
                }
            }

            ErrorText(state.error)
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "Log out",
            message = "Are you sure you want to log out?",
            confirmText = "Log out",
            onConfirm = {
                confirmLogout = false
                viewModel.logout()
            },
            onDismiss = { confirmLogout = false }
        )
    }
}

@Composable
private fun HabitRow(habit: HabitResponse) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CategoryBadge(habit.category?.name, size = 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                habit.name,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                listOfNotNull(habit.category?.name, habit.goal?.takeIf { it.isNotBlank() })
                    .joinToString(" · "),
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// EDIT PROFILE – új felhasználónév, email, profilkép
// ==========================================

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.uploadImage(uri)
    }

    LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        AppTopBar(title = "Edit profile", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Avatar(
                        profile = state.profile,
                        size = 96.dp,
                        fallbackText = state.username.ifBlank { state.email }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (state.isUploading) "Uploading..." else "Change photo",
                        color = AppColors.PrimarySoft,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(enabled = !state.isUploading) {
                                picker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(6.dp)
                    )
                    state.message?.let { Text(it, color = AppColors.Success) }
                }
            }

            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = "Username",
                        placeholder = "New username",
                        enabled = !state.isSaving
                    )
                    AppTextField(
                        value = state.email,
                        onValueChange = {},
                        label = "Email",
                        readOnly = true,
                        enabled = false
                    )
                }
            }

            ErrorText(state.error)

            PrimaryButton(
                text = "Save",
                onClick = viewModel::save,
                loading = state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ==========================================
// AI ASSISTANT (opcionális – csak helykitöltő)
// ==========================================

@Composable
fun AssistantScreen() {
    Column(Modifier.fillMaxSize()) {
        AppTopBar(title = "Daily Check-in", subtitle = "AI assistant")
        Column(Modifier.padding(16.dp)) {
            AppCard {
                Row {
                    Text("🤖", modifier = Modifier.width(32.dp))
                    Text(
                        "Hi! I'm your health assistant. This feature is optional in the specification " +
                                "and is not connected to the OpenAI API yet.",
                        color = AppColors.TextPrimary
                    )
                }
            }
        }
    }
}

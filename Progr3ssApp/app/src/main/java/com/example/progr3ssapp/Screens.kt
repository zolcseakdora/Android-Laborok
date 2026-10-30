package com.example.progr3ssapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.session.AuthSession
import com.example.progr3ssapp.ui.LoginUiState
import com.example.progr3ssapp.ui.LoginViewModel
import com.example.progr3ssapp.ui.RegisterUiState
import com.example.progr3ssapp.ui.RegisterViewModel
import androidx.compose.foundation.layout.Box

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val loading = uiState is LoginUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            loginViewModel.resetState()
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Progr3SS", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true, enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true, enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { loginViewModel.login(email.trim(), password) },
            enabled = !loading
        ) { Text(if (loading) "Bejelentkezes..." else "Login") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNavigateToRegister, enabled = !loading) {
            Text("Create account")
        }
        if (loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }
        (uiState as? LoginUiState.Error)?.let {
            Text(it.message, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable

fun HomeScreen(
    onNavigateToProfile: (String, String) -> Unit,
    onAddHabit: () -> Unit,
    onNewSchedule: () -> Unit
) {
    // Bejelentkezett felhasználó adatai
    val user by AuthSession.currentUser
        .collectAsStateWithLifecycle()

    val id = user?.id?.toString().orEmpty()

    val name = user?.profile?.username
        ?.takeIf { it.isNotBlank() }
        ?: user?.email
        ?: "Ismeretlen felhasználó"

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // ====================================
        // FELHASZNÁLÓI ADATOK
        // ====================================

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 12.dp,
                    bottom = 6.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Bejelentkezve: $name",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            // A régi View Profile gomb megmarad
            Button(
                onClick = {
                    onNavigateToProfile(id, name)
                },
                enabled = user != null
            ) {
                Text("View Profile")
            }
        }

        // ====================================
        // ÚJ HABIT DASHBOARD - 7. LABOR
        // ====================================

        Box(
            modifier = Modifier.weight(1f)
        ) {
            HabitDashboardContent(
                onAddHabit = onAddHabit,
                onNewSchedule = onNewSchedule
            )
        }
    }
}


@Composable
fun ProfileScreen(userId: String, userName: String, onLogout: () -> Unit) {
    val user by AuthSession.currentUser.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("ID: $userId")
        Text("Name: $userName")
        Text("Email: ${user?.email.orEmpty()}")
        Spacer(Modifier.height(20.dp))
        Button(onClick = onLogout) { Text("Logout") }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    registerViewModel: RegisterViewModel = viewModel()
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val state by registerViewModel.uiState.collectAsStateWithLifecycle()
    val loading = state is RegisterUiState.Loading

    LaunchedEffect(state) {
        if (state is RegisterUiState.Success) {
            registerViewModel.resetState()
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Progr3SS Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text("Username") }, enabled = !loading,
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, enabled = !loading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !loading, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = { registerViewModel.register(username, email, password) },
            enabled = !loading) {
            Text(if (loading) "Registering..." else "Register")
        }
        if (loading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }
        (state as? RegisterUiState.Error)?.let {
            Text(it.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBackToLogin, enabled = !loading) {
            Text("Back to Login")
        }
    }
}

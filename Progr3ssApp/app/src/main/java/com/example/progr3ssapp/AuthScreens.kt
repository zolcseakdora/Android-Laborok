package com.example.progr3ssapp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progr3ssapp.session.SessionManager
import com.example.progr3ssapp.ui.LoginUiState
import com.example.progr3ssapp.ui.LoginViewModel
import com.example.progr3ssapp.ui.RegisterUiState
import com.example.progr3ssapp.ui.RegisterViewModel
import com.example.progr3ssapp.ui.ResetPasswordViewModel
import com.example.progr3ssapp.ui.components.AppCard
import com.example.progr3ssapp.ui.components.AppTextField
import com.example.progr3ssapp.ui.components.AppTopBar
import com.example.progr3ssapp.ui.components.ErrorText
import com.example.progr3ssapp.ui.components.PrimaryButton
import com.example.progr3ssapp.ui.theme.AppColors
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// SPLASH – animáció + automatikus bejelentkezés
// ==========================================

@Composable
fun SplashScreen(
    onLoggedIn: () -> Unit,
    onNeedLogin: () -> Unit
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Az animáció és a login ellenőrzés párhuzamosan fut.
        val anim = launch {
            launch { alpha.animateTo(1f, tween(700)) }
            scale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
        val loggedIn = async {
            runCatching { SessionManager.tryAutoLogin() }.getOrDefault(false)
        }
        delay(1200)
        anim.join()
        if (loggedIn.await()) onLoggedIn() else onNeedLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            AppLogoBadge(size = 110)
            Spacer(Modifier.height(20.dp))
            AppWordmark(fontSize = 34)
            Spacer(Modifier.height(6.dp))
            Text("Habit Planner & Tracker", color = AppColors.TextSecondary)
        }
    }
}

@Composable
fun AppLogoBadge(size: Int = 72) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 4).dp))
            .background(AppColors.Primary),
        contentAlignment = Alignment.Center
    ) {
        Text("📈", fontSize = (size * 0.45f).sp)
    }
}

// "Progr3SS" – a 3-as kiemelve
@Composable
fun AppWordmark(fontSize: Int = 28) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = AppColors.TextPrimary)) { append("Progr") }
            withStyle(SpanStyle(color = AppColors.Accent)) { append("3") }
            withStyle(SpanStyle(color = AppColors.TextPrimary)) { append("SS") }
        },
        fontSize = fontSize.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

// Login / Register fül váltó
@Composable
private fun AuthTabs(loginSelected: Boolean, onLogin: () -> Unit, onRegister: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.Surface)
            .padding(4.dp)
    ) {
        listOf("Login" to loginSelected, "Register" to !loginSelected).forEach { (label, selected) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) AppColors.Primary else Color.Transparent)
                    .clickable { if (label == "Login") onLogin() else onRegister() }
                    .padding(horizontal = 26.dp, vertical = 9.dp)
            ) {
                Text(
                    label,
                    color = if (selected) Color.White else AppColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AuthHeader(loginSelected: Boolean, onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(32.dp))
        AppWordmark()
        Text("Habit Planner & Tracker", color = AppColors.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        AuthTabs(loginSelected, onLogin, onRegister)
        Spacer(Modifier.height(24.dp))
    }
}

// ==========================================
// LOGIN
// ==========================================

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit,
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AuthHeader(loginSelected = true, onLogin = {}, onRegister = onNavigateToRegister)

        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "you@example.com",
            keyboardType = KeyboardType.Email,
            enabled = !loading
        )
        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Your password",
            isPassword = true,
            enabled = !loading
        )
        Text(
            "Forgot password?",
            color = AppColors.PrimarySoft,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !loading, onClick = onForgotPassword)
                .padding(vertical = 4.dp)
        )

        ErrorText((uiState as? LoginUiState.Error)?.message)

        PrimaryButton(
            text = "Log in  →",
            onClick = { loginViewModel.login(email, password) },
            loading = loading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
    }
}

// ==========================================
// REGISTER
// ==========================================

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    registerViewModel: RegisterViewModel = viewModel()
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val state by registerViewModel.uiState.collectAsStateWithLifecycle()
    val loading = state is RegisterUiState.Loading

    // Eltérő jelszavak: piros keret + segítő szöveg
    val mismatch = confirm.isNotEmpty() && password != confirm

    LaunchedEffect(state) {
        if (state is RegisterUiState.Success) {
            registerViewModel.resetState()
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AuthHeader(loginSelected = false, onLogin = onBackToLogin, onRegister = {})

        AppTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            placeholder = "Your username",
            enabled = !loading
        )
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "you@example.com",
            keyboardType = KeyboardType.Email,
            enabled = !loading
        )
        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "At least 6 characters",
            isPassword = true,
            isError = mismatch,
            enabled = !loading
        )
        AppTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = "Confirm password",
            placeholder = "Repeat your password",
            isPassword = true,
            isError = mismatch,
            supportingText = if (mismatch) "The passwords do not match." else null,
            enabled = !loading
        )

        ErrorText((state as? RegisterUiState.Error)?.message)

        PrimaryButton(
            text = "Create account  →",
            onClick = { registerViewModel.register(username, email, password, confirm) },
            enabled = !mismatch,
            loading = loading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
    }
}

// ==========================================
// RESET PASSWORD
// ==========================================

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    viewModel: ResetPasswordViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        AppTopBar(title = "Reset password", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Enter the email address of your account. We will generate a new password and send it to you.",
                color = AppColors.TextSecondary
            )

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email,
                enabled = !state.isLoading
            )

            ErrorText(state.error)

            val sentTo = state.sentTo
            if (sentTo != null) {
                AppCard {
                    Column {
                        Text("✉️  Check your inbox", color = AppColors.Success, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "A new password has been sent to $sentTo. Log in with it and change it as soon as possible.",
                            color = AppColors.TextPrimary
                        )
                    }
                }
                PrimaryButton(
                    text = "Back to login",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PrimaryButton(
                    text = "Send new password",
                    onClick = { viewModel.reset(email) },
                    loading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

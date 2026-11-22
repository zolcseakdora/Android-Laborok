package com.example.progr3ssapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.progr3ssapp.session.AuthSession
import com.example.progr3ssapp.ui.theme.AppColors

// ==========================================
// NAVIGÁCIÓ
// Splash -> Login/Register/Reset -> MainGraph (Home, Assistant, Profile)
// ==========================================

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val user by AuthSession.currentUser.collectAsStateWithLifecycle()

    val insideMain = currentDestination?.hierarchy?.any { it.hasRoute<MainGraph>() } == true

    val showBottomBar = insideMain && user != null && (
            currentDestination?.hasRoute<Home>() == true ||
                    currentDestination?.hasRoute<Assistant>() == true ||
                    currentDestination?.hasRoute<Profile>() == true
            )

    // Védett képernyők: kijelentkezett felhasználót a Login-ra küldjük.
    LaunchedEffect(insideMain, user) {
        if (insideMain && user == null) {
            navController.navigate(Login) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = AppColors.Surface) {
                    BottomItem("Home", "🏠", currentDestination?.hasRoute<Home>() == true) {
                        navController.switchTab(Home)
                    }
                    BottomItem("Assistant", "🤖", currentDestination?.hasRoute<Assistant>() == true) {
                        navController.switchTab(Assistant)
                    }
                    BottomItem("Profile", "👤", currentDestination?.hasRoute<Profile>() == true) {
                        navController.switchTab(Profile)
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Splash,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ---------------- SPLASH ----------------
            composable<Splash> {
                SplashScreen(
                    onLoggedIn = {
                        navController.navigate(MainGraph) {
                            popUpTo<Splash> { inclusive = true }
                        }
                    },
                    onNeedLogin = {
                        navController.navigate(Login) {
                            popUpTo<Splash> { inclusive = true }
                        }
                    }
                )
            }

            // ---------------- AUTH ----------------
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = { navController.goToMain() },
                    onNavigateToRegister = {
                        navController.navigate(Register) {
                            popUpTo<Login> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onForgotPassword = { navController.navigate(ResetPassword) }
                )
            }

            composable<Register> {
                RegisterScreen(
                    // Sikeres regisztráció után egyből a Home képernyő
                    onRegisterSuccess = { navController.goToMain() },
                    onBackToLogin = {
                        navController.navigate(Login) {
                            popUpTo<Register> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<ResetPassword> {
                ResetPasswordScreen(onBack = { navController.popBackStack() })
            }

            // ---------------- MAIN (bejelentkezve) ----------------
            navigation<MainGraph>(startDestination = Home) {

                composable<Home> {
                    if (user != null) {
                        HomeScreen(
                            onOpenSchedule = { id -> navController.navigate(ScheduleDetails(id)) },
                            onNewSchedule = { navController.navigate(CreateSchedule) }
                        )
                    }
                }

                composable<Assistant> {
                    AssistantScreen()
                }

                composable<Profile> {
                    if (user != null) {
                        ProfileScreen(
                            onEditProfile = { navController.navigate(EditProfile) },
                            onAddHabit = { navController.navigate(AddHabit) },
                            onLoggedOut = {
                                navController.navigate(Login) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable<AddHabit> {
                    AddHabitScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }

                composable<CreateSchedule> {
                    CreateScheduleScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() },
                        onNavigateToAddHabit = { navController.navigate(AddHabit) }
                    )
                }

                composable<ScheduleDetails> { entry ->
                    val route = entry.toRoute<ScheduleDetails>()
                    ScheduleDetailsScreen(
                        scheduleId = route.id,
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate(EditSchedule(id)) },
                        onDeleted = { navController.popBackStack() }
                    )
                }

                composable<EditSchedule> { entry ->
                    val route = entry.toRoute<EditSchedule>()
                    EditScheduleScreen(
                        scheduleId = route.id,
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }

                composable<EditProfile> {
                    EditProfileScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomItem(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Text(icon, fontSize = 20.sp) },
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedTextColor = AppColors.PrimarySoft,
            unselectedTextColor = AppColors.TextSecondary,
            indicatorColor = AppColors.Primary.copy(alpha = 0.3f)
        )
    )
}

// Bejelentkezés után: a Main gráf, az auth képernyők törlésével
private fun NavHostController.goToMain() {
    navigate(MainGraph) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

// Alsó navigáció fülváltás (állapot megőrzésével)
private fun NavHostController.switchTab(route: Any) {
    navigate(route) {
        popUpTo<Home> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}


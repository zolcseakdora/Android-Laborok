
package com.example.progr3ssapp

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.progr3ssapp.network.RetrofitClient
import com.example.progr3ssapp.session.AuthSession
import com.example.progr3ssapp.session.SecureSessionStore
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentDestination = backStackEntry?.destination

    val user by AuthSession.currentUser
        .collectAsStateWithLifecycle()

    val context = LocalContext.current

    val sessionStore = remember(context) {
        SecureSessionStore(context.applicationContext)
    }

    val scope = rememberCoroutineScope()

    // A bejelentkezés utáni navigációs csoport
    val insideMain =
        currentDestination?.hierarchy?.any {
            it.hasRoute<MainGraph>()
        } == true

    // Alsó navigáció csak bejelentkezés után
    val showBottomBar =
        insideMain &&
                user != null &&
                (
                        currentDestination?.hasRoute<Home>() == true ||
                                currentDestination?.hasRoute<Profile>() == true
                        )
    // Jogosulatlan hozzáférés megakadályozása
    LaunchedEffect(insideMain, user) {
        if (insideMain && user == null) {

            navController.navigate(Login) {
                popUpTo<MainGraph> {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {

            if (showBottomBar) {

                NavigationBar {

                    // ==========================
                    // HOME MENÜPONT
                    // ==========================
                    NavigationBarItem(
                        selected =
                            currentDestination?.hierarchy?.any {
                                it.hasRoute<Home>()
                            } == true,

                        onClick = {
                            navController.navigate(Home) {
                                popUpTo<MainGraph> {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },

                        icon = { Text("⌂") },
                        label = { Text("Home") }
                    )

                    // ==========================
                    // PROFILE MENÜPONT
                    // ==========================
                    NavigationBarItem(
                        selected =
                            currentDestination?.hierarchy?.any {
                                it.hasRoute<Profile>()
                            } == true,

                        onClick = {

                            val current =
                                user ?: return@NavigationBarItem

                            val name =
                                current.profile?.username
                                    ?.takeIf { it.isNotBlank() }
                                    ?: current.email
                                    ?: "Ismeretlen felhasználó"

                            navController.navigate(
                                Profile(
                                    current.id?.toString().orEmpty(),
                                    name
                                )
                            ) {
                                popUpTo<MainGraph> {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },

                        icon = { Text("◉") },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Login,
            modifier = Modifier.padding(innerPadding)
        ) {

            // =================================
            // LOGIN - 6. LABOR
            // =================================

            composable<Login> {

                LoginScreen(
                    onLoginSuccess = {

                        navController.navigate(MainGraph) {
                            popUpTo<Login> {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    },

                    onNavigateToRegister = {
                        navController.navigate(Register) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // =================================
            // REGISTER - 6. LABOR
            // =================================

            composable<Register> {

                RegisterScreen(
                    onRegisterSuccess = {
                        navController.popBackStack()
                    },

                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // =================================
            // BEJELENTKEZÉS UTÁNI KÉPERNYŐK
            // =================================

            navigation<MainGraph>(
                startDestination = Home
            ) {

                // =================================
                // HOME - 7. LABOR
                // =================================

                composable<Home> {

                    if (user != null) {

                        HomeScreen(

                            onNavigateToProfile = { id, name ->
                                navController.navigate(
                                    Profile(id, name)
                                )
                            },

                            onAddHabit = {
                                navController.navigate(AddHabit)
                            },

                            onNewSchedule = {
                                navController.navigate(CreateSchedule)
                            }
                        )
                    }
                }

                // =================================
                // ADD HABIT - 7. LABOR
                // Ide kerül a valódi képernyő
                // =================================


                composable<AddHabit> {

                    AddHabitScreen(

                        // Vissza a Home képernyőre
                        onBack = {
                            navController.popBackStack()
                        },

                        // Sikeres mentés után
                        // újratöltjük a Home képernyőt.
                        onSaved = {
                            navController.navigate(Home) {

                                popUpTo<Home> {
                                    inclusive = true
                                }

                                launchSingleTop = true
                            }
                        },

                        // Ütemezés létrehozása
                        onNavigateToCreateSchedule = {
                            navController.navigate(CreateSchedule)
                        }
                    )
                }


                // =================================
                // CREATE SCHEDULE - 7. LABOR
                // Ide kerül a valódi képernyő
                // =================================


                composable<CreateSchedule> {

                    CreateScheduleScreen(

                        // Vissza az előző képernyőre
                        onBack = {
                            navController.popBackStack()
                        },

                        // Sikeres mentés után vissza Home-ra
                        onSaved = {
                            navController.navigate(Home) {

                                popUpTo<Home> {
                                    inclusive = true
                                }

                                launchSingleTop = true
                            }
                        },

                        // Új szokás létrehozása
                        onNavigateToAddHabit = {
                            navController.navigate(AddHabit)
                        }
                    )
                }


                // =================================
                // PROFILE - 6. LABOR
                // =================================

                composable<Profile> { entry ->

                    if (user != null) {

                        val route = entry.toRoute<Profile>()

                        ProfileScreen(
                            userId = route.userId,
                            userName = route.userName,

                            // =====================
                            // LOGOUT
                            // =====================

                            onLogout = {

                                scope.launch {

                                    try {

                                        // Titkosított munkamenet törlése
                                        sessionStore.clear()

                                        // Felhasználói adatok törlése
                                        AuthSession.clear()

                                        // JWT token törlése a memóriából
                                        RetrofitClient
                                            .updateAccessToken(null)

                                        // Vissza a Login képernyőre
                                        navController.navigate(Login) {

                                            popUpTo<MainGraph> {
                                                inclusive = true
                                            }

                                            launchSingleTop = true
                                        }

                                    } catch (e: Exception) {

                                        Toast.makeText(
                                            context,
                                            "Nem sikerült törölni a helyi munkamenetet.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                }

            }

        }

    }

}
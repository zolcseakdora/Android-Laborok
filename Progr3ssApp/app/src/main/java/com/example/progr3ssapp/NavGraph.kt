package com.example.progr3ssapp

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
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
import com.example.progr3ssapp.session.AuthSession
import com.example.progr3ssapp.session.SecureSessionStore
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val user by AuthSession.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sessionStore = remember(context) { SecureSessionStore(context.applicationContext) }
    val scope = rememberCoroutineScope()

    val insideMain = currentDestination?.hierarchy?.any {
        it.hasRoute<MainGraph>()
    } == true
    val showBottomBar = insideMain && user != null

    // Ha az Android visszaallitana a regi Home/Profile routot
    // hitelesitett munkamenet nelkul, kuldjuk a Login oldalra.
    LaunchedEffect(insideMain, user) {
        if (insideMain && user == null) {
            navController.navigate(Login) {
                popUpTo<MainGraph> { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.hasRoute<Home>()
                        } == true,
                        onClick = {
                            navController.navigate(Home) {
                                popUpTo<MainGraph> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text("⌂") }, label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.hasRoute<Profile>()
                        } == true,
                        onClick = {
                            val current = user ?: return@NavigationBarItem
                            val name = current.profile?.username?.takeIf { it.isNotBlank() }
                                ?: current.email ?: "Ismeretlen felhasznalo"
                            navController.navigate(
                                Profile(current.id?.toString().orEmpty(), name)
                            ) {
                                popUpTo<MainGraph> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text("◉") }, label = { Text("Profile") }
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
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo<Login> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Register) { launchSingleTop = true }
                    }
                )
            }
            composable<Register> {
                RegisterScreen(
                    onRegisterSuccess = { navController.popBackStack() },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            navigation<MainGraph>(startDestination = Home) {
                composable<Home> {
                    if (user != null) {
                        HomeScreen { id, name ->
                            navController.navigate(Profile(id, name))
                        }
                    }
                }
                composable<Profile> { entry ->
                    if (user != null) {
                        val route = entry.toRoute<Profile>()
                        ProfileScreen(
                            userId = route.userId,
                            userName = route.userName,
                            onLogout = {
                                scope.launch {
                                    try {
                                        sessionStore.clear()
                                        AuthSession.clear()
                                        navController.navigate(Login) {
                                            popUpTo<MainGraph> { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Nem sikerult torolni a helyi munkamenetet.",
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

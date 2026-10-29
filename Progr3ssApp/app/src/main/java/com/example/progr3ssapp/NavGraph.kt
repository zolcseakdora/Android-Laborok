
package com.example.progr3ssapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentDestination = backStackEntry?.destination

    // Csak a bejelentkezés után jelenjen meg az alsó menü
    val showBottomBar =
        currentDestination?.hierarchy?.any {
            it.hasRoute<MainGraph>()
        } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {

                    // HOME menüpont
                    NavigationBarItem(
                        selected = currentDestination
                            ?.hierarchy
                            ?.any { it.hasRoute<Home>() } == true,

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

                    // PROFILE menüpont
                    NavigationBarItem(
                        selected = currentDestination
                            ?.hierarchy
                            ?.any { it.hasRoute<Profile>() } == true,

                        onClick = {
                            navController.navigate(Profile()) {
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

            // LOGIN
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo<Login> {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // Bejelentkezés utáni képernyők
            navigation<MainGraph>(
                startDestination = Home
            ) {

                // HOME
                composable<Home> {
                    HomeScreen { userId, userName ->
                        navController.navigate(
                            Profile(userId, userName)
                        )
                    }
                }

                // PROFILE
                composable<Profile> { entry ->

                    val profile: Profile = entry.toRoute()

                    ProfileScreen(
                        userId = profile.userId,
                        userName = profile.userName
                    )
                }
            }
        }
    }
}

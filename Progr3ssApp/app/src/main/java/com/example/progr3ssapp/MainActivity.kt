package com.example.progr3ssapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.progr3ssapp.session.SessionManager
import com.example.progr3ssapp.ui.theme.Progr3ssAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // A rendszer splash (ikon) után a saját animált Splash képernyő jön,
        // ami az automatikus bejelentkezést is elvégzi.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Sötét rendszersávok a sötét témához
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        SessionManager.init(applicationContext)

        setContent {
            Progr3ssAppTheme {
                AppNavGraph()
            }
        }
    }
}

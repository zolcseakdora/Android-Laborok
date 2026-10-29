package com.example.progr3ssapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Az AndroidX splash a super.onCreate ELŐTT települ.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 1,5 másodperc minimum splash idő a labor példája szerint.
        var isLoading = true
        lifecycleScope.launch {
            delay(1500L)
            isLoading = false
        }
        splashScreen.setKeepOnScreenCondition { isLoading }

        // 0,5 másodperces elhalványulás.
        splashScreen.setOnExitAnimationListener { provider ->
            ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f).apply {
                duration = 500L
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        provider.remove()
                    }
                })
                start()
            }
        }

        setContent {
            MaterialTheme {
                AppNavGraph()
            }
        }
    }
}

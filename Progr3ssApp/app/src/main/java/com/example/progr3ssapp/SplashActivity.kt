package com.example.progr3ssapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.progr3ssapp.databinding.ActivitySplashBinding
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_TIME_OUT = 2000L
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fade-in animáció
        binding.logo.alpha = 0f
        binding.appName.alpha = 0f
        binding.tagline.alpha = 0f
        binding.logo.animate().alpha(1f).setDuration(800).start()
        binding.appName.animate().alpha(1f).setDuration(800).setStartDelay(300).start()
        binding.tagline.animate().alpha(1f).setDuration(800).setStartDelay(600).start()

        handler.postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, SPLASH_TIME_OUT)

        Log.d(TAG, "onCreate: SplashActivity created.")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: SplashActivity started.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: SplashActivity resumed.")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: SplashActivity paused.")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: SplashActivity stopped.")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: SplashActivity restarted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "onDestroy: SplashActivity destroyed.")
    }
}
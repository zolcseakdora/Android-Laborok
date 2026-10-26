package com.example.progr3ssapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.progr3ssapp.databinding.ActivityMainBinding
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.text = intent.getStringExtra("message")
        Log.d(TAG, "onCreate: MainActivity created.")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: MainActivity started.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: MainActivity resumed.")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: MainActivity paused.")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: MainActivity stopped.")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: MainActivity restarted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: MainActivity destroyed.")
    }
}
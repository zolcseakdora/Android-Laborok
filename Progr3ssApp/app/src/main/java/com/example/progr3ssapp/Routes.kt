package com.example.progr3ssapp

import kotlinx.serialization.Serializable

// Típusbiztos navigációs útvonalak

// Bejelentkezés előtt
@Serializable data object Splash
@Serializable data object Login
@Serializable data object Register
@Serializable data object ResetPassword

// Bejelentkezés után (alsó navigációval)
@Serializable data object MainGraph
@Serializable data object Home
@Serializable data object Assistant
@Serializable data object Profile

// További képernyők
@Serializable data object AddHabit
@Serializable data object CreateSchedule
@Serializable data class ScheduleDetails(val id: Long)
@Serializable data class EditSchedule(val id: Long)
@Serializable data object EditProfile

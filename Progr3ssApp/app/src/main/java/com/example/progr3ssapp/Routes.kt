package com.example.progr3ssapp

import kotlinx.serialization.Serializable

// Type-safe routes from the navigation lab PDF.
@Serializable data object Login
@Serializable data object MainGraph
@Serializable data object Home
@Serializable data class Profile(
    val userId: String = "guest",
    val userName: String = "Guest"
)

@kotlinx.serialization.Serializable
data object Register


@kotlinx.serialization.Serializable
data object AddHabit

@kotlinx.serialization.Serializable
data object CreateSchedule

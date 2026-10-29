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

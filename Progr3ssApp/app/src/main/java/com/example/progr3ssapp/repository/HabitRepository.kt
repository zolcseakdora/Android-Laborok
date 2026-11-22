package com.example.progr3ssapp.repository

import com.example.progr3ssapp.network.ApiService
import com.example.progr3ssapp.network.RetrofitClient
import com.example.progr3ssapp.network.bodyOrThrow
import com.example.progr3ssapp.network.models.CreateHabitRequest
import com.example.progr3ssapp.network.models.HabitCategory
import com.example.progr3ssapp.network.models.HabitResponse

// ==========================================
// HABIT REPOSITORY
// ==========================================

class HabitRepository(
    private val api: ApiService = RetrofitClient.api
) {

    // GET /habit/categories
    suspend fun getCategories(): Result<List<HabitCategory>> =
        runCatching {
            api.getHabitCategories()
                .bodyOrThrow("Nem sikerült lekérni a kategóriákat")
        }

    // GET /habit
    suspend fun getHabits(): Result<List<HabitResponse>> =
        runCatching {
            api.getHabits()
                .bodyOrThrow("Nem sikerült lekérni a szokásokat")
        }

    // GET /habit/user/{userId}
    suspend fun getHabitsByUser(userId: Long): Result<List<HabitResponse>> =
        runCatching {
            api.getHabitsByUser(userId)
                .bodyOrThrow("Nem sikerült lekérni a szokásokat")
        }

    // POST /habit
    suspend fun createHabit(
        name: String,
        description: String,
        categoryId: Long,
        goal: String
    ): Result<HabitResponse> =
        runCatching {
            val request = CreateHabitRequest(
                name = name.trim(),
                description = description.trim().ifBlank { null },
                categoryId = categoryId,
                goal = goal.trim()
            )
            api.createHabit(request)
                .bodyOrThrow("Nem sikerült létrehozni a szokást")
        }
}

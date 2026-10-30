
package com.example.progr3ssapp.repository

import com.example.progr3ssapp.network.ApiService
import com.example.progr3ssapp.network.RetrofitClient
import com.example.progr3ssapp.network.bodyOrThrow
import com.example.progr3ssapp.network.models.CreateHabitRequest
import com.example.progr3ssapp.network.models.HabitCategory
import com.example.progr3ssapp.network.models.HabitResponse

// ==========================================
// 7. LABOR - HABIT REPOSITORY
// Szokások kezelése a backend segítségével
// ==========================================

class HabitRepository(
    private val api: ApiService = RetrofitClient.api
) {

    // 1. A hét szokáskategória lekérése
    // GET /habit/categories

    suspend fun getCategories(): Result<List<HabitCategory>> =
        runCatching {

            api.getHabitCategories()
                .bodyOrThrow(
                    "Nem sikerült lekérni a kategóriákat"
                )
        }


    // 2. A bejelentkezett felhasználó
    // összes szokásának lekérése
    // GET /habit

    suspend fun getHabits(): Result<List<HabitResponse>> =
        runCatching {

            api.getHabits()
                .bodyOrThrow(
                    "Nem sikerült lekérni a szokásokat"
                )
        }


    // 3. Új szokás létrehozása
    // POST /habit

    suspend fun createHabit(
        name: String,
        description: String,
        categoryId: Long,
        goal: String
    ): Result<HabitResponse> =
        runCatching {

            // Elkészítjük a backendnek küldött adatokat
            val request = CreateHabitRequest(
                name = name.trim(),

                description = description.trim()
                    .ifBlank { null },

                categoryId = categoryId,

                goal = goal.trim()
            )

            // Elküldjük a kérést a backendnek
            api.createHabit(request)
                .bodyOrThrow(
                    "Nem sikerült létrehozni a szokást"
                )
        }
}

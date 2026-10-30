
package com.example.progr3ssapp.repository

import com.example.progr3ssapp.network.ApiService
import com.example.progr3ssapp.network.RetrofitClient
import com.example.progr3ssapp.network.bodyOrThrow

import com.example.progr3ssapp.network.models.CreateCustomScheduleRequest
import com.example.progr3ssapp.network.models.CreateRecurringScheduleRequest
import com.example.progr3ssapp.network.models.ScheduleResponse

// ==========================================
// 7. LABOR - SCHEDULE REPOSITORY
// Szokások időbeosztásának kezelése
// ==========================================

class ScheduleRepository(
    private val api: ApiService = RetrofitClient.api
) {

    // 1. Egy adott nap ütemezéseinek lekérése
    // GET /schedule/day?date=YYYY-MM-DD

    suspend fun getScheduleByDay(
        day: String
    ): Result<List<ScheduleResponse>> =
        runCatching {

            api.getScheduleByDay(day)
                .bodyOrThrow(
                    "Nem sikerült lekérni a napi ütemezést"
                )
        }


    // 2. Ismétlődő ütemezés létrehozása
    // POST /schedule/recurring

    suspend fun createRecurringSchedule(
        habitId: Long,
        startTime: String,
        endTime: String?,
        durationMinutes: Int,
        repeatPattern: String,
        repeatDays: Int,
        notes: String?
    ): Result<List<ScheduleResponse>> =
        runCatching {

            // Az ütemezés adatainak összeállítása
            val request = CreateRecurringScheduleRequest(
                habitId = habitId,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                repeatPattern = repeatPattern,
                repeatDays = repeatDays,
                isCustom = true,
                participantIds = emptyList(),
                notes = notes
            )

            // Kérés elküldése a backendnek
            api.createRecurringSchedule(request)
                .bodyOrThrow(
                    "Nem sikerült létrehozni az ismétlődő ütemezést"
                )
        }


    // 3. Egyszeri ütemezés létrehozása
    // POST /schedule/custom

    suspend fun createCustomSchedule(
        habitId: Long,
        date: String,
        startTime: String,
        endTime: String?,
        durationMinutes: Int,
        notes: String?
    ): Result<ScheduleResponse> =
        runCatching {

            val request = CreateCustomScheduleRequest(
                habitId = habitId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                isCustom = true,
                participantIds = emptyList(),
                notes = notes
            )

            // Egyszeri ütemezés mentése
            api.createCustomSchedule(request)
                .bodyOrThrow(
                    "Nem sikerült létrehozni az egyszeri ütemezést"
                )
        }
}

package com.example.progr3ssapp.repository

import com.example.progr3ssapp.network.ApiService
import com.example.progr3ssapp.network.RetrofitClient
import com.example.progr3ssapp.network.bodyOrThrow
import com.example.progr3ssapp.network.ensureSuccess
import com.example.progr3ssapp.network.models.CreateCustomScheduleRequest
import com.example.progr3ssapp.network.models.CreateProgressRequest
import com.example.progr3ssapp.network.models.CreateRecurringScheduleRequest
import com.example.progr3ssapp.network.models.CreateWeekdayScheduleRequest
import com.example.progr3ssapp.network.models.ProgressResponseDto
import com.example.progr3ssapp.network.models.ScheduleResponse
import com.example.progr3ssapp.network.models.UpdateScheduleRequest

// ==========================================
// SCHEDULE + PROGRESS REPOSITORY
// ==========================================

class ScheduleRepository(
    private val api: ApiService = RetrofitClient.api
) {

    // GET /schedule/day?date=YYYY-MM-DD
    suspend fun getScheduleByDay(day: String): Result<List<ScheduleResponse>> =
        runCatching {
            api.getScheduleByDay(day)
                .bodyOrThrow("Nem sikerült lekérni a napi ütemezést")
        }

    // GET /schedule/{id}
    suspend fun getScheduleById(id: Long): Result<ScheduleResponse> =
        runCatching {
            api.getScheduleById(id)
                .bodyOrThrow("Nem sikerült lekérni az ütemezést")
        }

    // POST /schedule/custom – egyszeri
    suspend fun createCustomSchedule(
        request: CreateCustomScheduleRequest
    ): Result<ScheduleResponse> =
        runCatching {
            api.createCustomSchedule(request)
                .bodyOrThrow("Nem sikerült létrehozni az ütemezést")
        }

    // POST /schedule/recurring – daily / weekdays / weekends
    suspend fun createRecurringSchedule(
        request: CreateRecurringScheduleRequest
    ): Result<List<ScheduleResponse>> =
        runCatching {
            api.createRecurringSchedule(request)
                .bodyOrThrow("Nem sikerült létrehozni az ismétlődő ütemezést")
        }

    // POST /schedule/recurring/weekdays – kiválasztott napokra
    suspend fun createWeekdaySchedule(
        request: CreateWeekdayScheduleRequest
    ): Result<List<ScheduleResponse>> =
        runCatching {
            api.createWeekdaySchedule(request)
                .bodyOrThrow("Nem sikerült létrehozni az ütemezést")
        }

    // PATCH /schedule/{id}
    suspend fun updateSchedule(
        id: Long,
        request: UpdateScheduleRequest
    ): Result<ScheduleResponse> =
        runCatching {
            api.updateSchedule(id, request)
                .bodyOrThrow("Nem sikerült módosítani az ütemezést")
        }

    // DELETE /schedule/{id} – 204, üres törzs
    suspend fun deleteSchedule(id: Long): Result<Unit> =
        runCatching {
            api.deleteSchedule(id)
                .ensureSuccess("Nem sikerült törölni az ütemezést")
        }

    // POST /progress
    suspend fun createProgress(
        request: CreateProgressRequest
    ): Result<ProgressResponseDto> =
        runCatching {
            api.createProgress(request)
                .bodyOrThrow("Nem sikerült menteni a haladást")
        }
}

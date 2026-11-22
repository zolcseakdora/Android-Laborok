package com.example.progr3ssapp.network.models

import com.google.gson.annotations.SerializedName

// ==========================================
// HABIT
// ==========================================

data class HabitCategory(
    val id: Long,
    val name: String,
    val iconUrl: String? = null
)

data class HabitResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val category: HabitCategory? = null,
    val goal: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class CreateHabitRequest(
    val name: String,
    val description: String? = null,
    val categoryId: Long,
    val goal: String
)

// ==========================================
// SCHEDULE
// ==========================================

data class ParticipantDto(
    val id: Long,
    val name: String? = null,
    val email: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null
)

data class ProgressResponseDto(
    val id: Long,
    @SerializedName("scheduleId") val scheduleId: Long? = null,
    val date: String? = null,
    @SerializedName("logged_time") val loggedTime: Double? = null,
    val notes: String? = null,
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class ScheduleResponse(
    val id: Long,
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    val status: String? = null,
    val date: String? = null,
    @SerializedName("is_custom") val isCustom: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    val type: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    val notes: String? = null,
    val participants: List<ParticipantDto>? = null,
    val habit: HabitResponse? = null,
    val progress: List<ProgressResponseDto>? = null,
    @SerializedName("is_participant_only") val isParticipantOnly: Boolean = false
)

data class CreateRecurringScheduleRequest(
    val habitId: Long,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    val repeatPattern: String,
    val repeatDays: Int = 30,
    @SerializedName("is_custom") val isCustom: Boolean = true,
    val participantIds: List<Long> = emptyList(),
    val notes: String? = null
)

data class CreateCustomScheduleRequest(
    val habitId: Long,
    val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("is_custom") val isCustom: Boolean = true,
    val participantIds: List<Long> = emptyList(),
    val notes: String? = null
)

// POST /schedule/recurring/weekdays
// daysOfWeek: 1 = hétfő ... 7 = vasárnap
data class CreateWeekdayScheduleRequest(
    val habitId: Long,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    val daysOfWeek: List<Int>,
    val numberOfWeeks: Int = 4,
    val participantIds: List<Long> = emptyList(),
    val notes: String? = null
)

// ==========================================
// PROGRESS
// ==========================================

data class CreateProgressRequest(
    val scheduleId: Long,
    val date: String,
    @SerializedName("logged_time") val loggedTime: Int? = null,
    val notes: String? = null,
    @SerializedName("is_completed") val isCompleted: Boolean = false
)

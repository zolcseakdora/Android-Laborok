package com.example.progr3ssapp.network.models

import com.google.gson.annotations.SerializedName

// PATCH /schedule/{id}
// Csak a nem null mezőket küldi el a Gson (a null mezőket kihagyja).
data class UpdateScheduleRequest(
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    val status: String? = null,
    val date: String? = null,
    @SerializedName("is_custom") val isCustom: Boolean? = null,
    val participantIds: List<Long>? = null,
    val notes: String? = null
)

package com.example.progr3ssapp.utils

import com.example.progr3ssapp.network.models.ScheduleResponse
import kotlin.math.roundToInt

// ==========================================
// HALADÁS SZÁZALÉKBAN
// Példa: 10 perc cél, 8 perc rögzítve -> 80%
// ==========================================

object ProgressUtils {

    // A cél időtartam percben (duration_minutes, vagy kezdés-befejezés különbsége)
    fun goalMinutes(schedule: ScheduleResponse): Int =
        schedule.durationMinutes
            ?: DateTimeUtils.minutesBetween(schedule.startTime, schedule.endTime)
            ?: 0

    // Az eddig rögzített percek összege (az összes progress bejegyzésből)
    fun loggedMinutes(schedule: ScheduleResponse): Int =
        schedule.progress.orEmpty()
            .sumOf { it.loggedTime ?: 0.0 }
            .roundToInt()

    // 0.0 – 1.0 közötti arány
    fun fraction(logged: Int, goal: Int, completed: Boolean = false): Float = when {
        goal > 0 && logged > 0 -> (logged.toFloat() / goal).coerceIn(0f, 1f)
        completed -> 1f   // percek nélkül, de készre jelölve
        else -> 0f
    }

    fun fraction(schedule: ScheduleResponse): Float =
        fraction(loggedMinutes(schedule), goalMinutes(schedule), schedule.status == "Completed")

    // Százalék egész számként: 80
    fun percent(logged: Int, goal: Int, completed: Boolean = false): Int =
        (fraction(logged, goal, completed) * 100).roundToInt()

    fun percent(schedule: ScheduleResponse): Int = (fraction(schedule) * 100).roundToInt()
}

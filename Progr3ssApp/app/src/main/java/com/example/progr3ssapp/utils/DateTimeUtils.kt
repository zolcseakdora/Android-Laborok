package com.example.progr3ssapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ==========================================
// DÁTUM ÉS IDŐ SEGÉDFÜGGVÉNYEK
// A backend UTC ISO időpontokat használ,
// a felületen helyi időt mutatunk.
// ==========================================

object DateTimeUtils {

    private const val ISO_OUT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    // ISO időpont feldolgozása (UTC)
    fun parse(value: String?): Date? {
        if (value.isNullOrBlank()) return null

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )

        for (pattern in patterns) {
            try {
                val formatter = SimpleDateFormat(pattern, Locale.US)
                formatter.isLenient = false
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                return formatter.parse(value)
            } catch (_: Exception) {
                // következő formátum
            }
        }
        return null
    }

    // Date -> ISO UTC szöveg a backendnek
    fun toIso(date: Date): String =
        SimpleDateFormat(ISO_OUT, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(date)

    fun nowIso(): String = toIso(Date())

    // Mai dátum: 2026-10-29
    fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Fejléc: "Thursday, Nov 19"
    fun todayHeader(): String =
        SimpleDateFormat("EEEE, MMM d", Locale.ENGLISH).format(Date())

    // Helyi dátum (yyyy-MM-dd) egy ISO időpontból
    fun localDate(value: String?): String? {
        val date = parse(value) ?: return null
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    // Dátum megjelenítése: Nov 19, 2026
    fun formatDate(value: String?): String {
        val date = parse(value) ?: return value ?: "-"
        return SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(date)
    }

    // Idő megjelenítése helyi időben: 10:00
    fun formatTime(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        if (Regex("""\d{2}:\d{2}""").matches(value)) return value
        val date = parse(value) ?: return value
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    // "Nov 19, 10:00"
    fun formatDateTime(value: String?): String {
        val date = parse(value) ?: return value ?: "-"
        return SimpleDateFormat("MMM d, HH:mm", Locale.ENGLISH).format(date)
    }

    // A nap melyik szakaszába esik (helyi idő szerint)
    fun localHour(value: String?): Int {
        val date = parse(value) ?: return 0
        return Calendar.getInstance().apply { time = date }
            .get(Calendar.HOUR_OF_DAY)
    }

    // Helyi dátum + idő -> UTC ISO. Példa: "2026-10-29", "10:00"
    fun toUtcIso(date: String, time: String): String? {
        val input = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            isLenient = false
        }
        val parsed = try {
            input.parse("$date $time")
        } catch (_: Exception) {
            null
        } ?: return null
        return toIso(parsed)
    }

    // ISO időponthoz percek hozzáadása
    fun plusMinutes(iso: String, minutes: Int): String? {
        val date = parse(iso) ?: return null
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.MINUTE, minutes)
        }
        return toIso(cal.time)
    }

    // Két ISO időpont között eltelt percek
    fun minutesBetween(startIso: String?, endIso: String?): Int? {
        val s = parse(startIso) ?: return null
        val e = parse(endIso) ?: return null
        return ((e.time - s.time) / 60000L).toInt()
    }

    // Aktuális helyi idő "HH:mm"
    fun nowTime(): String =
        SimpleDateFormat("HH:mm", Locale.US).format(Date())

    // Perc formázása: 30m / 1h 15m
    fun formatMinutes(minutes: Int?): String {
        if (minutes == null || minutes <= 0) return "-"
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "${m}m"
            m == 0 -> "${h}h"
            else -> "${h}h ${m}m"
        }
    }
}

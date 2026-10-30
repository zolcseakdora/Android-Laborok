
package com.example.progr3ssapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ==========================================
// 7. LABOR - DATE TIME UTILS
// Dátumok és időpontok kezelése
// ==========================================

object DateTimeUtils {

    // A backend UTC időpontjainak feldolgozása
    private fun parseDateTime(value: String): Date? {

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX"
        )

        for (pattern in patterns) {
            try {
                val formatter = SimpleDateFormat(
                    pattern,
                    Locale.US
                )

                formatter.isLenient = false
                formatter.timeZone =
                    TimeZone.getTimeZone("UTC")

                return formatter.parse(value)

            } catch (_: Exception) {
                // Másik formátummal próbálkozunk
            }
        }

        return null
    }

    // Mai dátum: 2026-10-29
    fun today(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())
    }

    // Dátum megjelenítése: 2026. 10. 29.
    fun formatDate(value: String?): String {

        if (value.isNullOrBlank()) return "-"

        val date = parseDateTime(value)
            ?: return value

        return SimpleDateFormat(
            "yyyy. MM. dd.",
            Locale.getDefault()
        ).format(date)
    }

    // Idő megjelenítése: 10:00
    fun formatTime(value: String?): String {

        if (value.isNullOrBlank()) return "-"

        // Ha már HH:mm alakú, nem alakítjuk át
        if (Regex("""\d{2}:\d{2}""")
                .matches(value)) {
            return value
        }

        val date = parseDateTime(value)
            ?: return value

        return SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(date)
    }

    // Teljes dátum és idő a felhasználónak
    fun formatDateTime(value: String?): String {

        if (value.isNullOrBlank()) return "-"

        val date = parseDateTime(value)
            ?: return value

        return SimpleDateFormat(
            "yyyy. MM. dd. HH:mm",
            Locale.getDefault()
        ).format(date)
    }

    // Helyi dátum és idő átalakítása UTC formára
    // Példa: "2026-10-29", "10:00"
    fun toUtcIso(
        date: String,
        time: String
    ): String? {

        val input = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.US
        ).apply {
            isLenient = false
        }

        val parsed = try {
            input.parse("$date $time")
        } catch (_: Exception) {
            null
        } ?: return null

        val output = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        )

        output.timeZone = TimeZone.getTimeZone("UTC")

        return output.format(parsed)
    }
}

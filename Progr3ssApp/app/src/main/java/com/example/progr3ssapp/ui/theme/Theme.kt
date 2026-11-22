package com.example.progr3ssapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ==========================================
// PROGR3SS SÖTÉT LILA TÉMA (a dizájnterv alapján)
// ==========================================

object AppColors {
    val Background = Color(0xFF1A1830)
    val Surface = Color(0xFF25233F)
    val SurfaceHigh = Color(0xFF2F2C4D)
    val Field = Color(0xFF1F1D36)
    val Outline = Color(0xFF3E3A63)
    val Primary = Color(0xFF6C63FF)
    val PrimarySoft = Color(0xFF8F88FF)
    val Accent = Color(0xFF1EC8D6)
    val Success = Color(0xFF3DD68C)
    val Warning = Color(0xFFFFB547)
    val Error = Color(0xFFFF6B6B)
    val TextPrimary = Color(0xFFECEAF6)
    val TextSecondary = Color(0xFF9C98B8)
}

private val DarkColors = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.SurfaceHigh,
    onPrimaryContainer = AppColors.TextPrimary,
    secondary = AppColors.Accent,
    onSecondary = Color.White,
    tertiary = AppColors.Success,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceHigh,
    onSurfaceVariant = AppColors.TextSecondary,
    surfaceContainer = AppColors.Surface,
    surfaceContainerHigh = AppColors.SurfaceHigh,
    surfaceContainerHighest = AppColors.SurfaceHigh,
    surfaceContainerLow = AppColors.Surface,
    outline = AppColors.Outline,
    outlineVariant = AppColors.Outline,
    error = AppColors.Error,
    onError = Color.White
)

@Composable
fun Progr3ssAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}

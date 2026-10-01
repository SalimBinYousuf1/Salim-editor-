package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import com.example.data.repository.EditorFont
import com.example.data.repository.EditorSettings
import com.example.data.repository.EditorThemeMode

data class SalimCustomColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val secondarySurface: Color = surfaceVariant,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val divider: Color,
    val cardBorder: Color,
    val isDark: Boolean
)

val LightEditorColors = SalimCustomColors(
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F3F5),
    textPrimary = Color(0xFF1E293B),
    textSecondary = Color(0xFF64748B),
    textTertiary = Color(0xFF94A3B8),
    accent = Color(0xFF2563EB),
    divider = Color(0xFFE2E8F0),
    cardBorder = Color(0xFFE2E8F0),
    isDark = false
)

val DarkEditorColors = SalimCustomColors(
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textTertiary = Color(0xFF64748B),
    accent = Color(0xFF3B82F6),
    divider = Color(0xFF334155),
    cardBorder = Color(0xFF334155),
    isDark = true
)

val AmoledEditorColors = SalimCustomColors(
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    textPrimary = Color(0xFFE4E4E7),
    textSecondary = Color(0xFFA1A1AA),
    textTertiary = Color(0xFF71717A),
    accent = Color(0xFF3B82F6),
    divider = Color(0xFF27272A),
    cardBorder = Color(0xFF27272A),
    isDark = true
)

val SepiaEditorColors = SalimCustomColors(
    background = Color(0xFFFBF0D9),
    surface = Color(0xFFF4E8C1),
    surfaceVariant = Color(0xFFEADCB1),
    textPrimary = Color(0xFF5F4B32),
    textSecondary = Color(0xFF8C7355),
    textTertiary = Color(0xFFA89478),
    accent = Color(0xFFB45309),
    divider = Color(0xFFE5D5A8),
    cardBorder = Color(0xFFDECBA0),
    isDark = false
)

val LocalEditorColors = staticCompositionLocalOf { LightEditorColors }
val LocalEditorSettings = staticCompositionLocalOf { EditorSettings() }

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF2563EB)): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = when (cleanHex.length) {
            6 -> (0xFF000000 or cleanHex.toLong(16)).toInt()
            8 -> cleanHex.toLong(16).toInt()
            else -> defaultColor.toArgb()
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

fun getFontFamily(font: EditorFont): FontFamily {
    return when (font) {
        EditorFont.SYSTEM -> FontFamily.Default
        EditorFont.MONOSPACE -> FontFamily.Monospace
        EditorFont.SERIF -> FontFamily.Serif
        EditorFont.SANS_SERIF -> FontFamily.SansSerif
        EditorFont.CASUAL -> FontFamily.Cursive
        EditorFont.CURSIVE -> FontFamily.Cursive
    }
}

@Composable
fun SalimAppTheme(
    settings: EditorSettings = EditorSettings(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        EditorThemeMode.SYSTEM -> systemDark
        EditorThemeMode.LIGHT -> false
        EditorThemeMode.DARK, EditorThemeMode.AMOLED -> true
        EditorThemeMode.SEPIA -> false
    }

    val accent = parseHexColor(settings.accentColorHex, Color(0xFF2563EB))

    val baseColors = when (settings.themeMode) {
        EditorThemeMode.SYSTEM -> if (systemDark) DarkEditorColors else LightEditorColors
        EditorThemeMode.LIGHT -> LightEditorColors
        EditorThemeMode.DARK -> DarkEditorColors
        EditorThemeMode.AMOLED -> AmoledEditorColors
        EditorThemeMode.SEPIA -> SepiaEditorColors
    }

    val customColors = baseColors.copy(accent = accent)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = accent,
            background = customColors.background,
            surface = customColors.surface,
            onPrimary = Color.White,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = customColors.background,
            surface = customColors.surface,
            onPrimary = Color.White,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = customColors.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalEditorColors provides customColors,
        LocalEditorSettings provides settings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SalimTypography,
            content = content
        )
    }
}

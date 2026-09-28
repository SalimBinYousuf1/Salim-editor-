package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.repository.EditorFont
import com.example.data.repository.EditorSettings
import com.example.data.repository.EditorThemeMode

data class EditorColors(
    val background: Color,
    val surface: Color,
    val secondarySurface: Color,
    val editorCanvas: Color,
    val cardBorder: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accessoryBar: Color,
    val lineNumbers: Color,
    val selectionHighlight: Color,
    val isDark: Boolean
)

val LocalEditorColors = staticCompositionLocalOf {
    EditorColors(
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        secondarySurface = Color(0xFFF8FAFC),
        editorCanvas = Color(0xFFFFFFFF),
        cardBorder = Color(0xFFE2E8F0),
        divider = Color(0xFFE2E8F0),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF64748B),
        textTertiary = Color(0xFF94A3B8),
        accent = Color(0xFF2563EB),
        accessoryBar = Color(0xFFF1F5F9),
        lineNumbers = Color(0xFF94A3B8),
        selectionHighlight = Color(0x332563EB),
        isDark = false
    )
}

val LocalEditorSettings = staticCompositionLocalOf { EditorSettings() }

fun parseHexColor(hex: String, fallback: Color = Color(0xFF2563EB)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        fallback
    }
}

fun getFontFamily(font: EditorFont): FontFamily {
    return when (font) {
        EditorFont.SANS -> FontFamily.Default
        EditorFont.SERIF -> FontFamily.Serif
        EditorFont.MONOSPACE -> FontFamily.Monospace
        EditorFont.ROUNDED -> FontFamily.SansSerif
    }
}

@Composable
fun SalimAppTheme(
    settings: EditorSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()
    val accentColor = remember(settings.accentColorHex) {
        parseHexColor(settings.accentColorHex, Color(0xFF2563EB))
    }

    val isDark = when (settings.themeMode) {
        EditorThemeMode.DARK, EditorThemeMode.OLED -> true
        EditorThemeMode.LIGHT, EditorThemeMode.PAPER, EditorThemeMode.WARM -> false
        EditorThemeMode.COOL -> systemInDark
        EditorThemeMode.DYNAMIC -> systemInDark
        EditorThemeMode.SYSTEM -> systemInDark
    }

    val editorColors = remember(settings.themeMode, isDark, accentColor) {
        when (settings.themeMode) {
            EditorThemeMode.OLED -> EditorColors(
                background = Color(0xFF000000),
                surface = Color(0xFF101012),
                secondarySurface = Color(0xFF18181B),
                editorCanvas = Color(0xFF000000),
                cardBorder = Color(0xFF27272A),
                divider = Color(0xFF27272A),
                textPrimary = Color(0xFFFFFFFF),
                textSecondary = Color(0xFFA1A1AA),
                textTertiary = Color(0xFF71717A),
                accent = accentColor,
                accessoryBar = Color(0xFF101012),
                lineNumbers = Color(0xFF52525B),
                selectionHighlight = accentColor.copy(alpha = 0.35f),
                isDark = true
            )
            EditorThemeMode.PAPER -> EditorColors(
                background = Color(0xFFFBF7EE),
                surface = Color(0xFFFFFDF8),
                secondarySurface = Color(0xFFF3ECE0),
                editorCanvas = Color(0xFFFBF7EE),
                cardBorder = Color(0xFFE5DDCB),
                divider = Color(0xFFE5DDCB),
                textPrimary = Color(0xFF2D2823),
                textSecondary = Color(0xFF73675C),
                textTertiary = Color(0xFFA09488),
                accent = Color(0xFF8B4513).takeIf { accentColor == Color(0xFF2563EB) } ?: accentColor,
                accessoryBar = Color(0xFFF0E8DA),
                lineNumbers = Color(0xFFA89C8E),
                selectionHighlight = Color(0x33C29B38),
                isDark = false
            )
            EditorThemeMode.WARM -> EditorColors(
                background = Color(0xFFF7F3EE),
                surface = Color(0xFFFFFFFF),
                secondarySurface = Color(0xFFEDE6DC),
                editorCanvas = Color(0xFFF7F3EE),
                cardBorder = Color(0xFFDFD7CC),
                divider = Color(0xFFDFD7CC),
                textPrimary = Color(0xFF332B25),
                textSecondary = Color(0xFF7A6E64),
                textTertiary = Color(0xFFA89C92),
                accent = accentColor,
                accessoryBar = Color(0xFFECE4D9),
                lineNumbers = Color(0xFFA89C92),
                selectionHighlight = accentColor.copy(alpha = 0.25f),
                isDark = false
            )
            EditorThemeMode.COOL -> {
                if (isDark) {
                    EditorColors(
                        background = Color(0xFF0B132B),
                        surface = Color(0xFF1C2541),
                        secondarySurface = Color(0xFF1C2541),
                        editorCanvas = Color(0xFF0B132B),
                        cardBorder = Color(0xFF3A506B),
                        divider = Color(0xFF3A506B),
                        textPrimary = Color(0xFFF0F4F8),
                        textSecondary = Color(0xFF94A3B8),
                        textTertiary = Color(0xFF64748B),
                        accent = Color(0xFF38BDF8),
                        accessoryBar = Color(0xFF1C2541),
                        lineNumbers = Color(0xFF475569),
                        selectionHighlight = Color(0x3338BDF8),
                        isDark = true
                    )
                } else {
                    EditorColors(
                        background = Color(0xFFF0F4F8),
                        surface = Color(0xFFFFFFFF),
                        secondarySurface = Color(0xFFE2E8F0),
                        editorCanvas = Color(0xFFF0F4F8),
                        cardBorder = Color(0xFFCBD5E1),
                        divider = Color(0xFFCBD5E1),
                        textPrimary = Color(0xFF0F172A),
                        textSecondary = Color(0xFF475569),
                        textTertiary = Color(0xFF64748B),
                        accent = Color(0xFF0284C7),
                        accessoryBar = Color(0xFFE2E8F0),
                        lineNumbers = Color(0xFF94A3B8),
                        selectionHighlight = Color(0x330284C7),
                        isDark = false
                    )
                }
            }
            else -> {
                if (isDark) {
                    EditorColors(
                        background = Color(0xFF0F172A),
                        surface = Color(0xFF1E293B),
                        secondarySurface = Color(0xFF1E293B),
                        editorCanvas = Color(0xFF0F172A),
                        cardBorder = Color(0xFF334155),
                        divider = Color(0xFF334155),
                        textPrimary = Color(0xFFF8FAFC),
                        textSecondary = Color(0xFF94A3B8),
                        textTertiary = Color(0xFF64748B),
                        accent = accentColor,
                        accessoryBar = Color(0xFF1E293B),
                        lineNumbers = Color(0xFF475569),
                        selectionHighlight = accentColor.copy(alpha = 0.35f),
                        isDark = true
                    )
                } else {
                    EditorColors(
                        background = Color(0xFFFFFFFF),
                        surface = Color(0xFFFFFFFF),
                        secondarySurface = Color(0xFFF8FAFC),
                        editorCanvas = Color(0xFFFFFFFF),
                        cardBorder = Color(0xFFE2E8F0),
                        divider = Color(0xFFE2E8F0),
                        textPrimary = Color(0xFF0F172A),
                        textSecondary = Color(0xFF64748B),
                        textTertiary = Color(0xFF94A3B8),
                        accent = accentColor,
                        accessoryBar = Color(0xFFF1F5F9),
                        lineNumbers = Color(0xFF94A3B8),
                        selectionHighlight = accentColor.copy(alpha = 0.20f),
                        isDark = false
                    )
                }
            }
        }
    }

    val colorScheme: ColorScheme = if (settings.themeMode == EditorThemeMode.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (isDark) {
        darkColorScheme(
            primary = editorColors.accent,
            onPrimary = Color.White,
            secondary = editorColors.textSecondary,
            background = editorColors.background,
            onBackground = editorColors.textPrimary,
            surface = editorColors.surface,
            onSurface = editorColors.textPrimary,
            surfaceVariant = editorColors.secondarySurface,
            onSurfaceVariant = editorColors.textSecondary,
            outline = editorColors.cardBorder
        )
    } else {
        lightColorScheme(
            primary = editorColors.accent,
            onPrimary = Color.White,
            secondary = editorColors.textSecondary,
            background = editorColors.background,
            onBackground = editorColors.textPrimary,
            surface = editorColors.surface,
            onSurface = editorColors.textPrimary,
            surfaceVariant = editorColors.secondarySurface,
            onSurfaceVariant = editorColors.textSecondary,
            outline = editorColors.cardBorder
        )
    }

    val selectedFontFamily = getFontFamily(settings.font)

    val typography = remember(selectedFontFamily) {
        Typography(
            headlineLarge = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.5).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 30.sp
            ),
            titleLarge = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                lineHeight = 24.sp
            ),
            titleMedium = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            labelLarge = TextStyle(
                fontFamily = selectedFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        )
    }

    CompositionLocalProvider(
        LocalEditorColors provides editorColors,
        LocalEditorSettings provides settings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

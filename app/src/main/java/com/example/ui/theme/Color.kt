package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Action & Semantic Colors (Constant across themes)
val SalimBlue = Color(0xFF007AFF)
val SalimRed = Color(0xFFFF3B30)
val SalimOrange = Color(0xFFFF9500)
val SalimGreen = Color(0xFF34C759)
val SalimPurple = Color(0xFFAF52DE)
val SalimYellow = Color(0xFFFFCC00)
val SalimAudioGray = Color(0xFF8E8E93)
val SalimFolderColor = Color(0xFFFFCC00)
val SalimSelectionBlue = Color(0xFF007AFF)

// Base static color definitions
val SalimWhite = Color(0xFFFFFFFF)
val SalimBackground = Color(0xFFFFFFFF)
val SalimSurface = Color(0xFFFFFFFF)
val SalimSecondarySurface = Color(0xFFF7F8FA)
val SalimSearchField = Color(0xFFF2F4F7)
val SalimDivider = Color(0xFFE8E8ED)
val SalimCardBorder = Color(0xFFEAEAEA)
val SalimTextPrimary = Color(0xFF111111)
val SalimTextSecondary = Color(0xFF6E6E73)
val SalimTextTertiary = Color(0xFFA1A1A6)
val SalimSelectionBackground = Color(0x14007AFF)

object SalimThemeColors {
    val isDark: Boolean = false
    val background = SalimBackground
    val surface = SalimSurface
    val textPrimary = SalimTextPrimary
    val textSecondary = SalimTextSecondary
    val accent = SalimBlue
    val divider = SalimDivider
}


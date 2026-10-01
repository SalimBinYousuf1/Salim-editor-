package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class EditorThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED,
    SEPIA
}

enum class EditorFont(val displayName: String) {
    SYSTEM("System Default"),
    MONOSPACE("Monospace (JetBrains)"),
    SERIF("Serif (Merriweather)"),
    SANS_SERIF("Sans-Serif (Inter)"),
    CASUAL("Casual"),
    CURSIVE("Cursive")
}

enum class EditorMargin(val horizontalDp: Int, val displayName: String) {
    COMPACT(8, "Compact (8dp)"),
    NORMAL(16, "Normal (16dp)"),
    RELAXED(24, "Relaxed (24dp)")
}

enum class NavigationPosition(val displayName: String) {
    BOTTOM("Bottom"),
    TOP("Top"),
    HIDDEN("Hidden")
}

enum class EditorPreset(val displayName: String) {
    DEFAULT("Default"),
    WRITER("Writer"),
    CODER("Coder"),
    MINIMAL("Minimal")
}

data class EditorSettings(
    val themeMode: EditorThemeMode = EditorThemeMode.SYSTEM,
    val accentColorHex: String = "#2563EB",
    val font: EditorFont = EditorFont.SYSTEM,
    val fontSize: Float = 16f,
    val fontSizeSp: Float = 16f,
    val lineHeightMultiplier: Float = 1.4f,
    val margin: EditorMargin = EditorMargin.NORMAL,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = true,
    val navigationPosition: NavigationPosition = NavigationPosition.BOTTOM,
    val quickAccessoryBar: Boolean = true,
    val gesturePinchZoom: Boolean = true,
    val gestureTwoFingerUndo: Boolean = true,
    val hapticFeedback: Boolean = true,
    val activePreset: EditorPreset = EditorPreset.DEFAULT
)

class EditorPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("salim_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<EditorSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): EditorSettings {
        val themeStr = prefs.getString("themeMode", EditorThemeMode.SYSTEM.name) ?: EditorThemeMode.SYSTEM.name
        val accentStr = prefs.getString("accentColorHex", "#2563EB") ?: "#2563EB"
        val fontStr = prefs.getString("font", EditorFont.SYSTEM.name) ?: EditorFont.SYSTEM.name
        val fontSize = prefs.getFloat("fontSizeSp", 16f)
        val lineHeight = prefs.getFloat("lineHeightMultiplier", 1.4f)
        val marginStr = prefs.getString("margin", EditorMargin.NORMAL.name) ?: EditorMargin.NORMAL.name
        val lineNumbers = prefs.getBoolean("showLineNumbers", true)
        val wordWrap = prefs.getBoolean("wordWrap", true)
        val navPosStr = prefs.getString("navigationPosition", NavigationPosition.BOTTOM.name) ?: NavigationPosition.BOTTOM.name
        val quickBar = prefs.getBoolean("quickAccessoryBar", true)
        val pinchZoom = prefs.getBoolean("gesturePinchZoom", true)
        val twoFingerUndo = prefs.getBoolean("gestureTwoFingerUndo", true)
        val haptic = prefs.getBoolean("hapticFeedback", true)

        return EditorSettings(
            themeMode = runCatching { EditorThemeMode.valueOf(themeStr) }.getOrDefault(EditorThemeMode.SYSTEM),
            accentColorHex = accentStr,
            font = runCatching { EditorFont.valueOf(fontStr) }.getOrDefault(EditorFont.SYSTEM),
            fontSizeSp = fontSize,
            lineHeightMultiplier = lineHeight,
            margin = runCatching { EditorMargin.valueOf(marginStr) }.getOrDefault(EditorMargin.NORMAL),
            showLineNumbers = lineNumbers,
            wordWrap = wordWrap,
            navigationPosition = runCatching { NavigationPosition.valueOf(navPosStr) }.getOrDefault(NavigationPosition.BOTTOM),
            quickAccessoryBar = quickBar,
            gesturePinchZoom = pinchZoom,
            gestureTwoFingerUndo = twoFingerUndo,
            hapticFeedback = haptic
        )
    }

    private fun saveAndEmit(settings: EditorSettings) {
        prefs.edit()
            .putString("themeMode", settings.themeMode.name)
            .putString("accentColorHex", settings.accentColorHex)
            .putString("font", settings.font.name)
            .putFloat("fontSizeSp", settings.fontSizeSp)
            .putFloat("lineHeightMultiplier", settings.lineHeightMultiplier)
            .putString("margin", settings.margin.name)
            .putBoolean("showLineNumbers", settings.showLineNumbers)
            .putBoolean("wordWrap", settings.wordWrap)
            .putString("navigationPosition", settings.navigationPosition.name)
            .putBoolean("quickAccessoryBar", settings.quickAccessoryBar)
            .putBoolean("gesturePinchZoom", settings.gesturePinchZoom)
            .putBoolean("gestureTwoFingerUndo", settings.gestureTwoFingerUndo)
            .putBoolean("hapticFeedback", settings.hapticFeedback)
            .apply()
        _settingsFlow.value = settings
    }

    fun setThemeMode(mode: EditorThemeMode) {
        saveAndEmit(_settingsFlow.value.copy(themeMode = mode))
    }

    fun setAccentColor(hex: String) {
        saveAndEmit(_settingsFlow.value.copy(accentColorHex = hex))
    }

    fun setFont(font: EditorFont) {
        saveAndEmit(_settingsFlow.value.copy(font = font))
    }

    fun setFontSize(size: Float) {
        saveAndEmit(_settingsFlow.value.copy(fontSizeSp = size))
    }

    fun setLineHeight(multiplier: Float) {
        saveAndEmit(_settingsFlow.value.copy(lineHeightMultiplier = multiplier))
    }

    fun setMargin(margin: EditorMargin) {
        saveAndEmit(_settingsFlow.value.copy(margin = margin))
    }

    fun setShowLineNumbers(enabled: Boolean) {
        saveAndEmit(_settingsFlow.value.copy(showLineNumbers = enabled))
    }

    fun setWordWrap(enabled: Boolean) {
        saveAndEmit(_settingsFlow.value.copy(wordWrap = enabled))
    }

    fun setNavigationPosition(pos: NavigationPosition) {
        saveAndEmit(_settingsFlow.value.copy(navigationPosition = pos))
    }

    fun setQuickAccessoryBar(enabled: Boolean) {
        saveAndEmit(_settingsFlow.value.copy(quickAccessoryBar = enabled))
    }

    fun setGesturePinchZoom(enabled: Boolean) {
        saveAndEmit(_settingsFlow.value.copy(gesturePinchZoom = enabled))
    }

    fun setGestureTwoFingerUndo(enabled: Boolean) {
        saveAndEmit(_settingsFlow.value.copy(gestureTwoFingerUndo = enabled))
    }

    fun setHapticFeedback(enabled: Boolean) {
        saveAndEmit(_settingsFlow.value.copy(hapticFeedback = enabled))
    }

    fun applyPreset(preset: EditorPreset) {
        val updated = when (preset) {
            EditorPreset.DEFAULT -> EditorSettings()
            EditorPreset.WRITER -> _settingsFlow.value.copy(
                font = EditorFont.SERIF,
                fontSizeSp = 18f,
                lineHeightMultiplier = 1.6f,
                margin = EditorMargin.RELAXED,
                showLineNumbers = false,
                wordWrap = true
            )
            EditorPreset.CODER -> _settingsFlow.value.copy(
                font = EditorFont.MONOSPACE,
                fontSizeSp = 14f,
                lineHeightMultiplier = 1.3f,
                margin = EditorMargin.COMPACT,
                showLineNumbers = true,
                wordWrap = false
            )
            EditorPreset.MINIMAL -> _settingsFlow.value.copy(
                fontSizeSp = 16f,
                lineHeightMultiplier = 1.5f,
                margin = EditorMargin.NORMAL,
                showLineNumbers = false,
                wordWrap = true,
                quickAccessoryBar = false
            )
        }
        saveAndEmit(updated)
    }
}

package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.editorDataStore: DataStore<Preferences> by preferencesDataStore(name = "salim_editor_prefs")

enum class EditorThemeMode {
    SYSTEM, LIGHT, DARK, OLED, PAPER, WARM, COOL, DYNAMIC
}

enum class EditorFont(val displayName: String) {
    SANS("System Sans"),
    SERIF("Elegant Serif"),
    MONOSPACE("Monospace Code"),
    ROUNDED("Soft Rounded")
}

enum class EditorMargin(val displayName: String) {
    COMPACT("Edge to Edge"),
    COMFORTABLE("Comfortable (16dp)"),
    FOCUS_READING("Reading Focus (Max Width)")
}

enum class NavigationPosition(val displayName: String) {
    BOTTOM("Bottom Thumb Zone"),
    TOP("Top Navigation")
}

enum class EditorPreset(val displayName: String) {
    MINIMAL("Minimal"),
    COMFORTABLE("Comfortable"),
    COMPACT("Compact"),
    WRITING("Writing Focus"),
    DEVELOPER("Developer Mode")
}

data class EditorSettings(
    val themeMode: EditorThemeMode = EditorThemeMode.SYSTEM,
    val accentColorHex: String = "#2563EB",
    val font: EditorFont = EditorFont.SANS,
    val fontSize: Float = 16f,
    val lineHeightMultiplier: Float = 1.5f,
    val margin: EditorMargin = EditorMargin.COMFORTABLE,
    val showLineNumbers: Boolean = false,
    val wordWrap: Boolean = true,
    val autoIndent: Boolean = true,
    val autoCloseBrackets: Boolean = true,
    val cursorBlinking: Boolean = true,
    val cursorThickness: Float = 2.0f,
    val navigationPosition: NavigationPosition = NavigationPosition.BOTTOM,
    val quickAccessoryBar: Boolean = true,
    val gesturePinchZoom: Boolean = true,
    val gestureTwoFingerUndo: Boolean = true,
    val gestureEdgeSwipe: Boolean = true,
    val hapticFeedback: Boolean = true,
    val activePreset: EditorPreset = EditorPreset.COMFORTABLE
)

class EditorPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val FONT = stringPreferencesKey("font")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LINE_HEIGHT = floatPreferencesKey("line_height")
        val MARGIN = stringPreferencesKey("margin")
        val SHOW_LINE_NUMBERS = booleanPreferencesKey("show_line_numbers")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val AUTO_INDENT = booleanPreferencesKey("auto_indent")
        val AUTO_CLOSE_BRACKETS = booleanPreferencesKey("auto_close_brackets")
        val CURSOR_BLINKING = booleanPreferencesKey("cursor_blinking")
        val CURSOR_THICKNESS = floatPreferencesKey("cursor_thickness")
        val NAVIGATION_POSITION = stringPreferencesKey("navigation_position")
        val QUICK_ACCESSORY_BAR = booleanPreferencesKey("quick_accessory_bar")
        val GESTURE_PINCH_ZOOM = booleanPreferencesKey("gesture_pinch_zoom")
        val GESTURE_TWO_FINGER_UNDO = booleanPreferencesKey("gesture_two_finger_undo")
        val GESTURE_EDGE_SWIPE = booleanPreferencesKey("gesture_edge_swipe")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val ACTIVE_PRESET = stringPreferencesKey("active_preset")
    }

    val settingsFlow: Flow<EditorSettings> = context.editorDataStore.data.map { prefs ->
        EditorSettings(
            themeMode = runCatching {
                EditorThemeMode.valueOf(prefs[PreferencesKeys.THEME_MODE] ?: EditorThemeMode.SYSTEM.name)
            }.getOrDefault(EditorThemeMode.SYSTEM),
            accentColorHex = prefs[PreferencesKeys.ACCENT_COLOR] ?: "#2563EB",
            font = runCatching {
                EditorFont.valueOf(prefs[PreferencesKeys.FONT] ?: EditorFont.SANS.name)
            }.getOrDefault(EditorFont.SANS),
            fontSize = prefs[PreferencesKeys.FONT_SIZE] ?: 16f,
            lineHeightMultiplier = prefs[PreferencesKeys.LINE_HEIGHT] ?: 1.5f,
            margin = runCatching {
                EditorMargin.valueOf(prefs[PreferencesKeys.MARGIN] ?: EditorMargin.COMFORTABLE.name)
            }.getOrDefault(EditorMargin.COMFORTABLE),
            showLineNumbers = prefs[PreferencesKeys.SHOW_LINE_NUMBERS] ?: false,
            wordWrap = prefs[PreferencesKeys.WORD_WRAP] ?: true,
            autoIndent = prefs[PreferencesKeys.AUTO_INDENT] ?: true,
            autoCloseBrackets = prefs[PreferencesKeys.AUTO_CLOSE_BRACKETS] ?: true,
            cursorBlinking = prefs[PreferencesKeys.CURSOR_BLINKING] ?: true,
            cursorThickness = prefs[PreferencesKeys.CURSOR_THICKNESS] ?: 2.0f,
            navigationPosition = runCatching {
                NavigationPosition.valueOf(prefs[PreferencesKeys.NAVIGATION_POSITION] ?: NavigationPosition.BOTTOM.name)
            }.getOrDefault(NavigationPosition.BOTTOM),
            quickAccessoryBar = prefs[PreferencesKeys.QUICK_ACCESSORY_BAR] ?: true,
            gesturePinchZoom = prefs[PreferencesKeys.GESTURE_PINCH_ZOOM] ?: true,
            gestureTwoFingerUndo = prefs[PreferencesKeys.GESTURE_TWO_FINGER_UNDO] ?: true,
            gestureEdgeSwipe = prefs[PreferencesKeys.GESTURE_EDGE_SWIPE] ?: true,
            hapticFeedback = prefs[PreferencesKeys.HAPTIC_FEEDBACK] ?: true,
            activePreset = runCatching {
                EditorPreset.valueOf(prefs[PreferencesKeys.ACTIVE_PRESET] ?: EditorPreset.COMFORTABLE.name)
            }.getOrDefault(EditorPreset.COMFORTABLE)
        )
    }

    suspend fun setThemeMode(mode: EditorThemeMode) {
        context.editorDataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColor(hex: String) {
        context.editorDataStore.edit { it[PreferencesKeys.ACCENT_COLOR] = hex }
    }

    suspend fun setFont(font: EditorFont) {
        context.editorDataStore.edit { it[PreferencesKeys.FONT] = font.name }
    }

    suspend fun setFontSize(size: Float) {
        val clamped = size.coerceIn(12f, 32f)
        context.editorDataStore.edit { it[PreferencesKeys.FONT_SIZE] = clamped }
    }

    suspend fun setLineHeight(multiplier: Float) {
        val clamped = multiplier.coerceIn(1.1f, 2.4f)
        context.editorDataStore.edit { it[PreferencesKeys.LINE_HEIGHT] = clamped }
    }

    suspend fun setMargin(margin: EditorMargin) {
        context.editorDataStore.edit { it[PreferencesKeys.MARGIN] = margin.name }
    }

    suspend fun setShowLineNumbers(enabled: Boolean) {
        context.editorDataStore.edit { it[PreferencesKeys.SHOW_LINE_NUMBERS] = enabled }
    }

    suspend fun setWordWrap(enabled: Boolean) {
        context.editorDataStore.edit { it[PreferencesKeys.WORD_WRAP] = enabled }
    }

    suspend fun setNavigationPosition(pos: NavigationPosition) {
        context.editorDataStore.edit { it[PreferencesKeys.NAVIGATION_POSITION] = pos.name }
    }

    suspend fun setQuickAccessoryBar(enabled: Boolean) {
        context.editorDataStore.edit { it[PreferencesKeys.QUICK_ACCESSORY_BAR] = enabled }
    }

    suspend fun setGesturePinchZoom(enabled: Boolean) {
        context.editorDataStore.edit { it[PreferencesKeys.GESTURE_PINCH_ZOOM] = enabled }
    }

    suspend fun setGestureTwoFingerUndo(enabled: Boolean) {
        context.editorDataStore.edit { it[PreferencesKeys.GESTURE_TWO_FINGER_UNDO] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.editorDataStore.edit { it[PreferencesKeys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun applyPreset(preset: EditorPreset) {
        context.editorDataStore.edit { prefs ->
            prefs[PreferencesKeys.ACTIVE_PRESET] = preset.name
            when (preset) {
                EditorPreset.MINIMAL -> {
                    prefs[PreferencesKeys.FONT] = EditorFont.SANS.name
                    prefs[PreferencesKeys.FONT_SIZE] = 16f
                    prefs[PreferencesKeys.LINE_HEIGHT] = 1.4f
                    prefs[PreferencesKeys.MARGIN] = EditorMargin.COMPACT.name
                    prefs[PreferencesKeys.SHOW_LINE_NUMBERS] = false
                    prefs[PreferencesKeys.QUICK_ACCESSORY_BAR] = true
                }
                EditorPreset.COMFORTABLE -> {
                    prefs[PreferencesKeys.FONT] = EditorFont.SANS.name
                    prefs[PreferencesKeys.FONT_SIZE] = 17f
                    prefs[PreferencesKeys.LINE_HEIGHT] = 1.55f
                    prefs[PreferencesKeys.MARGIN] = EditorMargin.COMFORTABLE.name
                    prefs[PreferencesKeys.SHOW_LINE_NUMBERS] = false
                    prefs[PreferencesKeys.QUICK_ACCESSORY_BAR] = true
                }
                EditorPreset.COMPACT -> {
                    prefs[PreferencesKeys.FONT] = EditorFont.SANS.name
                    prefs[PreferencesKeys.FONT_SIZE] = 14f
                    prefs[PreferencesKeys.LINE_HEIGHT] = 1.3f
                    prefs[PreferencesKeys.MARGIN] = EditorMargin.COMPACT.name
                    prefs[PreferencesKeys.SHOW_LINE_NUMBERS] = false
                    prefs[PreferencesKeys.QUICK_ACCESSORY_BAR] = true
                }
                EditorPreset.WRITING -> {
                    prefs[PreferencesKeys.FONT] = EditorFont.SERIF.name
                    prefs[PreferencesKeys.FONT_SIZE] = 18f
                    prefs[PreferencesKeys.LINE_HEIGHT] = 1.75f
                    prefs[PreferencesKeys.MARGIN] = EditorMargin.FOCUS_READING.name
                    prefs[PreferencesKeys.THEME_MODE] = EditorThemeMode.PAPER.name
                    prefs[PreferencesKeys.SHOW_LINE_NUMBERS] = false
                    prefs[PreferencesKeys.QUICK_ACCESSORY_BAR] = true
                }
                EditorPreset.DEVELOPER -> {
                    prefs[PreferencesKeys.FONT] = EditorFont.MONOSPACE.name
                    prefs[PreferencesKeys.FONT_SIZE] = 14.5f
                    prefs[PreferencesKeys.LINE_HEIGHT] = 1.35f
                    prefs[PreferencesKeys.MARGIN] = EditorMargin.COMPACT.name
                    prefs[PreferencesKeys.SHOW_LINE_NUMBERS] = true
                    prefs[PreferencesKeys.THEME_MODE] = EditorThemeMode.DARK.name
                    prefs[PreferencesKeys.QUICK_ACCESSORY_BAR] = true
                }
            }
        }
    }
}

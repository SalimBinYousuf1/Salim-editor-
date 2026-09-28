package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.repository.EditorFont
import com.example.data.repository.EditorMargin
import com.example.data.repository.EditorPreferencesRepository
import com.example.data.repository.EditorPreset
import com.example.data.repository.EditorSettings
import com.example.data.repository.EditorThemeMode
import com.example.data.repository.NavigationPosition
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo: EditorPreferencesRepository = (application as SalimApplication).editorPreferencesRepository

    val settingsState: StateFlow<EditorSettings> = prefsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EditorSettings()
    )

    fun setThemeMode(mode: EditorThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch { prefsRepo.setAccentColor(hex) }
    }

    fun setFont(font: EditorFont) {
        viewModelScope.launch { prefsRepo.setFont(font) }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch { prefsRepo.setFontSize(size) }
    }

    fun setLineHeight(multiplier: Float) {
        viewModelScope.launch { prefsRepo.setLineHeight(multiplier) }
    }

    fun setMargin(margin: EditorMargin) {
        viewModelScope.launch { prefsRepo.setMargin(margin) }
    }

    fun setShowLineNumbers(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setShowLineNumbers(enabled) }
    }

    fun setWordWrap(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setWordWrap(enabled) }
    }

    fun setNavigationPosition(pos: NavigationPosition) {
        viewModelScope.launch { prefsRepo.setNavigationPosition(pos) }
    }

    fun setQuickAccessoryBar(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setQuickAccessoryBar(enabled) }
    }

    fun setGesturePinchZoom(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setGesturePinchZoom(enabled) }
    }

    fun setGestureTwoFingerUndo(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setGestureTwoFingerUndo(enabled) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setHapticFeedback(enabled) }
    }

    fun applyPreset(preset: EditorPreset) {
        viewModelScope.launch { prefsRepo.applyPreset(preset) }
    }
}

package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.EditorFont
import com.example.data.repository.EditorMargin
import com.example.data.repository.EditorPreset
import com.example.data.repository.EditorThemeMode
import com.example.data.repository.NavigationPosition
import com.example.ui.theme.LocalEditorColors
import com.example.ui.theme.getFontFamily
import com.example.ui.theme.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()
    val colors = LocalEditorColors.current

    val accentPalette = listOf(
        "#2563EB" to "Royal Blue",
        "#10B981" to "Emerald",
        "#8B5CF6" to "Purple",
        "#F43F5E" to "Rose",
        "#F59E0B" to "Amber",
        "#F97316" to "Orange",
        "#06B6D4" to "Cyan",
        "#475569" to "Slate"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Personalization & Settings",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Presets (vivo personalization)
            item {
                SettingsSectionHeader(icon = Icons.Default.Tune, title = "Editor Presets")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorPreset.values().forEach { preset ->
                        val isSelected = settings.activePreset == preset
                        Surface(
                            onClick = { viewModel.applyPreset(preset) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.accent else colors.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.accent else colors.cardBorder),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = preset.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Appearance & Theme
            item {
                SettingsSectionHeader(icon = Icons.Default.Palette, title = "Appearance & Theme")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Theme", fontSize = 13.sp, color = colors.textSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EditorThemeMode.values().forEach { mode ->
                                val sel = settings.themeMode == mode
                                Surface(
                                    onClick = { viewModel.setThemeMode(mode) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sel) colors.accent.copy(alpha = 0.15f) else colors.secondarySurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) colors.accent else colors.cardBorder),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                            fontSize = 12.sp,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sel) colors.accent else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = colors.divider, thickness = 1.dp)

                        Text("Accent Color", fontSize = 13.sp, color = colors.textSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            accentPalette.forEach { (hex, _) ->
                                val color = parseHexColor(hex)
                                val isSelected = settings.accentColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) colors.textPrimary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setAccentColor(hex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Typography
            item {
                SettingsSectionHeader(icon = Icons.Default.FontDownload, title = "Typography")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Font Family", fontSize = 13.sp, color = colors.textSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EditorFont.values().forEach { font ->
                                val isSelected = settings.font == font
                                Surface(
                                    onClick = { viewModel.setFont(font) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) colors.accent.copy(alpha = 0.15f) else colors.secondarySurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.accent else colors.cardBorder),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = font.displayName,
                                            fontFamily = getFontFamily(font),
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) colors.accent else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Live Typography Sample Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.secondarySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "The quick brown fox jumps over the lazy dog. 1234567890",
                                fontFamily = getFontFamily(settings.font),
                                fontSize = settings.fontSize.sp,
                                lineHeight = (settings.fontSize * settings.lineHeightMultiplier).sp,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        // Font size slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Font Size", fontSize = 13.sp, color = colors.textSecondary)
                                Text("${settings.fontSize.toInt()} sp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                            }
                            Slider(
                                value = settings.fontSize,
                                onValueChange = { viewModel.setFontSize(it) },
                                valueRange = 12f..28f,
                                steps = 15,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.accent,
                                    activeTrackColor = colors.accent
                                )
                            )
                        }

                        // Line height slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Line Height Multiplier", fontSize = 13.sp, color = colors.textSecondary)
                                Text("${String.format("%.2f", settings.lineHeightMultiplier)}x", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                            }
                            Slider(
                                value = settings.lineHeightMultiplier,
                                onValueChange = { viewModel.setLineHeight(it) },
                                valueRange = 1.2f..2.2f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.accent,
                                    activeTrackColor = colors.accent
                                )
                            )
                        }
                    }
                }
            }

            // 4. Editor Canvas
            item {
                SettingsSectionHeader(icon = Icons.AutoMirrored.Filled.ViewQuilt, title = "Editor Canvas")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Editor Margins", fontSize = 13.sp, color = colors.textSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EditorMargin.values().forEach { margin ->
                                val sel = settings.margin == margin
                                Surface(
                                    onClick = { viewModel.setMargin(margin) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sel) colors.accent.copy(alpha = 0.15f) else colors.secondarySurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) colors.accent else colors.cardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = margin.displayName.substringBefore(" ("),
                                            fontSize = 11.sp,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sel) colors.accent else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = colors.divider, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        SettingsSwitchRow(
                            title = "Show Line Numbers",
                            subtitle = "Display margin numbers beside code and text lines",
                            checked = settings.showLineNumbers,
                            onCheckedChange = { viewModel.setShowLineNumbers(it) }
                        )

                        SettingsSwitchRow(
                            title = "Word Wrap",
                            subtitle = "Soft-wrap long lines to fit editor screen width",
                            checked = settings.wordWrap,
                            onCheckedChange = { viewModel.setWordWrap(it) }
                        )
                    }
                }
            }

            // 5. One-Handed & Keyboard
            item {
                SettingsSectionHeader(icon = Icons.Default.Keyboard, title = "One-Handed & Input")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsSwitchRow(
                            title = "Quick Input Accessory Strip",
                            subtitle = "Show symbols bar with Tab and cursor navigation above keyboard",
                            checked = settings.quickAccessoryBar,
                            onCheckedChange = { viewModel.setQuickAccessoryBar(it) }
                        )

                        SettingsSwitchRow(
                            title = "Haptic Feedback",
                            subtitle = "Subtle vibration on accessory keys and formatting taps",
                            checked = settings.hapticFeedback,
                            onCheckedChange = { viewModel.setHapticFeedback(it) }
                        )
                    }
                }
            }

            // 6. Gestures
            item {
                SettingsSectionHeader(icon = Icons.Default.Gesture, title = "Gestures")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsSwitchRow(
                            title = "Pinch to Zoom Font Size",
                            subtitle = "Pinch with two fingers inside the editor to dynamically scale text",
                            checked = settings.gesturePinchZoom,
                            onCheckedChange = { viewModel.setGesturePinchZoom(it) }
                        )

                        SettingsSwitchRow(
                            title = "Two-Finger Swipe Undo / Redo",
                            subtitle = "Quick gesture to step backward or forward in edit history",
                            checked = settings.gestureTwoFingerUndo,
                            onCheckedChange = { viewModel.setGestureTwoFingerUndo(it) }
                        )
                    }
                }
            }

            // 7. About
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.secondarySurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Salim Text Editor",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Version 1.0 · Apple simplicity · vivo reach · Samsung power",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(icon: ImageVector, title: String) {
    val colors = LocalEditorColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalEditorColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            Text(subtitle, fontSize = 11.sp, color = colors.textSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.accent
            )
        )
    }
}

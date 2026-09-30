package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimRed

@Composable
fun GoToLineDialog(
    totalLines: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    theme: EditorColorScheme
) {
    var lineInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = theme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Navigation,
                        contentDescription = null,
                        tint = SalimBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Go to Line",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.text
                    )
                }

                Text(
                    text = "Enter a line number between 1 and $totalLines:",
                    fontSize = 13.sp,
                    color = theme.gutterText
                )

                OutlinedTextField(
                    value = lineInput,
                    onValueChange = {
                        lineInput = it.filter { char -> char.isDigit() }
                        isError = false
                    },
                    singleLine = true,
                    isError = isError,
                    placeholder = { Text("Line number…", color = theme.gutterText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (isError) {
                    Text(
                        text = "Please enter a valid line between 1 and $totalLines",
                        color = SalimRed,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = theme.gutterText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val line = lineInput.toIntOrNull()
                            if (line != null && line in 1..totalLines) {
                                onConfirm(line)
                            } else {
                                isError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SalimBlue)
                    ) {
                        Text("Jump", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditorSettingsDialog(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    wordWrap: Boolean,
    onWordWrapChange: (Boolean) -> Unit,
    showLineNumbers: Boolean,
    onShowLineNumbersChange: (Boolean) -> Unit,
    tabSpaces: Int,
    onTabSpacesChange: (Int) -> Unit,
    theme: EditorColorScheme,
    onDismiss: () -> Unit
) {
    var currentSize by remember { mutableFloatStateOf(fontSize) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = theme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = SalimBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Editor Preferences",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.text
                        )
                    }
                }

                HorizontalDivider(color = theme.gutterDivider)

                // Font Size Slider
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Font Size",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.text
                        )
                        Text(
                            text = "${currentSize.toInt()} sp",
                            fontSize = 13.sp,
                            color = SalimBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = currentSize,
                        onValueChange = {
                            currentSize = it
                            onFontSizeChange(it)
                        },
                        valueRange = 10f..22f,
                        steps = 11
                    )
                }

                // Word Wrap Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Word Wrap",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.text
                        )
                        Text(
                            text = "Wrap long code lines without horizontal scroll",
                            fontSize = 11.sp,
                            color = theme.gutterText
                        )
                    }
                    Switch(
                        checked = wordWrap,
                        onCheckedChange = onWordWrapChange
                    )
                }

                // Show Line Numbers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Line Numbers Gutter",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.text
                        )
                        Text(
                            text = "Show numbered side gutter with active line",
                            fontSize = 11.sp,
                            color = theme.gutterText
                        )
                    }
                    Switch(
                        checked = showLineNumbers,
                        onCheckedChange = onShowLineNumbersChange
                    )
                }

                // Tab Width
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tab Indent Size",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.text
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 4).forEach { size ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (tabSpaces == size) SalimBlue else theme.gutterBackground)
                                    .clickable { onTabSpacesChange(size) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$size spaces",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (tabSpaces == size) Color.White else theme.text
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SalimBlue)
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelectorDialog(
    currentTheme: EditorColorScheme,
    onSelectTheme: (EditorColorScheme) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = currentTheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = SalimBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Editor Theme",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.text
                    )
                }

                HorizontalDivider(color = currentTheme.gutterDivider)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditorThemes.allThemes.forEach { thm ->
                        val isSelected = thm.id == currentTheme.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) currentTheme.activeLineBg else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SalimBlue else currentTheme.gutterDivider,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSelectTheme(thm)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Color preview circle
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(thm.background)
                                        .border(1.dp, thm.gutterDivider, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = thm.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = currentTheme.text
                                    )
                                    Text(
                                        text = if (thm.isDark) "Dark mode" else "Light mode",
                                        fontSize = 11.sp,
                                        color = currentTheme.gutterText
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = SalimBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = currentTheme.gutterText)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelectorDialog(
    currentLanguage: EditorLanguage,
    onSelectLanguage: (EditorLanguage) -> Unit,
    theme: EditorColorScheme,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = theme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Syntax Highlighting Mode",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.text
                )

                HorizontalDivider(color = theme.gutterDivider)

                LazyColumn(
                    modifier = Modifier.height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(EditorLanguage.entries.toList()) { lang ->
                        val isSelected = lang == currentLanguage
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) theme.activeLineBg else Color.Transparent)
                                .clickable {
                                    onSelectLanguage(lang)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.displayName,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SalimBlue else theme.text
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = "Active",
                                    tint = SalimBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = theme.gutterText)
                    }
                }
            }
        }
    }
}

@Composable
fun UnsavedChangesDialog(
    fileName: String,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit,
    theme: EditorColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Unsaved Changes",
                fontWeight = FontWeight.Bold,
                color = theme.text
            )
        },
        text = {
            Text(
                text = "Do you want to save the changes made to \"$fileName\" before closing?",
                color = theme.gutterText,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = SalimBlue)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = theme.gutterText)
                }
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(onClick = onDiscard) {
                    Text("Discard", color = SalimRed, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = theme.surface
    )
}

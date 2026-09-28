package com.example.ui.editor.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardTab
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalEditorColors

@Composable
fun KeyboardAccessoryBar(
    onInsertText: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onMoveCursor: (Int) -> Unit, // -1: left, +1: right
    onMoveLineStart: () -> Unit,
    onMoveLineEnd: () -> Unit,
    canUndo: Boolean = true,
    canRedo: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalEditorColors.current
    val view = LocalView.current
    val scrollState = rememberScrollState()

    fun haptic() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        color = colors.accessoryBar,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tab key
            AccessoryIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardTab,
                contentDescription = "Tab",
                onClick = {
                    haptic()
                    onInsertText("  ")
                }
            )

            // Undo & Redo
            AccessoryIconButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                enabled = canUndo,
                onClick = {
                    haptic()
                    onUndo()
                }
            )

            AccessoryIconButton(
                icon = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                enabled = canRedo,
                onClick = {
                    haptic()
                    onRedo()
                }
            )

            // Divider spacer
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(colors.divider)
            )

            // Cursor movement keys
            AccessoryIconButton(
                icon = Icons.Default.FirstPage,
                contentDescription = "Start of Line",
                onClick = {
                    haptic()
                    onMoveLineStart()
                }
            )

            AccessoryIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Move Left",
                onClick = {
                    haptic()
                    onMoveCursor(-1)
                }
            )

            AccessoryIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Move Right",
                onClick = {
                    haptic()
                    onMoveCursor(1)
                }
            )

            AccessoryIconButton(
                icon = Icons.AutoMirrored.Filled.LastPage,
                contentDescription = "End of Line",
                onClick = {
                    haptic()
                    onMoveLineEnd()
                }
            )

            // Divider spacer
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(colors.divider)
            )

            // Essential symbols
            val symbols = listOf(
                "#", "*", "_", "-", "`", "[", "]", "(", ")",
                "{", "}", "\"", "'", "/", "=", ">", "<", ":", ";", "$", "@", "|", "~"
            )

            symbols.forEach { sym ->
                AccessoryTextButton(
                    symbol = sym,
                    onClick = {
                        haptic()
                        onInsertText(sym)
                    }
                )
            }
        }
    }
}

@Composable
fun AccessoryIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val colors = LocalEditorColors.current
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = if (enabled) 0.85f else 0.35f),
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) colors.textPrimary else colors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AccessoryTextButton(
    symbol: String,
    onClick: () -> Unit
) {
    val colors = LocalEditorColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = colors.surface,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

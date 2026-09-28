package com.example.ui.editor.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalEditorColors

enum class ToolbarMode {
    NORMAL,
    SELECTION,
    MARKDOWN,
    CODE
}

@Composable
fun DynamicEditorToolbar(
    mode: ToolbarMode,
    onModeChange: (ToolbarMode) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFindClick: () -> Unit,
    onOutlineClick: () -> Unit,
    onMoreClick: () -> Unit,
    // Selection Actions
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onShareSelection: () -> Unit,
    // Formatting & Markdown Actions
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onHeading: (Int) -> Unit,
    onBulletList: () -> Unit,
    onNumberedList: () -> Unit,
    onChecklist: () -> Unit,
    onQuote: () -> Unit,
    onCodeBlock: () -> Unit,
    onLink: () -> Unit,
    // Code Actions
    onIndent: () -> Unit,
    onDedent: () -> Unit,
    onComment: () -> Unit,
    onInsertBrackets: (String, String) -> Unit,
    canUndo: Boolean = true,
    canRedo: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalEditorColors.current
    val view = LocalView.current

    fun haptic() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        color = colors.surface.copy(alpha = 0.95f),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedContent(
                targetState = mode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ToolbarModeTransition"
            ) { currentMode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (currentMode) {
                        ToolbarMode.NORMAL -> {
                            ToolbarButton(
                                icon = Icons.AutoMirrored.Filled.Undo,
                                title = "Undo",
                                enabled = canUndo,
                                onClick = { haptic(); onUndo() }
                            )
                            ToolbarButton(
                                icon = Icons.AutoMirrored.Filled.Redo,
                                title = "Redo",
                                enabled = canRedo,
                                onClick = { haptic(); onRedo() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.Search,
                                title = "Find",
                                onClick = { haptic(); onFindClick() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.ViewHeadline,
                                title = "Outline",
                                onClick = { haptic(); onOutlineClick() }
                            )

                            // Quick mode switchers
                            ModeSwitchPill(
                                label = "MD",
                                active = false,
                                onClick = { haptic(); onModeChange(ToolbarMode.MARKDOWN) }
                            )
                            ModeSwitchPill(
                                label = "Code",
                                active = false,
                                onClick = { haptic(); onModeChange(ToolbarMode.CODE) }
                            )

                            ToolbarButton(
                                icon = Icons.Default.MoreHoriz,
                                title = "More",
                                onClick = { haptic(); onMoreClick() }
                            )
                        }

                        ToolbarMode.SELECTION -> {
                            ToolbarButton(
                                icon = Icons.Default.ContentCut,
                                title = "Cut",
                                onClick = { haptic(); onCut() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.ContentCopy,
                                title = "Copy",
                                onClick = { haptic(); onCopy() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.ContentPaste,
                                title = "Paste",
                                onClick = { haptic(); onPaste() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.FormatBold,
                                title = "Bold",
                                onClick = { haptic(); onBold() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.FormatItalic,
                                title = "Italic",
                                onClick = { haptic(); onItalic() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.Code,
                                title = "Code",
                                onClick = { haptic(); onCodeBlock() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.Share,
                                title = "Share",
                                onClick = { haptic(); onShareSelection() }
                            )
                            ModeSwitchPill(
                                label = "Done",
                                active = true,
                                onClick = { haptic(); onModeChange(ToolbarMode.NORMAL) }
                            )
                        }

                        ToolbarMode.MARKDOWN -> {
                            ModeSwitchPill(
                                label = "Text",
                                active = false,
                                onClick = { haptic(); onModeChange(ToolbarMode.NORMAL) }
                            )
                            TextActionPill(
                                label = "H1",
                                onClick = { haptic(); onHeading(1) }
                            )
                            TextActionPill(
                                label = "H2",
                                onClick = { haptic(); onHeading(2) }
                            )
                            ToolbarButton(
                                icon = Icons.Default.FormatBold,
                                title = "Bold",
                                onClick = { haptic(); onBold() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.FormatItalic,
                                title = "Italic",
                                onClick = { haptic(); onItalic() }
                            )
                            ToolbarButton(
                                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                                title = "Bullet",
                                onClick = { haptic(); onBulletList() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.FormatListNumbered,
                                title = "Numbered",
                                onClick = { haptic(); onNumberedList() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.Checklist,
                                title = "Checklist",
                                onClick = { haptic(); onChecklist() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.FormatQuote,
                                title = "Quote",
                                onClick = { haptic(); onQuote() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.Code,
                                title = "Code",
                                onClick = { haptic(); onCodeBlock() }
                            )
                            ToolbarButton(
                                icon = Icons.Default.Link,
                                title = "Link",
                                onClick = { haptic(); onLink() }
                            )
                        }

                        ToolbarMode.CODE -> {
                            ModeSwitchPill(
                                label = "Text",
                                active = false,
                                onClick = { haptic(); onModeChange(ToolbarMode.NORMAL) }
                            )
                            ToolbarButton(
                                icon = Icons.AutoMirrored.Filled.FormatIndentIncrease,
                                title = "Indent",
                                onClick = { haptic(); onIndent() }
                            )
                            ToolbarButton(
                                icon = Icons.AutoMirrored.Filled.FormatIndentDecrease,
                                title = "Dedent",
                                onClick = { haptic(); onDedent() }
                            )
                            TextActionPill(
                                label = "//",
                                onClick = { haptic(); onComment() }
                            )
                            TextActionPill(
                                label = "{ }",
                                onClick = { haptic(); onInsertBrackets("{", "}") }
                            )
                            TextActionPill(
                                label = "[ ]",
                                onClick = { haptic(); onInsertBrackets("[", "]") }
                            )
                            TextActionPill(
                                label = "( )",
                                onClick = { haptic(); onInsertBrackets("(", ")") }
                            )
                            ToolbarButton(
                                icon = Icons.Default.Search,
                                title = "Find",
                                onClick = { haptic(); onFindClick() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val colors = LocalEditorColors.current
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = colors.secondarySurface.copy(alpha = if (enabled) 0.6f else 0.2f),
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (enabled) colors.textPrimary else colors.textTertiary,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
fun ModeSwitchPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalEditorColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (active) colors.accent else colors.secondarySurface,
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (active) androidx.compose.ui.graphics.Color.White else colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TextActionPill(
    label: String,
    onClick: () -> Unit
) {
    val colors = LocalEditorColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = colors.secondarySurface.copy(alpha = 0.8f),
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

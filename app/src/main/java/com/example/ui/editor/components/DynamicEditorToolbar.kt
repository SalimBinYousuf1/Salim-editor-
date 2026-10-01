package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.WrapText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EditorColors

enum class ToolbarContextMode {
    WRITING,
    MARKDOWN,
    CODE
}

@Composable
fun DynamicEditorToolbar(
    mode: ToolbarContextMode,
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSearchClick: () -> Unit,
    wordWrap: Boolean,
    onToggleWordWrap: () -> Unit,
    colors: EditorColors,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    fun wrapSelectionOrInsert(prefix: String, suffix: String) {
        val sel = textFieldValue.selection
        val text = textFieldValue.text
        if (!sel.collapsed) {
            val selected = text.substring(sel.min, sel.max)
            val wrapped = "$prefix$selected$suffix"
            val newText = text.replaceRange(sel.min, sel.max, wrapped)
            onValueChange(textFieldValue.copy(text = newText, selection = TextRange(sel.min + wrapped.length)))
        } else {
            val insert = "$prefix$suffix"
            val newText = text.replaceRange(sel.start, sel.start, insert)
            onValueChange(textFieldValue.copy(text = newText, selection = TextRange(sel.start + prefix.length)))
        }
    }

    fun prependLine(prefix: String) {
        val sel = textFieldValue.selection
        val text = textFieldValue.text
        val lineStart = text.lastIndexOf('\n', (sel.start - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        onValueChange(textFieldValue.copy(text = newText, selection = TextRange(sel.start + prefix.length)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Undo / Redo
            IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) colors.textPrimary else colors.textTertiary,
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) colors.textPrimary else colors.textTertiary,
                    modifier = Modifier.size(19.dp)
                )
            }

            DividerPill(colors = colors)

            // Context-specific actions
            when (mode) {
                ToolbarContextMode.WRITING, ToolbarContextMode.MARKDOWN -> {
                    ToolbarButton(
                        icon = Icons.Outlined.Title,
                        contentDescription = "Heading",
                        colors = colors,
                        onClick = { prependLine("## ") }
                    )
                    ToolbarButton(
                        icon = Icons.Outlined.FormatBold,
                        contentDescription = "Bold",
                        colors = colors,
                        onClick = { wrapSelectionOrInsert("**", "**") }
                    )
                    ToolbarButton(
                        icon = Icons.Outlined.FormatItalic,
                        contentDescription = "Italic",
                        colors = colors,
                        onClick = { wrapSelectionOrInsert("*", "*") }
                    )
                    ToolbarButton(
                        icon = Icons.Outlined.FormatStrikethrough,
                        contentDescription = "Strikethrough",
                        colors = colors,
                        onClick = { wrapSelectionOrInsert("~~", "~~") }
                    )
                    ToolbarButton(
                        icon = Icons.Outlined.FormatQuote,
                        contentDescription = "Blockquote",
                        colors = colors,
                        onClick = { prependLine("> ") }
                    )
                    ToolbarButton(
                        icon = Icons.Outlined.FormatListBulleted,
                        contentDescription = "List",
                        colors = colors,
                        onClick = { prependLine("- ") }
                    )
                    ToolbarButton(
                        icon = Icons.Outlined.Checklist,
                        contentDescription = "Checklist",
                        colors = colors,
                        onClick = { prependLine("- [ ] ") }
                    )
                    ToolbarButton(
                        icon = Icons.Outlined.Code,
                        contentDescription = "Inline Code",
                        colors = colors,
                        onClick = { wrapSelectionOrInsert("`", "`") }
                    )
                }
                ToolbarContextMode.CODE -> {
                    TextToolbarButton(
                        text = "Tab",
                        colors = colors,
                        onClick = {
                            val sel = textFieldValue.selection
                            val newText = textFieldValue.text.replaceRange(sel.start, sel.end, "    ")
                            onValueChange(textFieldValue.copy(text = newText, selection = TextRange(sel.start + 4)))
                        }
                    )
                    TextToolbarButton(
                        text = "//",
                        colors = colors,
                        onClick = { prependLine("// ") }
                    )
                    TextToolbarButton(
                        text = "{ }",
                        colors = colors,
                        onClick = { wrapSelectionOrInsert("{\n    ", "\n}") }
                    )
                    TextToolbarButton(
                        text = "( )",
                        colors = colors,
                        onClick = { wrapSelectionOrInsert("(", ")") }
                    )
                    TextToolbarButton(
                        text = "[ ]",
                        colors = colors,
                        onClick = { wrapSelectionOrInsert("[", "]") }
                    )
                    TextToolbarButton(
                        text = "\" \"",
                        colors = colors,
                        onClick = { wrapSelectionOrInsert("\"", "\"") }
                    )
                }
            }

            DividerPill(colors = colors)

            // Word wrap toggle
            IconButton(onClick = onToggleWordWrap, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Outlined.WrapText,
                    contentDescription = "Toggle Word Wrap",
                    tint = if (wordWrap) colors.accent else colors.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Search
            IconButton(onClick = onSearchClick, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    colors: EditorColors,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(34.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun TextToolbarButton(
    text: String,
    colors: EditorColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = colors.textPrimary,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
private fun DividerPill(colors: EditorColors) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(colors.divider)
    )
}

package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorQuickBar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    theme: EditorColorScheme,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val quickTokens = listOf(
        Pair("TAB", "    "),
        Pair("{ }", "{"),
        Pair("( )", "("),
        Pair("[ ]", "["),
        Pair("< >", "<"),
        Pair("\"", "\""),
        Pair("'", "'"),
        Pair("`", "`"),
        Pair("=", "="),
        Pair(":", ":"),
        Pair(";", ";"),
        Pair(".", "."),
        Pair(",", ","),
        Pair("/", "/"),
        Pair("\\", "\\"),
        Pair("_", "_"),
        Pair("-", "-"),
        Pair("+", "+"),
        Pair("*", "*"),
        Pair("!", "!"),
        Pair("?", "?"),
        Pair("&", "&"),
        Pair("|", "|"),
        Pair("#", "#"),
        Pair("@", "@"),
        Pair("$", "$"),
        Pair("->", "->"),
        Pair("=>", "=>"),
        Pair("//", "// ")
    )

    fun handleInsert(label: String, insertText: String) {
        val sel = textFieldValue.selection
        val text = textFieldValue.text

        // Check if smart wrapping can be applied to selection
        if (!sel.collapsed) {
            val selectedText = text.substring(sel.min, sel.max)
            val wrapped = when (label) {
                "\"" -> "\"$selectedText\""
                "'" -> "'$selectedText'"
                "`" -> "`$selectedText`"
                "( )" -> "($selectedText)"
                "{ }" -> "{$selectedText}"
                "[ ]" -> "[$selectedText]"
                "< >" -> "<$selectedText>"
                else -> null
            }
            if (wrapped != null) {
                val newText = text.replaceRange(sel.min, sel.max, wrapped)
                onValueChange(
                    textFieldValue.copy(
                        text = newText,
                        selection = TextRange(sel.min + wrapped.length)
                    )
                )
                return
            }
        }

        // Auto-close pairs if nothing selected
        val (finalInsert, cursorOffset) = when (label) {
            "{ }" -> Pair("{}", 1)
            "( )" -> Pair("()", 1)
            "[ ]" -> Pair("[]", 1)
            "< >" -> Pair("<>", 1)
            "\"" -> Pair("\"\"", 1)
            "'" -> Pair("''", 1)
            "`" -> Pair("``", 1)
            else -> Pair(insertText, insertText.length)
        }

        val start = sel.min
        val newText = text.replaceRange(start, sel.max, finalInsert)
        onValueChange(
            textFieldValue.copy(
                text = newText,
                selection = TextRange(start + cursorOffset)
            )
        )
    }

    fun moveCursor(delta: Int) {
        val current = textFieldValue.selection.start
        val target = (current + delta).coerceIn(0, textFieldValue.text.length)
        onValueChange(textFieldValue.copy(selection = TextRange(target)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface)
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Precision Cursor Left
            QuickBarButton(
                text = "◀",
                theme = theme,
                onClick = { moveCursor(-1) }
            )

            // Precision Cursor Right
            QuickBarButton(
                text = "▶",
                theme = theme,
                onClick = { moveCursor(1) }
            )

            // Divider pill
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(theme.gutterDivider)
            )

            // Standard quick code tokens
            quickTokens.forEach { (label, insert) ->
                QuickBarButton(
                    text = label,
                    theme = theme,
                    onClick = { handleInsert(label, insert) }
                )
            }
        }
    }
}

@Composable
private fun QuickBarButton(
    text: String,
    theme: EditorColorScheme,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(theme.gutterBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = theme.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

package com.example.ui.editor

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SalimBlue

@Composable
fun MarkdownPreview(
    markdownText: String,
    theme: EditorColorScheme,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val lines = markdownText.lines()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var inCodeBlock = false
        val codeBlockLines = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // Close code block
                    val codeContent = codeBlockLines.joinToString("\n")
                    CodeBlock(code = codeContent, theme = theme)
                    codeBlockLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBlockLines.add(line)
                continue
            }

            // Headings
            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("# ").trim(), theme),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.text,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                    HorizontalDivider(color = theme.gutterDivider, thickness = 1.dp)
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("## ").trim(), theme),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.text,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("### ").trim(), theme),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.text,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                trimmed.startsWith("#### ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("#### ").trim(), theme),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.text,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                trimmed == "---" || trimmed == "***" -> {
                    HorizontalDivider(color = theme.gutterDivider, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                }
                trimmed.startsWith("> ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(theme.cursor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.removePrefix("> ").trim(), theme),
                            color = theme.text.copy(alpha = 0.85f),
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp
                        )
                    }
                }
                trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ") -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "Unchecked",
                            tint = theme.gutterText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.substring(6), theme),
                            color = theme.text,
                            fontSize = 14.sp
                        )
                    }
                }
                trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") || trimmed.startsWith("* [x] ") -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = "Checked",
                            tint = SalimBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.substring(6), theme),
                            color = theme.gutterText,
                            fontSize = 14.sp
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
                    Row(modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp)) {
                        Text("• ", color = theme.cursor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = parseInlineMarkdown(trimmed.substring(2), theme),
                            color = theme.text,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val dotIdx = trimmed.indexOf('.')
                    val num = trimmed.substring(0, dotIdx + 1)
                    val rest = trimmed.substring(dotIdx + 1).trim()
                    Row(modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp)) {
                        Text("$num ", color = theme.cursor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            text = parseInlineMarkdown(rest, theme),
                            color = theme.text,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = parseInlineMarkdown(line, theme),
                        color = theme.text,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            CodeBlock(code = codeBlockLines.joinToString("\n"), theme = theme)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun CodeBlock(code: String, theme: EditorColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.surface)
            .padding(12.dp)
    ) {
        Text(
            text = code,
            color = theme.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

private fun parseInlineMarkdown(text: String, theme: EditorColorScheme) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        // Inline code `...`
        if (text[i] == '`') {
            val nextBacktick = text.indexOf('`', i + 1)
            if (nextBacktick != -1) {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = theme.activeLineBg,
                        color = theme.type,
                        fontSize = 13.sp
                    )
                )
                append(" " + text.substring(i + 1, nextBacktick) + " ")
                pop()
                i = nextBacktick + 1
                continue
            }
        }

        // Bold **...**
        if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
            val nextBold = text.indexOf("**", i + 2)
            if (nextBold != -1) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = theme.text))
                append(text.substring(i + 2, nextBold))
                pop()
                i = nextBold + 2
                continue
            }
        }

        // Italic *...*
        if (text[i] == '*') {
            val nextItalic = text.indexOf('*', i + 1)
            if (nextItalic != -1) {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = theme.text))
                append(text.substring(i + 1, nextItalic))
                pop()
                i = nextItalic + 1
                continue
            }
        }

        append(text[i])
        i++
    }
}

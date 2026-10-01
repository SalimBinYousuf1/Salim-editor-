package com.example.ui.editor.components

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
import com.example.ui.theme.EditorColors

@Composable
fun MarkdownPreviewView(
    markdownText: String,
    colors: EditorColors,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val lines = markdownText.lines()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var inCodeBlock = false
        val codeBlockLines = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    val codeContent = codeBlockLines.joinToString("\n")
                    PreviewCodeBlock(code = codeContent, colors = colors)
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

            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = parseInline(trimmed.removePrefix("# ").trim(), colors),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                    HorizontalDivider(color = colors.divider, thickness = 1.dp)
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = parseInline(trimmed.removePrefix("## ").trim(), colors),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = parseInline(trimmed.removePrefix("### ").trim(), colors),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                trimmed.startsWith("#### ") -> {
                    Text(
                        text = parseInline(trimmed.removePrefix("#### ").trim(), colors),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                trimmed == "---" || trimmed == "***" -> {
                    HorizontalDivider(color = colors.divider, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
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
                                .background(colors.accent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInline(trimmed.removePrefix("> ").trim(), colors),
                            color = colors.textSecondary,
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
                            tint = colors.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInline(trimmed.substring(6), colors),
                            color = colors.textPrimary,
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
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInline(trimmed.substring(6), colors),
                            color = colors.textTertiary,
                            fontSize = 14.sp
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
                    Row(modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp)) {
                        Text("• ", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = parseInline(trimmed.substring(2), colors),
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                    }
                }
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val dotIdx = trimmed.indexOf('.')
                    val num = trimmed.substring(0, dotIdx + 1)
                    val rest = trimmed.substring(dotIdx + 1).trim()
                    Row(modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp)) {
                        Text("$num ", color = colors.accent, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            text = parseInline(rest, colors),
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                    }
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = parseInline(line, colors),
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            PreviewCodeBlock(code = codeBlockLines.joinToString("\n"), colors = colors)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun PreviewCodeBlock(code: String, colors: EditorColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            text = code,
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

private fun parseInline(text: String, colors: EditorColors) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        if (text[i] == '`') {
            val nextBacktick = text.indexOf('`', i + 1)
            if (nextBacktick != -1) {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = colors.surfaceVariant,
                        color = colors.accent,
                        fontSize = 13.sp
                    )
                )
                append(" " + text.substring(i + 1, nextBacktick) + " ")
                pop()
                i = nextBacktick + 1
                continue
            }
        }

        if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
            val nextBold = text.indexOf("**", i + 2)
            if (nextBold != -1) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.textPrimary))
                append(text.substring(i + 2, nextBold))
                pop()
                i = nextBold + 2
                continue
            }
        }

        if (text[i] == '*') {
            val nextItalic = text.indexOf('*', i + 1)
            if (nextItalic != -1) {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = colors.textPrimary))
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

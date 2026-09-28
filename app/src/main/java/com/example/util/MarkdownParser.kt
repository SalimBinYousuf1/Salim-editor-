package com.example.util

data class OutlineItem(
    val title: String,
    val level: Int,
    val lineIndex: Int
)

sealed class MarkdownNode {
    data class Heading(val level: Int, val text: String) : MarkdownNode()
    data class Paragraph(val text: String) : MarkdownNode()
    data class BulletItem(val text: String) : MarkdownNode()
    data class NumberedItem(val number: String, val text: String) : MarkdownNode()
    data class ChecklistItem(val isChecked: Boolean, val text: String) : MarkdownNode()
    data class Blockquote(val text: String) : MarkdownNode()
    data class CodeBlock(val code: String, val language: String = "") : MarkdownNode()
    object Divider : MarkdownNode()
}

object MarkdownParser {

    fun extractOutline(markdown: String): List<OutlineItem> {
        val outline = mutableListOf<OutlineItem>()
        val lines = markdown.lines()
        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#")) {
                var level = 0
                while (level < trimmed.length && trimmed[level] == '#') {
                    level++
                }
                if (level in 1..6 && trimmed.length > level && trimmed[level] == ' ') {
                    val title = trimmed.substring(level).trim()
                    outline.add(OutlineItem(title = title, level = level, lineIndex = index))
                }
            }
        }
        return outline
    }

    fun parseNodes(markdown: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val lines = markdown.lines()
        var inCodeBlock = false
        var codeBlockLang = ""
        val codeBuffer = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    nodes.add(MarkdownNode.CodeBlock(codeBuffer.toString().trimEnd(), codeBlockLang))
                    codeBuffer.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                    codeBlockLang = trimmed.removePrefix("```").trim()
                }
                continue
            }

            if (inCodeBlock) {
                codeBuffer.append(line).append("\n")
                continue
            }

            when {
                trimmed.startsWith("---") || trimmed.startsWith("***") || trimmed.startsWith("___") -> {
                    nodes.add(MarkdownNode.Divider)
                }
                trimmed.startsWith("#") -> {
                    var level = 0
                    while (level < trimmed.length && trimmed[level] == '#') {
                        level++
                    }
                    if (level in 1..6 && trimmed.length > level && trimmed[level] == ' ') {
                        nodes.add(MarkdownNode.Heading(level, trimmed.substring(level).trim()))
                    } else {
                        nodes.add(MarkdownNode.Paragraph(line))
                    }
                }
                trimmed.startsWith("- [ ]") || trimmed.startsWith("* [ ]") -> {
                    nodes.add(MarkdownNode.ChecklistItem(false, trimmed.substring(5).trim()))
                }
                trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]") ||
                trimmed.startsWith("* [x]") || trimmed.startsWith("* [X]") -> {
                    nodes.add(MarkdownNode.ChecklistItem(true, trimmed.substring(5).trim()))
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    nodes.add(MarkdownNode.BulletItem(trimmed.substring(2).trim()))
                }
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val number = trimmed.substringBefore('.')
                    val text = trimmed.substringAfter('.').trim()
                    nodes.add(MarkdownNode.NumberedItem(number, text))
                }
                trimmed.startsWith("> ") -> {
                    nodes.add(MarkdownNode.Blockquote(trimmed.removePrefix("> ").trim()))
                }
                trimmed.isBlank() -> {
                    // Blank spacer line
                }
                else -> {
                    nodes.add(MarkdownNode.Paragraph(line))
                }
            }
        }

        if (inCodeBlock && codeBuffer.isNotEmpty()) {
            nodes.add(MarkdownNode.CodeBlock(codeBuffer.toString().trimEnd(), codeBlockLang))
        }

        return nodes
    }

    fun generateHtml(title: String, markdown: String, isDark: Boolean = false): String {
        val nodes = parseNodes(markdown)
        val bodyBuilder = StringBuilder()

        for (node in nodes) {
            when (node) {
                is MarkdownNode.Heading -> {
                    val tag = "h${node.level}"
                    bodyBuilder.append("<$tag>${escapeHtml(node.text)}</$tag>\n")
                }
                is MarkdownNode.Paragraph -> {
                    bodyBuilder.append("<p>${formatInlineHtml(node.text)}</p>\n")
                }
                is MarkdownNode.BulletItem -> {
                    bodyBuilder.append("<ul><li>${formatInlineHtml(node.text)}</li></ul>\n")
                }
                is MarkdownNode.NumberedItem -> {
                    bodyBuilder.append("<ol start='${node.number}'><li>${formatInlineHtml(node.text)}</li></ol>\n")
                }
                is MarkdownNode.ChecklistItem -> {
                    val checked = if (node.isChecked) "checked" else ""
                    bodyBuilder.append("<div class='checklist-item'><input type='checkbox' $checked disabled/> ${formatInlineHtml(node.text)}</div>\n")
                }
                is MarkdownNode.Blockquote -> {
                    bodyBuilder.append("<blockquote>${formatInlineHtml(node.text)}</blockquote>\n")
                }
                is MarkdownNode.CodeBlock -> {
                    bodyBuilder.append("<pre><code>${escapeHtml(node.code)}</code></pre>\n")
                }
                is MarkdownNode.Divider -> {
                    bodyBuilder.append("<hr/>\n")
                }
            }
        }

        val bgColor = if (isDark) "#0F172A" else "#FFFFFF"
        val textColor = if (isDark) "#F8FAFC" else "#0F172A"
        val surfaceColor = if (isDark) "#1E293B" else "#F8FAFC"
        val borderColor = if (isDark) "#334155" else "#E2E8F0"

        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${escapeHtml(title)}</title>
<style>
  body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    line-height: 1.65;
    color: $textColor;
    background-color: $bgColor;
    padding: 24px;
    max-width: 800px;
    margin: 0 auto;
  }
  h1, h2, h3, h4, h5, h6 {
    margin-top: 1.5em;
    margin-bottom: 0.5em;
    font-weight: 600;
    line-height: 1.25;
  }
  h1 { font-size: 2em; border-bottom: 1px solid $borderColor; padding-bottom: 0.3em; }
  h2 { font-size: 1.5em; }
  h3 { font-size: 1.25em; }
  p { margin: 0.8em 0; }
  code {
    background-color: $surfaceColor;
    border: 1px solid $borderColor;
    padding: 0.2em 0.4em;
    border-radius: 4px;
    font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
    font-size: 85%;
  }
  pre {
    background-color: $surfaceColor;
    border: 1px solid $borderColor;
    padding: 16px;
    border-radius: 8px;
    overflow-x: auto;
  }
  pre code {
    background: transparent;
    border: none;
    padding: 0;
  }
  blockquote {
    margin: 1em 0;
    padding-left: 16px;
    border-left: 4px solid #2563EB;
    color: #64748B;
  }
  ul, ol { padding-left: 24px; margin: 0.5em 0; }
  li { margin: 0.25em 0; }
  .checklist-item { margin: 0.4em 0; display: flex; align-items: center; gap: 8px; }
  hr { border: none; border-top: 1px solid $borderColor; margin: 2em 0; }
</style>
</head>
<body>
$bodyBuilder
</body>
</html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun formatInlineHtml(text: String): String {
        var formatted = escapeHtml(text)
        // Bold: **text**
        formatted = formatted.replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
        // Italic: *text*
        formatted = formatted.replace(Regex("\\*(.*?)\\*"), "<em>$1</em>")
        // Inline code: `text`
        formatted = formatted.replace(Regex("`(.*?)`"), "<code>$1</code>")
        return formatted
    }
}

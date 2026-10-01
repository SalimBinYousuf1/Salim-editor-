package com.example.util

data class OutlineItem(
    val title: String,
    val level: Int,
    val lineIndex: Int = 0
)

object MarkdownParser {

    fun extractOutline(markdown: String): List<OutlineItem> {
        val outline = mutableListOf<OutlineItem>()
        val lines = markdown.lines()
        for ((idx, line) in lines.withIndex()) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    outline.add(OutlineItem(trimmed.removePrefix("### ").trim(), 3, idx))
                }
                trimmed.startsWith("## ") -> {
                    outline.add(OutlineItem(trimmed.removePrefix("## ").trim(), 2, idx))
                }
                trimmed.startsWith("# ") -> {
                    outline.add(OutlineItem(trimmed.removePrefix("# ").trim(), 1, idx))
                }
            }
        }
        return outline
    }

    fun generateHtml(title: String, markdown: String): String {
        val bodyBuilder = StringBuilder()
        val lines = markdown.lines()
        var inList = false

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("# ") -> {
                    if (inList) { bodyBuilder.append("</ul>\n"); inList = false }
                    bodyBuilder.append("<h1>").append(parseInlineHtml(trimmed.removePrefix("# "))).append("</h1>\n")
                }
                trimmed.startsWith("## ") -> {
                    if (inList) { bodyBuilder.append("</ul>\n"); inList = false }
                    bodyBuilder.append("<h2>").append(parseInlineHtml(trimmed.removePrefix("## "))).append("</h2>\n")
                }
                trimmed.startsWith("### ") -> {
                    if (inList) { bodyBuilder.append("</ul>\n"); inList = false }
                    bodyBuilder.append("<h3>").append(parseInlineHtml(trimmed.removePrefix("### "))).append("</h3>\n")
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    if (!inList) { bodyBuilder.append("<ul>\n"); inList = true }
                    val item = trimmed.substring(2).trim()
                    bodyBuilder.append("<li>").append(parseInlineHtml(item)).append("</li>\n")
                }
                trimmed.isBlank() -> {
                    if (inList) { bodyBuilder.append("</ul>\n"); inList = false }
                }
                else -> {
                    if (inList) { bodyBuilder.append("</ul>\n"); inList = false }
                    bodyBuilder.append("<p>").append(parseInlineHtml(trimmed)).append("</p>\n")
                }
            }
        }
        if (inList) { bodyBuilder.append("</ul>\n") }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>$title</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; padding: 24px; color: #111; max-width: 780px; margin: 0 auto; }
                    code { background: #f3f4f6; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 0.9em; }
                    pre code { display: block; padding: 12px; overflow-x: auto; background: #1e293b; color: #f8fafc; border-radius: 8px; }
                    blockquote { border-left: 4px solid #3b82f6; margin: 0; padding-left: 16px; color: #4b5563; }
                </style>
            </head>
            <body>
            $bodyBuilder
            </body>
            </html>
        """.trimIndent()
    }

    private fun parseInlineHtml(text: String): String {
        var result = text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        // Bold: **text**
        result = result.replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
        // Italic: *text*
        result = result.replace(Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)"), "<em>$1</em>")
        // Code: `code`
        result = result.replace(Regex("`(.*?)`"), "<code>$1</code>")

        return result
    }
}

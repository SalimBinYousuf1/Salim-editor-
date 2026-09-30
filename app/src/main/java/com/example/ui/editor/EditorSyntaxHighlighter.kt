package com.example.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Locale

enum class EditorLanguage(val displayName: String, val extensions: List<String>) {
    KOTLIN("Kotlin", listOf("kt", "kts")),
    JAVA("Java", listOf("java")),
    PYTHON("Python", listOf("py")),
    JAVASCRIPT("JavaScript", listOf("js", "jsx", "mjs")),
    TYPESCRIPT("TypeScript", listOf("ts", "tsx")),
    JSON("JSON", listOf("json")),
    XML_HTML("HTML / XML", listOf("html", "htm", "xml", "svg", "xhtml")),
    CSS("CSS", listOf("css", "scss", "less")),
    MARKDOWN("Markdown", listOf("md", "markdown")),
    SQL("SQL", listOf("sql")),
    SHELL("Shell / Bash", listOf("sh", "bash", "zsh")),
    YAML_INI("YAML / Config", listOf("yaml", "yml", "properties", "ini", "env", "conf", "toml")),
    C_CPP("C / C++", listOf("c", "h", "cpp", "hpp", "cc")),
    RUST_GO("Rust / Go", listOf("rs", "go")),
    PLAINTEXT("Plain Text", listOf("txt", "log", "csv", "tsv"));

    companion object {
        fun fromExtension(ext: String): EditorLanguage {
            val lower = ext.lowercase(Locale.ROOT)
            return entries.firstOrNull { it.extensions.contains(lower) } ?: PLAINTEXT
        }

        fun fromFileName(name: String): EditorLanguage {
            val dot = name.lastIndexOf('.')
            if (dot >= 0 && dot < name.length - 1) {
                val ext = name.substring(dot + 1)
                return fromExtension(ext)
            }
            if (name.startsWith(".env")) return YAML_INI
            return PLAINTEXT
        }
    }
}

class SyntaxHighlighterVisualTransformation(
    private val language: EditorLanguage,
    private val theme: EditorColorScheme,
    private val searchQuery: String = "",
    private val activeMatchIndex: Int = -1,
    private val matchCase: Boolean = false
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val builder = AnnotatedString.Builder(raw)
        // Default text color
        builder.addStyle(SpanStyle(color = theme.text), 0, raw.length)

        try {
            // Apply language syntax highlighting
            when (language) {
                EditorLanguage.KOTLIN, EditorLanguage.JAVA, EditorLanguage.C_CPP, EditorLanguage.RUST_GO -> {
                    highlightCStyle(builder, raw, language)
                }
                EditorLanguage.PYTHON -> {
                    highlightPython(builder, raw)
                }
                EditorLanguage.JAVASCRIPT, EditorLanguage.TYPESCRIPT -> {
                    highlightJsTs(builder, raw)
                }
                EditorLanguage.JSON -> {
                    highlightJson(builder, raw)
                }
                EditorLanguage.XML_HTML -> {
                    highlightXmlHtml(builder, raw)
                }
                EditorLanguage.CSS -> {
                    highlightCss(builder, raw)
                }
                EditorLanguage.MARKDOWN -> {
                    highlightMarkdown(builder, raw)
                }
                EditorLanguage.SQL -> {
                    highlightSql(builder, raw)
                }
                EditorLanguage.SHELL -> {
                    highlightShell(builder, raw)
                }
                EditorLanguage.YAML_INI -> {
                    highlightYaml(builder, raw)
                }
                EditorLanguage.PLAINTEXT -> {
                    // Plain text only has search matches
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully on parsing errors
        }

        // Apply Search Highlight overlay
        if (searchQuery.isNotEmpty()) {
            try {
                val pattern = Regex.escape(searchQuery)
                val options = if (matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE)
                val regex = Regex(pattern, options)
                val matches = regex.findAll(raw).toList()
                matches.forEachIndexed { index, matchResult ->
                    val range = matchResult.range
                    val isCurrent = index == activeMatchIndex
                    val bg = if (isCurrent) theme.searchMatchCurrent else theme.searchMatch
                    val textCol = if (isCurrent) theme.background else theme.text
                    builder.addStyle(
                        SpanStyle(
                            background = bg,
                            color = textCol,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        ),
                        range.first,
                        range.last + 1
                    )
                }
            } catch (_: Exception) {
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    private fun highlightCStyle(builder: AnnotatedString.Builder, text: String, lang: EditorLanguage) {
        // Strings ("..." and '...')
        val stringRegex = Regex("(\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\.[^'\\\\]*)*')")
        stringRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.string), it.range.first, it.range.last + 1)
        }

        // Numbers
        val numRegex = Regex("\\b(?:0x[0-9a-fA-F]+|0b[01]+|\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?[fFL]?)\\b")
        numRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.number), it.range.first, it.range.last + 1)
        }

        // Keywords
        val keywords = when (lang) {
            EditorLanguage.KOTLIN -> setOf(
                "package", "import", "class", "interface", "object", "val", "var", "fun", "return",
                "if", "else", "when", "for", "while", "do", "try", "catch", "finally", "throw",
                "true", "false", "null", "this", "super", "is", "as", "in", "by", "data", "sealed",
                "override", "private", "public", "protected", "internal", "companion", "suspend",
                "inline", "reified", "enum", "typealias", "constructor", "init"
            )
            EditorLanguage.JAVA -> setOf(
                "package", "import", "class", "interface", "extends", "implements", "public",
                "private", "protected", "static", "final", "void", "return", "if", "else", "for",
                "while", "do", "switch", "case", "default", "break", "continue", "try", "catch",
                "finally", "throw", "throws", "new", "this", "super", "null", "true", "false",
                "synchronized", "volatile", "transient", "native", "abstract"
            )
            else -> setOf(
                "fn", "let", "mut", "pub", "struct", "enum", "impl", "trait", "match", "return",
                "if", "else", "for", "while", "loop", "func", "type", "package", "import", "go",
                "defer", "select", "chan", "true", "false", "null", "nil"
            )
        }

        val kwPattern = Regex("\\b(" + keywords.joinToString("|") + ")\\b")
        kwPattern.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.keyword, fontWeight = FontWeight.SemiBold), it.range.first, it.range.last + 1)
        }

        // Annotations / Types (e.g. @Composable, String, Int)
        val annotationRegex = Regex("@[A-Za-z0-9_]+")
        annotationRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.attribute), it.range.first, it.range.last + 1)
        }

        val typeRegex = Regex("\\b[A-Z][A-Za-z0-9_]+\\b")
        typeRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.type), it.range.first, it.range.last + 1)
        }

        // Single-line comments // ...
        val singleCommentRegex = Regex("//.*")
        singleCommentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }

        // Multi-line comments /* ... */
        val multiCommentRegex = Regex("/\\*[\\s\\S]*?\\*/")
        multiCommentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }
    }

    private fun highlightPython(builder: AnnotatedString.Builder, text: String) {
        // Strings
        val stringRegex = Regex("(\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\.[^'\\\\]*)*')")
        stringRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.string), it.range.first, it.range.last + 1)
        }

        // Numbers
        val numRegex = Regex("\\b(?:0x[0-9a-fA-F]+|\\d+(?:\\.\\d+)?)\\b")
        numRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.number), it.range.first, it.range.last + 1)
        }

        val pyKeywords = setOf(
            "def", "class", "import", "from", "as", "return", "if", "elif", "else", "while", "for",
            "in", "try", "except", "finally", "raise", "with", "yield", "lambda", "global",
            "nonlocal", "pass", "break", "continue", "True", "False", "None", "async", "await", "assert"
        )
        val kwPattern = Regex("\\b(" + pyKeywords.joinToString("|") + ")\\b")
        kwPattern.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.keyword, fontWeight = FontWeight.SemiBold), it.range.first, it.range.last + 1)
        }

        // Decorators
        val decoratorRegex = Regex("@[A-Za-z0-9_]+")
        decoratorRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.attribute), it.range.first, it.range.last + 1)
        }

        // Comments # ...
        val commentRegex = Regex("#.*")
        commentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }
    }

    private fun highlightJsTs(builder: AnnotatedString.Builder, text: String) {
        // Strings and template literals
        val strRegex = Regex("(`[^`\\\\]*(?:\\\\.[^`\\\\]*)*`|\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\.[^'\\\\]*)*')")
        strRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.string), it.range.first, it.range.last + 1)
        }

        // Numbers
        val numRegex = Regex("\\b(?:\\d+(?:\\.\\d+)?)\\b")
        numRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.number), it.range.first, it.range.last + 1)
        }

        val jsKeywords = setOf(
            "const", "let", "var", "function", "return", "if", "else", "for", "while", "do",
            "switch", "case", "default", "break", "continue", "import", "export", "from", "as",
            "default", "class", "extends", "new", "this", "super", "try", "catch", "finally",
            "throw", "typeof", "instanceof", "async", "await", "yield", "true", "false", "null",
            "undefined", "interface", "type", "enum", "implements", "public", "private"
        )
        val kwPattern = Regex("\\b(" + jsKeywords.joinToString("|") + ")\\b")
        kwPattern.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.keyword, fontWeight = FontWeight.SemiBold), it.range.first, it.range.last + 1)
        }

        // Comments
        val commentRegex = Regex("//.*")
        commentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }
        val multiComment = Regex("/\\*[\\s\\S]*?\\*/")
        multiComment.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }
    }

    private fun highlightJson(builder: AnnotatedString.Builder, text: String) {
        // Keys: "key":
        val keyRegex = Regex("\"([^\"]+)\"\\s*:")
        keyRegex.findAll(text).forEach {
            val keyRange = it.groups[1]?.range
            if (keyRange != null) {
                builder.addStyle(SpanStyle(color = theme.tag, fontWeight = FontWeight.SemiBold), keyRange.first - 1, keyRange.last + 2)
            }
        }

        // String values: : "value"
        val valStrRegex = Regex(":\\s*(\"[^\"]*\")")
        valStrRegex.findAll(text).forEach {
            val strRange = it.groups[1]?.range
            if (strRange != null) {
                builder.addStyle(SpanStyle(color = theme.string), strRange.first, strRange.last + 1)
            }
        }

        // Numbers, booleans, null
        val literalRegex = Regex(":\\s*(-?\\d+(?:\\.\\d+)?|true|false|null)")
        literalRegex.findAll(text).forEach {
            val range = it.groups[1]?.range
            if (range != null) {
                val matchVal = it.groups[1]?.value ?: ""
                val col = if (matchVal == "true" || matchVal == "false" || matchVal == "null") theme.keyword else theme.number
                builder.addStyle(SpanStyle(color = col, fontWeight = FontWeight.SemiBold), range.first, range.last + 1)
            }
        }
    }

    private fun highlightXmlHtml(builder: AnnotatedString.Builder, text: String) {
        // Comments <!-- ... -->
        val commentRegex = Regex("<!--[\\s\\S]*?-->")
        commentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }

        // Tags <tag> or </tag>
        val tagRegex = Regex("</?([A-Za-z0-9_-]+)")
        tagRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.tag, fontWeight = FontWeight.SemiBold), it.range.first, it.range.last + 1)
        }

        // Attributes attr="value"
        val attrRegex = Regex("([A-Za-z0-9_:-]+)=\"([^\"]*)\"")
        attrRegex.findAll(text).forEach {
            val nameGroup = it.groups[1]?.range
            val valGroup = it.groups[2]?.range
            if (nameGroup != null) {
                builder.addStyle(SpanStyle(color = theme.attribute), nameGroup.first, nameGroup.last + 1)
            }
            if (valGroup != null) {
                builder.addStyle(SpanStyle(color = theme.string), valGroup.first - 1, valGroup.last + 2)
            }
        }
    }

    private fun highlightCss(builder: AnnotatedString.Builder, text: String) {
        // Selectors
        val selectorRegex = Regex("([.#]?[A-Za-z0-9_-]+)\\s*\\{")
        selectorRegex.findAll(text).forEach {
            val range = it.groups[1]?.range
            if (range != null) {
                builder.addStyle(SpanStyle(color = theme.keyword, fontWeight = FontWeight.SemiBold), range.first, range.last + 1)
            }
        }
        // Properties
        val propRegex = Regex("([A-Za-z-]+)\\s*:")
        propRegex.findAll(text).forEach {
            val range = it.groups[1]?.range
            if (range != null) {
                builder.addStyle(SpanStyle(color = theme.attribute), range.first, range.last + 1)
            }
        }
        // Comments
        val commentRegex = Regex("/\\*[\\s\\S]*?\\*/")
        commentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }
    }

    private fun highlightMarkdown(builder: AnnotatedString.Builder, text: String) {
        // Headers (# ...)
        val headerRegex = Regex("^(#{1,6}\\s+.*)$", RegexOption.MULTILINE)
        headerRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.keyword, fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1)
        }

        // Inline code `...`
        val codeRegex = Regex("`[^`\n]+`")
        codeRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.type, background = theme.activeLineBg), it.range.first, it.range.last + 1)
        }

        // Bold & Italic
        val boldRegex = Regex("\\*\\*([^*]+)\\*\\*")
        boldRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = theme.text), it.range.first, it.range.last + 1)
        }

        // Links [text](url)
        val linkRegex = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        linkRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.cursor), it.range.first, it.range.last + 1)
        }
    }

    private fun highlightSql(builder: AnnotatedString.Builder, text: String) {
        val keywords = setOf(
            "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE", "TABLE",
            "DROP", "ALTER", "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "ON", "AND", "OR",
            "NOT", "NULL", "IS", "IN", "AS", "ORDER", "BY", "GROUP", "HAVING", "LIMIT",
            "OFFSET", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "VALUES", "SET", "DEFAULT"
        )
        val kwPattern = Regex("(?i)\\b(" + keywords.joinToString("|") + ")\\b")
        kwPattern.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.keyword, fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1)
        }
        val strRegex = Regex("('[^']*')")
        strRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.string), it.range.first, it.range.last + 1)
        }
        val commentRegex = Regex("(--.*)")
        commentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }
    }

    private fun highlightShell(builder: AnnotatedString.Builder, text: String) {
        val keywords = setOf("if", "then", "else", "elif", "fi", "for", "while", "do", "done", "in", "case", "esac", "echo", "exit", "return", "export")
        val kwPattern = Regex("\\b(" + keywords.joinToString("|") + ")\\b")
        kwPattern.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.keyword, fontWeight = FontWeight.SemiBold), it.range.first, it.range.last + 1)
        }
        val vars = Regex("\\$[A-Za-z0-9_{}]+")
        vars.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.attribute), it.range.first, it.range.last + 1)
        }
        val commentRegex = Regex("#.*")
        commentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }
    }

    private fun highlightYaml(builder: AnnotatedString.Builder, text: String) {
        val keyRegex = Regex("^\\s*([A-Za-z0-9_.-]+)\\s*:", RegexOption.MULTILINE)
        keyRegex.findAll(text).forEach {
            val range = it.groups[1]?.range
            if (range != null) {
                builder.addStyle(SpanStyle(color = theme.tag, fontWeight = FontWeight.SemiBold), range.first, range.last + 1)
            }
        }
        val commentRegex = Regex("#.*")
        commentRegex.findAll(text).forEach {
            builder.addStyle(SpanStyle(color = theme.comment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1)
        }
    }
}

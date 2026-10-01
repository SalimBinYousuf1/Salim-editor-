package com.example

import com.example.data.repository.EditorFont
import com.example.data.repository.EditorPreset
import com.example.data.repository.EditorSettings
import com.example.data.repository.EditorThemeMode
import com.example.ui.theme.parseHexColor
import com.example.util.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMarkdownOutline() {
        val content = "# Heading 1\nSome text\n## Heading 2\nMore text"
        val outline = MarkdownParser.extractOutline(content)
        assertEquals(2, outline.size)
        assertEquals("Heading 1", outline[0].title)
        assertEquals(1, outline[0].level)
        assertEquals("Heading 2", outline[1].title)
        assertEquals(2, outline[1].level)
    }

    @Test
    fun testWordCountMetrics() {
        val text = "The quick brown fox jumps over the lazy dog."
        val stats = MarkdownParser.computeDetailedStats(text)
        assertEquals(9, stats.wordCount)
        assertEquals(44, stats.charCount)
        assertEquals(36, stats.charNoSpaceCount)
    }

    @Test
    fun testColorParsing() {
        val color = parseHexColor("#2563EB")
        assertNotNull(color)
    }

    @Test
    fun testDefaultEditorSettings() {
        val settings = EditorSettings()
        assertEquals(EditorThemeMode.SYSTEM, settings.themeMode)
        assertEquals(EditorFont.SYSTEM_DEFAULT, settings.font)
        assertEquals(14f, settings.fontSize, 0.01f)
        assertTrue(settings.wordWrap)
        assertTrue(settings.quickAccessoryBar)
    }
}

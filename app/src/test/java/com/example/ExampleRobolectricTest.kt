package com.example

import com.example.data.repository.DiffType
import com.example.data.repository.DocumentRepository
import com.example.util.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testMarkdownOutlineExtraction() {
        val md = """
            # Title 1
            Some content here.
            ## Subheading 1.1
            More text.
            ### Section 1.1.1
            Text.
            # Title 2
        """.trimIndent()

        val outline = MarkdownParser.extractOutline(md)
        assertEquals(4, outline.size)
        assertEquals("Title 1", outline[0].title)
        assertEquals(1, outline[0].level)
        assertEquals("Subheading 1.1", outline[1].title)
        assertEquals(2, outline[1].level)
        assertEquals("Section 1.1.1", outline[2].title)
        assertEquals(3, outline[2].level)
        assertEquals("Title 2", outline[3].title)
        assertEquals(1, outline[3].level)
    }

    @Test
    fun testMarkdownHtmlGeneration() {
        val md = "# Heading\n**Bold text** and *italic* and `code`"
        val html = MarkdownParser.generateHtml("Test Doc", md)
        assertTrue(html.contains("<h1>Heading</h1>"))
        assertTrue(html.contains("<strong>Bold text</strong>"))
        assertTrue(html.contains("<em>italic</em>"))
        assertTrue(html.contains("<code>code</code>"))
    }

    @Test
    fun testDiffComputation() {
        val oldText = "Line 1\nLine 2\nLine 3"
        val newText = "Line 1\nLine 2 (edited)\nLine 3\nLine 4"

        val diff = DocumentRepository.computeDiff(oldText, newText)
        assertTrue(diff.any { it.type == DiffType.UNCHANGED && it.text == "Line 1" })
        assertTrue(diff.any { it.type == DiffType.ADDED && it.text == "Line 4" })
    }
}

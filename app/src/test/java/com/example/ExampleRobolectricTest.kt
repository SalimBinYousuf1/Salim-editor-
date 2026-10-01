package com.example

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
    fun testMarkdownHtmlRendering() {
        val md = "# Heading\n**Bold text** and *italic* and `code`"
        val html = MarkdownParser.renderToHtml(md)
        assertTrue(html.contains("<h1>Heading</h1>"))
        assertTrue(html.contains("<strong>Bold text</strong>"))
        assertTrue(html.contains("<em>italic</em>"))
        assertTrue(html.contains("<code>code</code>"))
    }
}

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalEditorColors
import com.example.util.MarkdownNode
import com.example.util.MarkdownParser

@Composable
fun MarkdownPreviewView(
    markdownContent: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalEditorColors.current
    val nodes = MarkdownParser.parseNodes(markdownContent)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.editorCanvas
    ) {
        if (nodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No content to preview.\nStart typing Markdown in the editor!",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(nodes) { node ->
                    when (node) {
                        is MarkdownNode.Heading -> {
                            val fontSize = when (node.level) {
                                1 -> 24.sp
                                2 -> 20.sp
                                3 -> 17.sp
                                else -> 15.sp
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = node.text,
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    lineHeight = (fontSize.value * 1.3).sp
                                )
                                if (node.level == 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider(color = colors.divider, thickness = 1.dp)
                                }
                            }
                        }

                        is MarkdownNode.Paragraph -> {
                            Text(
                                text = node.text,
                                fontSize = 15.sp,
                                color = colors.textPrimary,
                                lineHeight = 22.sp
                            )
                        }

                        is MarkdownNode.BulletItem -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 16.sp,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(16.dp)
                                )
                                Text(
                                    text = node.text,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        is MarkdownNode.NumberedItem -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${node.number}.",
                                    fontSize = 14.sp,
                                    color = colors.accent,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = node.text,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        is MarkdownNode.ChecklistItem -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (node.isChecked) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = if (node.isChecked) colors.accent else colors.textTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = node.text,
                                    fontSize = 14.sp,
                                    color = if (node.isChecked) colors.textSecondary else colors.textPrimary,
                                    style = if (node.isChecked) androidx.compose.ui.text.TextStyle(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    ) else androidx.compose.ui.text.TextStyle.Default
                                )
                            }
                        }

                        is MarkdownNode.Blockquote -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(colors.accent)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = node.text,
                                    fontSize = 14.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = colors.textSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        is MarkdownNode.CodeBlock -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.secondarySurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (node.language.isNotBlank()) {
                                        Text(
                                            text = node.language.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                    Text(
                                        text = node.code,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        is MarkdownNode.Divider -> {
                            HorizontalDivider(
                                color = colors.divider,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

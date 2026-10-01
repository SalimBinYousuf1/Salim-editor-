package com.example.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalEditorColors
import com.example.util.MarkdownOutlineItem
import com.example.util.MarkdownParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentOutlineSheet(
    content: String,
    onSelectHeading: (MarkdownOutlineItem) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalEditorColors.current
    val outlineItems = remember(content) {
        MarkdownParser.extractOutline(content)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.FormatListBulleted,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Document Outline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${outlineItems.size} headings",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            HorizontalDivider(color = colors.divider)

            if (outlineItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Headings Detected",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Use #, ##, or ### to structure your document outline.",
                            color = colors.textTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(outlineItems) { item ->
                        val indent = ((item.level - 1) * 16).dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectHeading(item)
                                    onDismiss()
                                }
                                .padding(start = indent, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "H${item.level}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                fontSize = (16 - item.level).coerceAtLeast(13).sp,
                                fontWeight = if (item.level <= 2) FontWeight.SemiBold else FontWeight.Normal,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Ln ${item.lineIndex}",
                                fontSize = 11.sp,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

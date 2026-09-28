package com.example.ui.editor.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DocumentEntity
import com.example.ui.theme.LocalEditorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentStatsModal(
    document: DocumentEntity,
    currentContent: String,
    onDismiss: () -> Unit
) {
    val colors = LocalEditorColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val wordCount = if (currentContent.isBlank()) 0 else currentContent.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    val charCount = currentContent.length
    val charNoSpaces = currentContent.count { !it.isWhitespace() }
    val lineCount = if (currentContent.isEmpty()) 1 else currentContent.lines().size
    val paragraphCount = currentContent.split(Regex("(?m)^\\s*$")).count { it.isNotBlank() }
    val readingMinutes = (wordCount / 200.0).coerceAtLeast(0.1)
    val readingTime = if (readingMinutes < 1.0) "< 1 min read" else "${Math.round(readingMinutes)} min read"
    val sizeBytes = currentContent.toByteArray(Charsets.UTF_8).size
    val formattedSize = when {
        sizeBytes < 1024 -> "$sizeBytes B"
        sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
        else -> String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Document Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Words",
                    value = "$wordCount",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Characters",
                    value = "$charCount",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Lines",
                    value = "$lineCount",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Detailed List
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.secondarySurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatRow(label = "Reading Time", value = readingTime)
                    StatRow(label = "Characters (no spaces)", value = "$charNoSpaces")
                    StatRow(label = "Paragraphs", value = "$paragraphCount")
                    StatRow(label = "File Size", value = formattedSize)
                    StatRow(label = "Format & Extension", value = ".${document.extension.uppercase()} (${document.syntax.replaceFirstChar { it.uppercase() }})")
                    StatRow(label = "Encoding", value = document.encoding)
                    StatRow(label = "Line Endings", value = document.lineEnding)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalEditorColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.secondarySurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String
) {
    val colors = LocalEditorColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = colors.textSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
    }
}

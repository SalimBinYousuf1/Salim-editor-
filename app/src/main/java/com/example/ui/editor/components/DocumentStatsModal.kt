package com.example.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DocumentEntity
import com.example.ui.theme.LocalEditorColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentStatsModal(
    document: DocumentEntity,
    currentContent: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val colors = LocalEditorColors.current

    val words = if (currentContent.isBlank()) 0 else currentContent.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
    val characters = currentContent.length
    val charactersNoSpaces = currentContent.count { !it.isWhitespace() }
    val lines = if (currentContent.isEmpty()) 0 else currentContent.lines().size
    val readTimeMinutes = maxOf(1, (words / 200))
    val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.accent
                )
                Text(
                    text = "File Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${document.title}.${document.extension}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Syntax: ${document.syntax.uppercase()}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatRow("Words", "$words", colors.textPrimary, colors.textSecondary)
                HorizontalDivider(color = colors.divider.copy(alpha = 0.5f))
                StatRow("Characters", "$characters ($charactersNoSpaces without spaces)", colors.textPrimary, colors.textSecondary)
                HorizontalDivider(color = colors.divider.copy(alpha = 0.5f))
                StatRow("Lines", "$lines", colors.textPrimary, colors.textSecondary)
                HorizontalDivider(color = colors.divider.copy(alpha = 0.5f))
                StatRow("Estimated Read Time", "~$readTimeMinutes min", colors.textPrimary, colors.textSecondary)
                HorizontalDivider(color = colors.divider.copy(alpha = 0.5f))
                StatRow("Created", dateFormat.format(Date(document.createdAt)), colors.textPrimary, colors.textSecondary)
                HorizontalDivider(color = colors.divider.copy(alpha = 0.5f))
                StatRow("Modified", dateFormat.format(Date(document.updatedAt)), colors.textPrimary, colors.textSecondary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", color = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    primaryColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = secondaryColor)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primaryColor)
    }
}

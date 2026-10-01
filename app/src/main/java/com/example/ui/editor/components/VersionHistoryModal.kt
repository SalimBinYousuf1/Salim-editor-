package com.example.ui.editor.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DocumentVersionEntity
import com.example.ui.theme.LocalEditorColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryModal(
    versions: List<DocumentVersionEntity>,
    onRestoreVersion: (DocumentVersionEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalEditorColors.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · h:mm:ss a", Locale.getDefault()) }
    var selectedVersion by remember { mutableStateOf<DocumentVersionEntity?>(versions.firstOrNull()) }

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
                    Icons.Outlined.History,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Revision & Version History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${versions.size} snapshots",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            HorizontalDivider(color = colors.divider)

            if (versions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No prior snapshots available yet.\nSnapshots are created periodically as you write.",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: List of versions
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(versions) { ver ->
                            val isSelected = selectedVersion?.id == ver.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.surfaceVariant else Color.Transparent)
                                    .clickable { selectedVersion = ver }
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = ver.note,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.accent else colors.textPrimary
                                    )
                                    Text(
                                        text = dateFormat.format(Date(ver.timestamp)),
                                        fontSize = 10.sp,
                                        color = colors.textTertiary
                                    )
                                    Text(
                                        text = "${ver.wordCount} words",
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Right: Version preview snippet
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Snapshot Preview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedVersion?.content?.take(600) ?: "Select a version",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }

                        selectedVersion?.let { ver ->
                            Button(
                                onClick = {
                                    onRestoreVersion(ver)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore This", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

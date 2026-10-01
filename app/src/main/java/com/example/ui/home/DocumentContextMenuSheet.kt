package com.example.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.FolderEntity
import com.example.ui.theme.LocalEditorColors
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentContextMenuSheet(
    document: DocumentEntity,
    folders: List<FolderEntity>,
    onOpen: () -> Unit,
    onRename: (String) -> Unit,
    onMoveToFolder: (Long?) -> Unit,
    onDuplicate: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onManageTags: (String) -> Unit,
    onDelete: () -> Unit,
    onShowDetails: () -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    val colors = LocalEditorColors.current
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMoveFolderDialog by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header with Document Title & Extension
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = document.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${document.extension.uppercase()} · ${document.wordCount} words · ${document.charCount} chars",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(vertical = 6.dp))

            // Actions
            MenuItem(
                icon = Icons.Default.OpenInNew,
                title = "Open Editor",
                tint = colors.textPrimary,
                onClick = {
                    onDismiss()
                    onOpen()
                }
            )

            MenuItem(
                icon = if (document.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                title = if (document.isPinned) "Unpin Document" else "Pin to Top",
                tint = if (document.isPinned) colors.accent else colors.textPrimary,
                onClick = {
                    onTogglePin()
                    onDismiss()
                }
            )

            MenuItem(
                icon = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                title = if (document.isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = if (document.isFavorite) Color(0xFFFF9500) else colors.textPrimary,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.Edit,
                title = "Rename Document",
                tint = colors.textPrimary,
                onClick = {
                    showRenameDialog = true
                }
            )

            MenuItem(
                icon = Icons.Default.DriveFileMove,
                title = "Move to Folder",
                tint = colors.textPrimary,
                onClick = {
                    showMoveFolderDialog = true
                }
            )

            MenuItem(
                icon = Icons.Default.Label,
                title = "Manage Tags",
                tint = colors.textPrimary,
                onClick = {
                    showTagsDialog = true
                }
            )

            MenuItem(
                icon = Icons.Default.ContentCopy,
                title = "Duplicate Document",
                tint = colors.textPrimary,
                onClick = {
                    onDuplicate()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.Share,
                title = "Share Content",
                tint = colors.textPrimary,
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, document.title)
                        putExtra(Intent.EXTRA_TEXT, document.content)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Document"))
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.Info,
                title = "Details & Analytics",
                tint = colors.textPrimary,
                onClick = {
                    onDismiss()
                    onShowDetails()
                }
            )

            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(vertical = 6.dp))

            MenuItem(
                icon = Icons.Default.Delete,
                title = "Move to Trash",
                tint = SalimRed,
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(document.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Document", color = colors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) onRename(newTitle.trim())
                        showRenameDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Rename", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface
        )
    }

    if (showMoveFolderDialog) {
        AlertDialog(
            onDismissRequest = { showMoveFolderDialog = false },
            title = { Text("Move to Folder", color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = {
                            onMoveToFolder(null)
                            showMoveFolderDialog = false
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Root (No Folder)", color = colors.textPrimary)
                    }
                    folders.forEach { folder ->
                        TextButton(
                            onClick = {
                                onMoveToFolder(folder.id)
                                showMoveFolderDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📁 ${folder.name}", color = colors.accent)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMoveFolderDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface
        )
    }

    if (showTagsDialog) {
        var tagsText by remember { mutableStateOf(document.tags) }
        AlertDialog(
            onDismissRequest = { showTagsDialog = false },
            title = { Text("Document Tags", color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter comma-separated tags:", fontSize = 12.sp, color = colors.textSecondary)
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        placeholder = { Text("e.g. Work, Notes, Study") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onManageTags(tagsText.trim())
                        showTagsDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Save Tags", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagsDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}

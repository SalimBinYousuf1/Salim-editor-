package com.example.ui.home

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.FolderEntity
import com.example.ui.theme.LocalEditorColors
import com.example.util.PrintHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(document.title) }

    var showMoveFolderDialog by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }
    var tagsInput by remember { mutableStateOf(document.tags) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // Header: Title, Extension badge, Modified date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.accent.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = document.extension.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${document.wordCount} words · Modified ${dateFormat.format(Date(document.updatedAt))}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Items
            SheetActionItem(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                title = "Open Document",
                onClick = { onDismiss(); onOpen() }
            )

            SheetActionItem(
                icon = Icons.Default.Edit,
                title = "Rename",
                onClick = { showRenameDialog = true }
            )

            SheetActionItem(
                icon = if (document.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                title = if (document.isPinned) "Unpin Document" else "Pin to Top",
                onClick = { onTogglePin(); onDismiss() }
            )

            SheetActionItem(
                icon = if (document.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                title = if (document.isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = if (document.isFavorite) Color(0xFFF59E0B) else null,
                onClick = { onToggleFavorite(); onDismiss() }
            )

            SheetActionItem(
                icon = Icons.AutoMirrored.Filled.DriveFileMove,
                title = "Move to Folder",
                onClick = { showMoveFolderDialog = true }
            )

            SheetActionItem(
                icon = Icons.AutoMirrored.Filled.Label,
                title = "Edit Tags",
                onClick = { showTagsDialog = true }
            )

            SheetActionItem(
                icon = Icons.Default.ContentCopy,
                title = "Duplicate",
                onClick = { onDuplicate(); onDismiss() }
            )

            SheetActionItem(
                icon = Icons.Default.Share,
                title = "Share",
                onClick = {
                    onDismiss()
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, document.title)
                        putExtra(Intent.EXTRA_TEXT, document.content)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Document"))
                }
            )

            SheetActionItem(
                icon = Icons.Default.PictureAsPdf,
                title = "Print / Export PDF",
                onClick = {
                    onDismiss()
                    PrintHelper.printDocument(context, document.title, document.content)
                }
            )

            SheetActionItem(
                icon = Icons.Default.Info,
                title = "Document Details",
                onClick = { onDismiss(); onShowDetails() }
            )

            HorizontalDivider(color = colors.divider, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            SheetActionItem(
                icon = Icons.Default.Delete,
                title = "Move to Trash",
                tint = Color(0xFFEF4444),
                onClick = { onDelete(); onDismiss() }
            )
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRename(renameText.trim())
                        }
                        showRenameDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Move to Folder Dialog
    if (showMoveFolderDialog) {
        AlertDialog(
            onDismissRequest = { showMoveFolderDialog = false },
            title = { Text("Move to Folder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = {
                            onMoveToFolder(null)
                            showMoveFolderDialog = false
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (document.folderId == null) colors.accent.copy(alpha = 0.12f) else colors.secondarySurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Root (No Folder)",
                            modifier = Modifier.padding(12.dp),
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    folders.forEach { f ->
                        Surface(
                            onClick = {
                                onMoveToFolder(f.id)
                                showMoveFolderDialog = false
                                onDismiss()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (document.folderId == f.id) colors.accent.copy(alpha = 0.12f) else colors.secondarySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = f.name,
                                modifier = Modifier.padding(12.dp),
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMoveFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Tags Dialog
    if (showTagsDialog) {
        AlertDialog(
            onDismissRequest = { showTagsDialog = false },
            title = { Text("Manage Tags") },
            text = {
                Column {
                    Text(
                        text = "Separate tags with commas (e.g. Ideas, Work, Draft):",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text("Tags") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onManageTags(tagsInput.trim())
                        showTagsDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Save Tags")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SheetActionItem(
    icon: ImageVector,
    title: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val colors = LocalEditorColors.current
    val itemTint = tint ?: colors.textPrimary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = itemTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = itemTint
            )
        }
    }
}

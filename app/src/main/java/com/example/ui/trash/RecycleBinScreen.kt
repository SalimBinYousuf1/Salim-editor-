package com.example.ui.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
fun RecycleBinScreen(
    viewModel: RecycleBinViewModel,
    onBack: () -> Unit
) {
    val documents by viewModel.trashedDocuments.collectAsState()
    val trashedFiles by viewModel.trashedFiles.collectAsState()
    val colors = LocalEditorColors.current
    var showEmptyConfirm by remember { mutableStateOf(false) }

    val totalCount = documents.size + trashedFiles.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Recycle Bin",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (totalCount > 0) {
                TextButton(onClick = { viewModel.restoreAll() }) {
                    Text("Restore All", color = colors.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { showEmptyConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Empty Trash",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        if (totalCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = colors.secondarySurface,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Recycle Bin is Empty",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Deleted files and documents are safely stored here and can be restored anytime.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (trashedFiles.isNotEmpty()) {
                    item {
                        Text(
                            text = "DEVICE FILES (${trashedFiles.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(trashedFiles, key = { it.file.absolutePath }) { item ->
                        TrashedFileItem(
                            item = item,
                            onRestore = { viewModel.restoreFile(item.file) },
                            onDeletePermanent = { viewModel.deletePermanentlyFile(item.file) }
                        )
                    }
                }

                if (documents.isNotEmpty()) {
                    item {
                        Text(
                            text = "DOCUMENTS & NOTES (${documents.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(documents, key = { it.id }) { doc ->
                        TrashedDocumentItem(
                            document = doc,
                            onRestore = { viewModel.restoreDocument(doc.id) },
                            onDeletePermanent = { viewModel.deletePermanently(doc.id) }
                        )
                    }
                }
            }
        }
    }

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            title = { Text("Empty Recycle Bin?") },
            text = { Text("All $totalCount deleted item(s) will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete All Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TrashedFileItem(
    item: com.example.data.repository.TrashedFileInfo,
    onRestore: () -> Unit,
    onDeletePermanent: () -> Unit
) {
    val colors = LocalEditorColors.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.secondarySurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (item.isDirectory) Icons.Default.Delete else Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.originalName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${item.formattedSize} • Deleted ${dateFormat.format(Date(item.trashedAt))}",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "Orig: ${item.originalPath}",
                    fontSize = 10.sp,
                    color = colors.textTertiary,
                    maxLines = 1
                )
            }

            IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Restore",
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDeletePermanent, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Forever",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TrashedDocumentItem(
    document: DocumentEntity,
    onRestore: () -> Unit,
    onDeletePermanent: () -> Unit
) {
    val colors = LocalEditorColors.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.secondarySurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = document.extension.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = "Deleted ${document.trashedTimestamp?.let { dateFormat.format(Date(it)) } ?: "recently"}",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Restore",
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDeletePermanent, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Forever",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

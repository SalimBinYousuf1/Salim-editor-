package com.example.ui.files

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FileItem
import com.example.data.repository.FileSortOption
import com.example.ui.components.TextEditorDialog
import com.example.ui.home.ViewMode
import com.example.ui.theme.LocalEditorColors
import com.example.util.FileCategory
import com.example.util.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalEditorColors.current
    val context = LocalContext.current

    // Handle back button inside directories
    BackHandler(enabled = uiState.breadcrumbs.size > 1) {
        viewModel.navigateUp()
    }

    var editingFile by remember { mutableStateOf<File?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header / Multi-Select Bar
            if (uiState.isMultiSelectMode) {
                MultiSelectBar(
                    selectedCount = uiState.selectedPaths.size,
                    totalCount = uiState.files.size,
                    onClose = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll() },
                    onShare = {
                        val files = uiState.files.filter { uiState.selectedPaths.contains(it.path) }.map { it.file }
                        FileUtils.shareMultipleFiles(context, files)
                    },
                    onCopy = { viewModel.copySelected() },
                    onCut = { viewModel.cutSelected() },
                    onDelete = {
                        val files = uiState.files.filter { uiState.selectedPaths.contains(it.path) }.map { it.file }
                        viewModel.openDialog(FileDialogType.DeleteConfirm(files, permanent = false))
                    },
                    onZip = {
                        val files = uiState.files.filter { uiState.selectedPaths.contains(it.path) }.map { it.file }
                        viewModel.openDialog(FileDialogType.ZipDialog(files))
                    }
                )
            } else {
                TopAppBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    viewMode = uiState.viewMode,
                    onToggleViewMode = {
                        val next = when (uiState.viewMode) {
                            ViewMode.LIST -> ViewMode.GRID
                            ViewMode.GRID -> ViewMode.COMPACT
                            ViewMode.COMPACT -> ViewMode.LIST
                        }
                        viewModel.setViewMode(next)
                    },
                    showHidden = uiState.showHiddenFiles,
                    onToggleHidden = { viewModel.toggleHiddenFiles() },
                    onOpenSortMenu = { showSortMenu = true },
                    onRefresh = { viewModel.refresh() }
                )
            }

            // Storage Permission Warning Banner
            if (!uiState.hasStoragePermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                StoragePermissionBanner(
                    onGrant = {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            context.startActivity(fallback)
                        }
                    }
                )
            }

            // Storage Overview Card (shown when at root)
            if (uiState.breadcrumbs.size <= 1 && uiState.searchQuery.isBlank()) {
                StorageOverviewCard(
                    storageInfo = uiState.storageInfo,
                    onDownloads = { viewModel.navigateToDownloads() },
                    onDocuments = { viewModel.navigateToDocuments() },
                    onPictures = { viewModel.navigateToPictures() },
                    onDCIM = { viewModel.navigateToDCIM() },
                    onMusic = { viewModel.navigateToMusic() }
                )
            }

            // Breadcrumbs Navigation Bar
            BreadcrumbsBar(
                breadcrumbs = uiState.breadcrumbs,
                onNavigate = { viewModel.navigateTo(it) }
            )

            // Category Filter Chips
            CategoryFilterChips(
                selected = uiState.selectedCategory,
                onSelect = { viewModel.setCategory(it) }
            )

            // File Content List / Grid
            if (uiState.files.isEmpty()) {
                EmptyStateView(
                    searchQuery = uiState.searchQuery,
                    category = uiState.selectedCategory
                )
            } else {
                when (uiState.viewMode) {
                    ViewMode.LIST -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(uiState.files, key = { it.path }) { item ->
                                FileListItem(
                                    item = item,
                                    isSelected = uiState.selectedPaths.contains(item.path),
                                    isMultiSelect = uiState.isMultiSelectMode,
                                    onTap = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleSelection(item.path)
                                        } else if (item.isDirectory) {
                                            viewModel.navigateTo(item.file)
                                        } else if (FileUtils.isTextOrCode(item.file)) {
                                            editingFile = item.file
                                        } else if (FileUtils.isArchive(item.file)) {
                                            viewModel.extractZip(item.file)
                                        } else {
                                            FileUtils.openWithIntent(context, item.file)
                                        }
                                    },
                                    onLongPress = {
                                        viewModel.toggleSelection(item.path)
                                    },
                                    onOpenWith = { FileUtils.openWithIntent(context, item.file) },
                                    onShare = { FileUtils.shareFile(context, item.file) },
                                    onCopy = { viewModel.copyFile(item.file) },
                                    onCut = { viewModel.cutFile(item.file) },
                                    onRename = { viewModel.openDialog(FileDialogType.Rename(item)) },
                                    onDelete = { viewModel.openDialog(FileDialogType.DeleteConfirm(listOf(item.file), permanent = false)) },
                                    onDetails = { viewModel.openDialog(FileDialogType.Details(item)) },
                                    onExtract = if (FileUtils.isArchive(item.file)) { { viewModel.extractZip(item.file) } } else null
                                )
                            }
                        }
                    }
                    ViewMode.COMPACT -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(uiState.files, key = { it.path }) { item ->
                                FileCompactItem(
                                    item = item,
                                    isSelected = uiState.selectedPaths.contains(item.path),
                                    isMultiSelect = uiState.isMultiSelectMode,
                                    onTap = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleSelection(item.path)
                                        } else if (item.isDirectory) {
                                            viewModel.navigateTo(item.file)
                                        } else if (FileUtils.isTextOrCode(item.file)) {
                                            editingFile = item.file
                                        } else {
                                            FileUtils.openWithIntent(context, item.file)
                                        }
                                    },
                                    onLongPress = { viewModel.toggleSelection(item.path) },
                                    onDelete = { viewModel.openDialog(FileDialogType.DeleteConfirm(listOf(item.file), permanent = false)) }
                                )
                            }
                        }
                    }
                    ViewMode.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 100.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.files, key = { it.path }) { item ->
                                FileGridItem(
                                    item = item,
                                    isSelected = uiState.selectedPaths.contains(item.path),
                                    isMultiSelect = uiState.isMultiSelectMode,
                                    onTap = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleSelection(item.path)
                                        } else if (item.isDirectory) {
                                            viewModel.navigateTo(item.file)
                                        } else if (FileUtils.isTextOrCode(item.file)) {
                                            editingFile = item.file
                                        } else {
                                            FileUtils.openWithIntent(context, item.file)
                                        }
                                    },
                                    onLongPress = { viewModel.toggleSelection(item.path) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Clipboard Paste Bar (when files are in copy or cut mode)
        if (uiState.clipboardMode != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.clipboardMode == ClipboardMode.COPY) Icons.Default.ContentCopy else Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = colors.accent
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${uiState.clipboardFiles.size} items ready to paste",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.cancelClipboard() }) {
                        Text("Cancel", color = colors.textSecondary)
                    }
                    Button(
                        onClick = { viewModel.paste() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paste Here")
                    }
                }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(visible = showFabMenu, enter = fadeIn(), exit = fadeOut()) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        ExtendedFabItem(
                            label = "New Folder",
                            icon = Icons.Default.CreateNewFolder,
                            onClick = {
                                showFabMenu = false
                                viewModel.openDialog(FileDialogType.CreateFolder)
                            }
                        )
                        ExtendedFabItem(
                            label = "New File",
                            icon = Icons.Default.NoteAdd,
                            onClick = {
                                showFabMenu = false
                                viewModel.openDialog(FileDialogType.CreateFile)
                            }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = colors.accent,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        }

        // Sort Menu Dropdown
        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false },
            modifier = Modifier.background(colors.surface)
        ) {
            FileSortOption.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sort.displayName,
                                color = if (uiState.sortOption == sort) colors.accent else colors.textPrimary,
                                fontWeight = if (uiState.sortOption == sort) FontWeight.Bold else FontWeight.Normal
                            )
                            if (uiState.sortOption == sort) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Check, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    onClick = {
                        viewModel.setSortOption(sort)
                        showSortMenu = false
                    }
                )
            }
        }

        // User Message Toast / Banner
        uiState.userMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp, start = 16.dp, end = 16.dp),
                containerColor = colors.surface,
                contentColor = colors.textPrimary,
                action = {
                    TextButton(onClick = { viewModel.clearMessage() }) {
                        Text("OK", color = colors.accent)
                    }
                }
            ) {
                Text(msg)
            }
        }

        // Active Dialogs
        uiState.activeDialog?.let { dialog ->
            when (dialog) {
                is FileDialogType.CreateFolder -> {
                    var folderName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissDialog() },
                        title = { Text("New Folder", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                        text = {
                            OutlinedTextField(
                                value = folderName,
                                onValueChange = { folderName = it },
                                label = { Text("Folder Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.createFolder(folderName) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                            ) {
                                Text("Create")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.dismissDialog() }) {
                                Text("Cancel", color = colors.textSecondary)
                            }
                        },
                        containerColor = colors.surface
                    )
                }
                is FileDialogType.CreateFile -> {
                    var fileName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissDialog() },
                        title = { Text("New File", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                        text = {
                            OutlinedTextField(
                                value = fileName,
                                onValueChange = { fileName = it },
                                label = { Text("File Name (e.g. notes.txt)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.createFile(fileName) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                            ) {
                                Text("Create")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.dismissDialog() }) {
                                Text("Cancel", color = colors.textSecondary)
                            }
                        },
                        containerColor = colors.surface
                    )
                }
                is FileDialogType.Rename -> {
                    var newName by remember { mutableStateOf(dialog.item.name) }
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissDialog() },
                        title = { Text("Rename", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                        text = {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.rename(dialog.item.file, newName) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                            ) {
                                Text("Rename")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.dismissDialog() }) {
                                Text("Cancel", color = colors.textSecondary)
                            }
                        },
                        containerColor = colors.surface
                    )
                }
                is FileDialogType.Details -> {
                    FileDetailsDialog(
                        item = dialog.item,
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                is FileDialogType.DeleteConfirm -> {
                    var permanent by remember { mutableStateOf(dialog.permanent) }
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissDialog() },
                        title = { Text("Delete Item(s)?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text(
                                    text = "Are you sure you want to delete ${dialog.items.size} item(s)?",
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = permanent,
                                        onCheckedChange = { permanent = it }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Delete permanently (bypass Recycle Bin)",
                                        fontSize = 13.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (dialog.items.size == 1) {
                                        viewModel.deleteItem(dialog.items.first(), permanent)
                                    } else {
                                        viewModel.deleteSelected(permanent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.dismissDialog() }) {
                                Text("Cancel", color = colors.textSecondary)
                            }
                        },
                        containerColor = colors.surface
                    )
                }
                is FileDialogType.ZipDialog -> {
                    var zipName by remember { mutableStateOf("Archive_${System.currentTimeMillis() / 1000}") }
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissDialog() },
                        title = { Text("Compress to Zip", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
                        text = {
                            OutlinedTextField(
                                value = zipName,
                                onValueChange = { zipName = it },
                                label = { Text("Archive Name") },
                                singleLine = true,
                                suffix = { Text(".zip") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.zipSelected(zipName) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                            ) {
                                Text("Compress")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.dismissDialog() }) {
                                Text("Cancel", color = colors.textSecondary)
                            }
                        },
                        containerColor = colors.surface
                    )
                }
            }
        }

        // Open Built-in Text Editor Dialog when user opens a text or code file
        editingFile?.let { file ->
            TextEditorDialog(
                file = file,
                onDismiss = { editingFile = null },
                onSaved = { viewModel.refresh() }
            )
        }
    }
}

@Composable
private fun TopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewMode: ViewMode,
    onToggleViewMode: () -> Unit,
    showHidden: Boolean,
    onToggleHidden: () -> Unit,
    onOpenSortMenu: () -> Unit,
    onRefresh: () -> Unit
) {
    val colors = LocalEditorColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Salim Files",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.textSecondary)
                }
                IconButton(onClick = onToggleHidden) {
                    Icon(
                        imageVector = if (showHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Hidden",
                        tint = if (showHidden) colors.accent else colors.textSecondary
                    )
                }
                IconButton(onClick = onOpenSortMenu) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort", tint = colors.textSecondary)
                }
                IconButton(onClick = onToggleViewMode) {
                    val icon = when (viewMode) {
                        ViewMode.LIST -> Icons.Default.GridView
                        ViewMode.GRID -> Icons.Default.ViewList
                        ViewMode.COMPACT -> Icons.Default.ViewHeadline
                    }
                    Icon(icon, contentDescription = "Change View Mode", tint = colors.textSecondary)
                }
            }

            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = colors.surfaceVariant
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(color = colors.textPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Search files & folders...", color = colors.textTertiary, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiSelectBar(
    selectedCount: Int,
    totalCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onDelete: () -> Unit,
    onZip: () -> Unit
) {
    val colors = LocalEditorColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textPrimary)
            }
            Text(
                text = "$selectedCount selected",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = colors.textSecondary)
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = colors.textSecondary)
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = colors.textSecondary)
            }
            IconButton(onClick = onCut) {
                Icon(Icons.Default.ContentCut, contentDescription = "Cut", tint = colors.textSecondary)
            }
            IconButton(onClick = onZip) {
                Icon(Icons.Default.Archive, contentDescription = "Zip", tint = colors.textSecondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StoragePermissionBanner(onGrant: () -> Unit) {
    val colors = LocalEditorColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.accent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = colors.accent)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Full Storage Access", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                Text("Grant permission to manage all files across device storage.", fontSize = 12.sp, color = colors.textSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Grant", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StorageOverviewCard(
    storageInfo: com.example.data.repository.StorageInfo,
    onDownloads: () -> Unit,
    onDocuments: () -> Unit,
    onPictures: () -> Unit,
    onDCIM: () -> Unit,
    onMusic: () -> Unit
) {
    val colors = LocalEditorColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Internal Storage", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${storageInfo.formattedUsed} / ${storageInfo.formattedTotal}",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { storageInfo.usedPercentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colors.accent,
                trackColor = colors.surfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Shortcut Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickFolderChip("Downloads", Icons.Default.Folder, onDownloads)
                QuickFolderChip("Documents", Icons.Default.Description, onDocuments)
                QuickFolderChip("Pictures", Icons.Default.Image, onPictures)
                QuickFolderChip("Camera (DCIM)", Icons.Default.Movie, onDCIM)
                QuickFolderChip("Music", Icons.Default.AudioFile, onMusic)
            }
        }
    }
}

@Composable
private fun QuickFolderChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = LocalEditorColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
        }
    }
}

@Composable
private fun BreadcrumbsBar(
    breadcrumbs: List<File>,
    onNavigate: (File) -> Unit
) {
    val colors = LocalEditorColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, dir ->
            val isLast = index == breadcrumbs.size - 1
            val displayName = if (index == 0) "Storage" else dir.name

            TextButton(
                onClick = { onNavigate(dir) },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = displayName,
                    fontSize = 13.sp,
                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                    color = if (isLast) colors.accent else colors.textSecondary
                )
            }

            if (!isLast) {
                Text("/", color = colors.textTertiary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selected: FileCategory,
    onSelect: (FileCategory) -> Unit
) {
    val colors = LocalEditorColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FileCategory.entries.forEach { cat ->
            val isSelected = selected == cat
            Surface(
                onClick = { onSelect(cat) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) colors.accent else colors.surface,
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, colors.divider) else null
            ) {
                Text(
                    text = cat.displayName,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    item: FileItem,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onOpenWith: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit,
    onExtract: (() -> Unit)? = null
) {
    val colors = LocalEditorColors.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            ),
        color = if (isSelected) colors.accent.copy(alpha = 0.12f) else colors.surface,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, colors.accent) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelect) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onTap() },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            FileIconBadge(item = item)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.formattedSize,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", fontSize = 10.sp, color = colors.textTertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.formattedDate,
                        fontSize = 12.sp,
                        color = colors.textTertiary
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = colors.textSecondary)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    if (!item.isDirectory) {
                        DropdownMenuItem(
                            text = { Text("Open with...", color = colors.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.OpenWith, null, tint = colors.textSecondary) },
                            onClick = { showMenu = false; onOpenWith() }
                        )
                        DropdownMenuItem(
                            text = { Text("Share", color = colors.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.Share, null, tint = colors.textSecondary) },
                            onClick = { showMenu = false; onShare() }
                        )
                    }
                    if (onExtract != null) {
                        DropdownMenuItem(
                            text = { Text("Extract Zip", color = colors.accent) },
                            leadingIcon = { Icon(Icons.Default.Unarchive, null, tint = colors.accent) },
                            onClick = { showMenu = false; onExtract() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Copy", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = colors.textSecondary) },
                        onClick = { showMenu = false; onCopy() }
                    )
                    DropdownMenuItem(
                        text = { Text("Cut", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.ContentCut, null, tint = colors.textSecondary) },
                        onClick = { showMenu = false; onCut() }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = colors.textSecondary) },
                        onClick = { showMenu = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text("Details", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Info, null, tint = colors.textSecondary) },
                        onClick = { showMenu = false; onDetails() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileCompactItem(
    item: FileItem,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalEditorColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .combinedClickable(onClick = onTap, onLongClick = onLongPress),
        color = if (isSelected) colors.accent.copy(alpha = 0.12f) else colors.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileIconBadge(item = item, compact = true)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.name,
                fontSize = 13.sp,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(item.formattedSize, fontSize = 11.sp, color = colors.textSecondary)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGridItem(
    item: FileItem,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val colors = LocalEditorColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onTap, onLongClick = onLongPress),
        color = if (isSelected) colors.accent.copy(alpha = 0.12f) else colors.surface,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, colors.accent) else androidx.compose.foundation.BorderStroke(1.dp, colors.divider.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FileIconBadge(item = item, size = 44.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.formattedSize,
                fontSize = 10.sp,
                color = colors.textTertiary
            )
        }
    }
}

@Composable
private fun FileIconBadge(item: FileItem, compact: Boolean = false, size: androidx.compose.ui.unit.Dp = if (compact) 28.dp else 40.dp) {
    val (icon, tint, bg) = when {
        item.isDirectory -> Triple(Icons.Default.Folder, Color(0xFFF59E0B), Color(0xFFFEF3C7))
        item.category == FileCategory.IMAGE -> Triple(Icons.Default.Image, Color(0xFF8B5CF6), Color(0xFFEDE9FE))
        item.category == FileCategory.VIDEO -> Triple(Icons.Default.Movie, Color(0xFFEF4444), Color(0xFFFEE2E2))
        item.category == FileCategory.AUDIO -> Triple(Icons.Default.AudioFile, Color(0xFFF97316), Color(0xFFFFEDD5))
        item.category == FileCategory.APK -> Triple(Icons.Default.Android, Color(0xFF10B981), Color(0xFFD1FAE5))
        item.category == FileCategory.ARCHIVE -> Triple(Icons.Default.Archive, Color(0xFF78350F), Color(0xFFFEF3C7))
        FileUtils.isTextOrCode(item.file) -> Triple(Icons.Default.Description, Color(0xFF2563EB), Color(0xFFDBEAFE))
        else -> Triple(Icons.AutoMirrored.Filled.InsertDriveFile, Color(0xFF64748B), Color(0xFFF1F5F9))
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
private fun ExtendedFabItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = LocalEditorColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
        }
    }
}

@Composable
private fun EmptyStateView(searchQuery: String, category: FileCategory) {
    val colors = LocalEditorColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (searchQuery.isNotBlank()) "No files match \"$searchQuery\"" else "This folder is empty",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (category != FileCategory.ALL) "No ${category.displayName} in this directory" else "Tap + to create a new folder or file",
            fontSize = 13.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun FileDetailsDialog(item: FileItem, onDismiss: () -> Unit) {
    val colors = LocalEditorColors.current
    val md5Hash = remember(item.path) {
        if (!item.isDirectory) FileUtils.calculateHash(item.file, "MD5") else "Directory"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Item Details", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Name", item.name, colors)
                DetailRow("Path", item.path, colors)
                DetailRow("Type", if (item.isDirectory) "Folder" else FileUtils.getMimeType(item.file), colors)
                DetailRow("Size", item.formattedSize, colors)
                DetailRow("Last Modified", item.formattedDate, colors)
                DetailRow("Readable", if (item.file.canRead()) "Yes" else "No", colors)
                DetailRow("Writable", if (item.file.canWrite()) "Yes" else "No", colors)
                if (!item.isDirectory) {
                    DetailRow("MD5 Hash", md5Hash, colors)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Close")
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun DetailRow(label: String, value: String, colors: com.example.ui.theme.SalimCustomColors) {
    Column {
        Text(label, fontSize = 11.sp, color = colors.textTertiary, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Normal)
        Spacer(modifier = Modifier.height(2.dp))
    }
}

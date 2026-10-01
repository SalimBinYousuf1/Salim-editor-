package com.example.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.FolderEntity
import com.example.ui.editor.components.DocumentStatsModal
import com.example.ui.theme.LocalEditorColors
import com.example.ui.theme.LocalEditorSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDocument: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTrash: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalEditorColors.current
    val settings = LocalEditorSettings.current
    val context = LocalContext.current

    var contextMenuDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var detailsDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showNewDocDialog by remember { mutableStateOf(false) }
    var newDocTitle by remember { mutableStateOf("") }
    var newDocExtension by remember { mutableStateOf("md") }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importFromUri(uri) { id ->
                onOpenDocument(id)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // Top Section: Header & Action Icons
        if (uiState.isMultiSelectMode) {
            // Multi-Select Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(colors.surface)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.clearMultiSelect() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Multi-Select", tint = colors.textPrimary)
                }
                Text(
                    text = "${uiState.selectedDocIds.size} selected",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.selectAll() }) {
                    Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = colors.textPrimary)
                }
                IconButton(onClick = { viewModel.batchPin() }) {
                    Icon(Icons.Default.PushPin, contentDescription = "Pin Selected", tint = colors.textPrimary)
                }
                IconButton(onClick = { viewModel.batchFavorite() }) {
                    Icon(Icons.Default.Star, contentDescription = "Favorite Selected", tint = colors.textPrimary)
                }
                IconButton(onClick = { viewModel.batchMoveToTrash() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Trash Selected", tint = Color(0xFFEF4444))
                }
            }
        } else {
            // Standard Header (Apple & Vivo hierarchy)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Documents",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${uiState.totalDocumentCount} notes · ${uiState.totalWordCount} words written",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // View Mode Switcher
                    IconButton(
                        onClick = {
                            val next = when (uiState.viewMode) {
                                ViewMode.LIST -> ViewMode.GRID
                                ViewMode.GRID -> ViewMode.COMPACT
                                ViewMode.COMPACT -> ViewMode.LIST
                            }
                            viewModel.setViewMode(next)
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        val icon = when (uiState.viewMode) {
                            ViewMode.LIST -> Icons.Default.ViewAgenda
                            ViewMode.GRID -> Icons.Default.GridView
                            ViewMode.COMPACT -> Icons.Default.ViewHeadline
                        }
                        Icon(icon, contentDescription = "View Mode", tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                    }

                    // Sort & Filter
                    IconButton(
                        onClick = { showSortSheet = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                    }

                    // Trash
                    IconButton(
                        onClick = onNavigateToTrash,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Trash", tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                    }

                    // Settings
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(38.dp).testTag("nav_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Search Bar (Apple-smooth pill)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.secondarySurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (uiState.searchQuery.isEmpty()) {
                        Text("Search notes, code, tags, text…", color = colors.textTertiary, fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.textPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth().testTag("home_search_input")
                    )
                }
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.onSearchQueryChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DocumentFilter.values().forEach { filter ->
                val active = uiState.activeFilter == filter && uiState.selectedFolderId == null
                Surface(
                    onClick = { viewModel.setFilter(filter) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (active) colors.accent else colors.secondarySurface,
                    modifier = Modifier.height(30.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = filter.label,
                            color = if (active) Color.White else colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            // Folders pills
            uiState.folders.forEach { f ->
                val active = uiState.selectedFolderId == f.id
                Surface(
                    onClick = { viewModel.selectFolder(if (active) null else f.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (active) colors.accent else colors.secondarySurface,
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (active) Color.White else colors.accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = f.name,
                            color = if (active) Color.White else colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Document List / Grid
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.accent)
                }
            } else if (uiState.documents.isEmpty()) {
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
                                Icon(Icons.Default.EditNote, contentDescription = null, tint = colors.accent, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) "No matching documents" else "No documents in this view",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button below to create your first document or open an external file.",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                when (uiState.viewMode) {
                    ViewMode.LIST -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.documents, key = { it.id }) { doc ->
                                DocumentListItem(
                                    document = doc,
                                    isSelected = uiState.selectedDocIds.contains(doc.id),
                                    isMultiSelect = uiState.isMultiSelectMode,
                                    onClick = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleMultiSelect(doc.id)
                                        } else {
                                            onOpenDocument(doc.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleMultiSelect(doc.id)
                                        } else {
                                            contextMenuDoc = doc
                                        }
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }

                    ViewMode.COMPACT -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.documents, key = { it.id }) { doc ->
                                DocumentCompactItem(
                                    document = doc,
                                    isSelected = uiState.selectedDocIds.contains(doc.id),
                                    isMultiSelect = uiState.isMultiSelectMode,
                                    onClick = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleMultiSelect(doc.id)
                                        } else {
                                            onOpenDocument(doc.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleMultiSelect(doc.id)
                                        } else {
                                            contextMenuDoc = doc
                                        }
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }

                    ViewMode.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.documents, key = { it.id }) { doc ->
                                DocumentGridCard(
                                    document = doc,
                                    isSelected = uiState.selectedDocIds.contains(doc.id),
                                    isMultiSelect = uiState.isMultiSelectMode,
                                    onClick = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleMultiSelect(doc.id)
                                        } else {
                                            onOpenDocument(doc.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleMultiSelect(doc.id)
                                        } else {
                                            contextMenuDoc = doc
                                        }
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }

            // Bottom Thumb Reach Actions Bar (vivo principle: one-handed reachable!)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surface.copy(alpha = 0.94f),
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick Note Button
                    Surface(
                        onClick = {
                            viewModel.createQuickNote { id ->
                                onOpenDocument(id)
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = colors.secondarySurface,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quick Note", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                        }
                    }

                    // Open File (SAF)
                    Surface(
                        onClick = { openDocumentLauncher.launch(arrayOf("*/*")) },
                        shape = RoundedCornerShape(18.dp),
                        color = colors.secondarySurface,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FileOpen, contentDescription = "Open File", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Large New Document FAB
                    Surface(
                        onClick = { showNewDocDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = colors.accent,
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("home_new_doc_fab")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Context Menu Bottom Sheet
    contextMenuDoc?.let { doc ->
        DocumentContextMenuSheet(
            document = doc,
            folders = uiState.folders,
            onOpen = { onOpenDocument(doc.id) },
            onRename = { viewModel.renameDocument(doc.id, it) },
            onMoveToFolder = { viewModel.setDocumentFolder(doc.id, it) },
            onDuplicate = { viewModel.duplicate(doc) },
            onTogglePin = { viewModel.togglePin(doc.id) },
            onToggleFavorite = { viewModel.toggleFavorite(doc.id) },
            onManageTags = { viewModel.setDocumentTags(doc.id, it) },
            onDelete = { viewModel.moveToTrash(doc.id) },
            onShowDetails = { detailsDoc = doc },
            onDismiss = { contextMenuDoc = null },
            context = context
        )
    }

    // Details Modal
    detailsDoc?.let { doc ->
        DocumentStatsModal(
            document = doc,
            currentContent = doc.content,
            onDismiss = { detailsDoc = null }
        )
    }

    // Sort Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor = colors.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                Text("Sort Documents By", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                SortOption.values().forEach { option ->
                    val selected = uiState.sortOption == option
                    Surface(
                        onClick = {
                            viewModel.setSortOption(option)
                            showSortSheet = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) colors.accent.copy(alpha = 0.12f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option.label,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) colors.accent else colors.textPrimary
                            )
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // New Document Dialog
    if (showNewDocDialog) {
        AlertDialog(
            onDismissRequest = { showNewDocDialog = false },
            title = { Text("New Document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newDocTitle,
                        onValueChange = { newDocTitle = it },
                        label = { Text("Document Title") },
                        placeholder = { Text("e.g. Project Notes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_doc_title_input")
                    )

                    Text("Format / Extension:", fontSize = 12.sp, color = colors.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("md", "txt", "kt", "py", "json", "html").forEach { ext ->
                            val sel = newDocExtension == ext
                            Surface(
                                onClick = { newDocExtension = ext },
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) colors.accent else colors.secondarySurface,
                                modifier = Modifier.height(32.dp)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = ".$ext",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (sel) Color.White else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = newDocTitle.ifBlank { "Untitled" }
                        val syntax = when (newDocExtension) {
                            "md" -> "markdown"
                            "txt" -> "plaintext"
                            else -> "code"
                        }
                        showNewDocDialog = false
                        viewModel.createNewDocument(title, newDocExtension, syntax) { id ->
                            onOpenDocument(id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.testTag("confirm_create_doc_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDocDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DocumentListItem(
    document: DocumentEntity,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = LocalEditorColors.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) colors.accent else colors.cardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("doc_item_${document.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelect) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (isSelected) colors.accent else colors.textTertiary,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 6.dp)
                )
            }

            // Extension badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.accent.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (document.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = colors.accent, modifier = Modifier.size(14.dp))
                    }
                    if (document.isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Star, contentDescription = "Favorite", tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                    }
                }

                if (document.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = document.content.replace('\n', ' ').trim(),
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dateFormat.format(Date(document.updatedAt)),
                        fontSize = 11.sp,
                        color = colors.textTertiary
                    )
                    Text(
                        text = "·",
                        fontSize = 11.sp,
                        color = colors.textTertiary
                    )
                    Text(
                        text = "${document.wordCount} words",
                        fontSize = 11.sp,
                        color = colors.textTertiary
                    )
                    if (document.tags.isNotBlank()) {
                        document.tags.split(",").take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.secondarySurface,
                                modifier = Modifier.height(18.dp)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                    Text(tag.trim(), fontSize = 10.sp, color = colors.textSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DocumentCompactItem(
    document: DocumentEntity,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = LocalEditorColors.current
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) colors.accent else colors.cardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ".${document.extension}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
                modifier = Modifier.width(36.dp)
            )
            Text(
                text = document.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (document.isPinned) {
                Icon(Icons.Default.PushPin, contentDescription = null, tint = colors.accent, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = dateFormat.format(Date(document.updatedAt)),
                fontSize = 11.sp,
                color = colors.textTertiary
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DocumentGridCard(
    document: DocumentEntity,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = LocalEditorColors.current
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) colors.accent else colors.cardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = document.extension.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                    Row {
                        if (document.isPinned) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = colors.accent, modifier = Modifier.size(12.dp))
                        }
                        if (document.isFavorite) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = document.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = document.content.replace('\n', ' ').trim(),
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${document.wordCount}w",
                    fontSize = 10.sp,
                    color = colors.textTertiary
                )
                Text(
                    text = dateFormat.format(Date(document.updatedAt)),
                    fontSize = 10.sp,
                    color = colors.textTertiary
                )
            }
        }
    }
}

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
    context: android.content.Context
) {
    val colors = LocalEditorColors.current
    var showRenameDialog by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf(document.title) }
    var newTags by remember { mutableStateOf(document.tags) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${document.title}.${document.extension}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            DropdownMenuItem(
                text = { Text(if (document.isPinned) "Unpin" else "Pin to Top", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = colors.accent) },
                onClick = { onTogglePin(); onDismiss() }
            )
            DropdownMenuItem(
                text = { Text(if (document.isFavorite) "Remove from Favorites" else "Add to Favorites", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B)) },
                onClick = { onToggleFavorite(); onDismiss() }
            )
            DropdownMenuItem(
                text = { Text("Rename", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null, tint = colors.textSecondary) },
                onClick = { showRenameDialog = true }
            )
            DropdownMenuItem(
                text = { Text("Duplicate", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, tint = colors.textSecondary) },
                onClick = { onDuplicate(); onDismiss() }
            )
            DropdownMenuItem(
                text = { Text("Details & Stats", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
                onClick = { onShowDetails(); onDismiss() }
            )
            DropdownMenuItem(
                text = { Text("Delete (Move to Trash)", color = Color(0xFFEF4444)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                onClick = { onDelete(); onDismiss() }
            )
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Document", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRename(newTitle)
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
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface
        )
    }
}


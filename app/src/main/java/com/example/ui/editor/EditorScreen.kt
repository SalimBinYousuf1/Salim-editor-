package com.example.ui.editor

import android.content.Intent
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.EditorFont
import com.example.data.repository.EditorMargin
import com.example.ui.editor.components.DocumentOutlineSheet
import com.example.ui.editor.components.DocumentStatsModal
import com.example.ui.editor.components.DynamicEditorToolbar
import com.example.ui.editor.components.FindAndReplaceBar
import com.example.ui.editor.components.KeyboardAccessoryBar
import com.example.ui.editor.components.MarkdownPreviewView
import com.example.ui.editor.components.ToolbarMode
import com.example.ui.editor.components.VersionHistoryModal
import com.example.ui.theme.LocalEditorColors
import com.example.ui.theme.LocalEditorSettings
import com.example.ui.theme.getFontFamily
import com.example.util.PrintHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    documentId: Long,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val versions by viewModel.versionsFlow.collectAsState()
    val colors = LocalEditorColors.current
    val settings = LocalEditorSettings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    BackHandler {
        viewModel.saveDocument(manualSnapshot = false)
        onBack()
    }

    val computedFontSize = (settings.fontSize + uiState.fontSizeZoomDelta).coerceIn(12f, 32f)
    val textStyle = TextStyle(
        fontFamily = getFontFamily(settings.font),
        fontSize = computedFontSize.sp,
        lineHeight = (computedFontSize * settings.lineHeightMultiplier).sp,
        color = colors.textPrimary
    )

    // Cursor position metrics: Line and Column
    val cursor = uiState.contentValue.selection.start
    val textBeforeCursor = uiState.contentValue.text.take(cursor)
    val currentLine = textBeforeCursor.count { it == '\n' } + 1
    val currentCol = cursor - (textBeforeCursor.lastIndexOf('\n').takeIf { it != -1 }?.plus(1) ?: 0) + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.editorCanvas)
            .statusBarsPadding()
            .imePadding()
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    val ctrl = keyEvent.isCtrlPressed
                    val shift = keyEvent.isShiftPressed
                    when (keyEvent.key.keyCode) {
                        KeyEvent.KEYCODE_S.toLong() -> if (ctrl) { viewModel.saveDocument(manualSnapshot = true); true } else false
                        KeyEvent.KEYCODE_Z.toLong() -> if (ctrl) {
                            if (shift) viewModel.redo() else viewModel.undo()
                            true
                        } else false
                        KeyEvent.KEYCODE_Y.toLong() -> if (ctrl) { viewModel.redo(); true } else false
                        KeyEvent.KEYCODE_F.toLong() -> if (ctrl) { viewModel.openFind(); true } else false
                        KeyEvent.KEYCODE_B.toLong() -> if (ctrl) { viewModel.wrapSelection("**", "**"); true } else false
                        KeyEvent.KEYCODE_I.toLong() -> if (ctrl) { viewModel.wrapSelection("*", "*"); true } else false
                        else -> false
                    }
                } else false
            }
    ) {
        // Minimalist Top Bar (Apple simplicity)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(colors.surface.copy(alpha = 0.95f))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.saveDocument(manualSnapshot = false)
                    onBack()
                },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("editor_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Inline Editable Title
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = getFontFamily(settings.font),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_title_input")
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Saved / Edited status indicator pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.isDirty) colors.secondarySurface else colors.accent.copy(alpha = 0.12f),
                modifier = Modifier.height(24.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.isSaving) "Saving…" else if (uiState.isDirty) "Edited" else "Saved",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (uiState.isDirty) colors.textSecondary else colors.accent
                    )
                }
            }

            // Outline button
            IconButton(
                onClick = { viewModel.setShowOutline(true) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ViewHeadline,
                    contentDescription = "Outline",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Split View button (Samsung multitasking)
            IconButton(
                onClick = { viewModel.toggleSplitMode() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerticalSplit,
                    contentDescription = "Split View",
                    tint = if (uiState.splitMode != SplitMode.NONE) colors.accent else colors.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

            // More Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Find & Replace") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        onClick = { showMenu = false; viewModel.openFind() }
                    )
                    DropdownMenuItem(
                        text = { Text("Document Details & Stats") },
                        leadingIcon = { Icon(Icons.Default.BarChart, null) },
                        onClick = { showMenu = false; viewModel.setShowStats(true) }
                    )
                    DropdownMenuItem(
                        text = { Text("Version History & Diff") },
                        leadingIcon = { Icon(Icons.Default.History, null) },
                        onClick = { showMenu = false; viewModel.setShowVersionHistory(true) }
                    )
                    DropdownMenuItem(
                        text = { Text("Save Checkpoint") },
                        leadingIcon = { Icon(Icons.Default.Check, null) },
                        onClick = {
                            showMenu = false
                            viewModel.saveDocument(manualSnapshot = true, snapshotNote = "Manual Checkpoint")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share Text") },
                        leadingIcon = { Icon(Icons.Default.Share, null) },
                        onClick = {
                            showMenu = false
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, uiState.title)
                                putExtra(Intent.EXTRA_TEXT, uiState.contentValue.text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Document"))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Print / Save as PDF") },
                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) },
                        onClick = {
                            showMenu = false
                            PrintHelper.printDocument(context, uiState.title, uiState.contentValue.text)
                        }
                    )
                }
            }
        }

        // Find & Replace Bar (collapsible)
        AnimatedVisibility(
            visible = uiState.isFindOpen,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            FindAndReplaceBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                replaceQuery = uiState.replaceQuery,
                onReplaceQueryChange = { viewModel.onReplaceQueryChange(it) },
                currentMatchIndex = uiState.currentMatchIndex,
                totalMatches = uiState.matchRanges.size,
                matchCase = uiState.matchCase,
                onToggleMatchCase = { viewModel.toggleMatchCase() },
                onFindPrevious = { viewModel.prevMatch() },
                onFindNext = { viewModel.nextMatch() },
                onReplaceOne = { viewModel.replaceOne() },
                onReplaceAll = { viewModel.replaceAll() },
                onClose = { viewModel.closeFind() }
            )
        }

        // Center: Document Canvas (with Samsung split support)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (uiState.splitMode) {
                SplitMode.NONE -> {
                    // Full Screen Editor
                    SingleEditorCanvas(
                        contentValue = uiState.contentValue,
                        onContentChange = { viewModel.onContentChange(it) },
                        textStyle = textStyle,
                        settings = settings,
                        colors = colors,
                        onZoomGesture = { delta ->
                            if (settings.gesturePinchZoom) {
                                viewModel.setZoomDelta(delta)
                            }
                        }
                    )
                }

                SplitMode.SIDE_BY_SIDE_PREVIEW -> {
                    // Side-by-Side Split: Editor on left, Preview on right
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            SingleEditorCanvas(
                                contentValue = uiState.contentValue,
                                onContentChange = { viewModel.onContentChange(it) },
                                textStyle = textStyle,
                                settings = settings,
                                colors = colors,
                                onZoomGesture = { delta ->
                                    if (settings.gesturePinchZoom) viewModel.setZoomDelta(delta)
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(colors.divider)
                        )
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            MarkdownPreviewView(markdownContent = uiState.contentValue.text)
                        }
                    }
                }

                SplitMode.STACKED_PREVIEW -> {
                    // Stacked Split: Editor on top, Preview on bottom
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            SingleEditorCanvas(
                                contentValue = uiState.contentValue,
                                onContentChange = { viewModel.onContentChange(it) },
                                textStyle = textStyle,
                                settings = settings,
                                colors = colors,
                                onZoomGesture = { delta ->
                                    if (settings.gesturePinchZoom) viewModel.setZoomDelta(delta)
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(colors.divider)
                        )
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            MarkdownPreviewView(markdownContent = uiState.contentValue.text)
                        }
                    }
                }
            }

            // Minimalist Cursor / Status Pill (floating bottom right unobtrusively)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surface.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ln $currentLine, Col $currentCol · ${uiState.contentValue.text.split(Regex("\\s+")).count { it.isNotBlank() }}w",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Dynamic Context-Aware Toolbar
        DynamicEditorToolbar(
            mode = uiState.toolbarMode,
            onModeChange = { viewModel.setToolbarMode(it) },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onFindClick = { viewModel.openFind() },
            onOutlineClick = { viewModel.setShowOutline(true) },
            onMoreClick = { showMenu = true },
            onCut = {
                // Handled natively by clipboard or selection
                val sel = uiState.contentValue.selection
                if (!sel.collapsed) {
                    val clip = android.content.ClipData.newPlainText("text", uiState.contentValue.text.substring(sel.min, sel.max))
                    (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                    viewModel.insertText("")
                }
            },
            onCopy = {
                val sel = uiState.contentValue.selection
                if (!sel.collapsed) {
                    val clip = android.content.ClipData.newPlainText("text", uiState.contentValue.text.substring(sel.min, sel.max))
                    (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                }
            },
            onPaste = {
                val clipManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipManager.primaryClip?.getItemAt(0)?.text?.let {
                    viewModel.insertText(it.toString())
                }
            },
            onShareSelection = {
                val sel = uiState.contentValue.selection
                val selectedText = if (!sel.collapsed) uiState.contentValue.text.substring(sel.min, sel.max) else uiState.contentValue.text
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, selectedText)
                }
                context.startActivity(Intent.createChooser(intent, "Share Selection"))
            },
            onBold = { viewModel.wrapSelection("**", "**") },
            onItalic = { viewModel.wrapSelection("*", "*") },
            onUnderline = { viewModel.wrapSelection("<u>", "</u>") },
            onHeading = { level -> viewModel.toggleHeading(level) },
            onBulletList = { viewModel.toggleLinePrefix("- ") },
            onNumberedList = { viewModel.toggleLinePrefix("1. ") },
            onChecklist = { viewModel.toggleLinePrefix("- [ ] ") },
            onQuote = { viewModel.toggleLinePrefix("> ") },
            onCodeBlock = { viewModel.wrapSelection("```\n", "\n```") },
            onLink = { viewModel.wrapSelection("[", "](url)") },
            onIndent = { viewModel.indent() },
            onDedent = { viewModel.dedent() },
            onComment = { viewModel.toggleComment() },
            onInsertBrackets = { open, close -> viewModel.wrapSelection(open, close) },
            canUndo = uiState.canUndo,
            canRedo = uiState.canRedo
        )

        // Keyboard Accessory Strip (above keyboard)
        if (settings.quickAccessoryBar) {
            KeyboardAccessoryBar(
                onInsertText = { viewModel.insertText(it) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onMoveCursor = { viewModel.moveCursor(it) },
                onMoveLineStart = { viewModel.moveLineStart() },
                onMoveLineEnd = { viewModel.moveLineEnd() },
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo
            )
        }
    }

    // Outline Bottom Sheet
    if (uiState.showOutline) {
        DocumentOutlineSheet(
            outline = uiState.outline,
            onSelectHeading = { lineIdx ->
                // Jump cursor to line
                val lines = uiState.contentValue.text.lines()
                var charCount = 0
                for (i in 0 until lineIdx.coerceAtMost(lines.size)) {
                    charCount += lines[i].length + 1
                }
                viewModel.onContentChange(
                    uiState.contentValue.copy(
                        selection = androidx.compose.ui.text.TextRange(charCount.coerceIn(0, uiState.contentValue.text.length))
                    )
                )
            },
            onDismiss = { viewModel.setShowOutline(false) }
        )
    }

    // Stats Modal
    if (uiState.showStats && uiState.document != null) {
        DocumentStatsModal(
            document = uiState.document!!,
            currentContent = uiState.contentValue.text,
            onDismiss = { viewModel.setShowStats(false) }
        )
    }

    // Version History Modal
    if (uiState.showVersionHistory) {
        VersionHistoryModal(
            versions = versions,
            currentContent = uiState.contentValue.text,
            onComputeDiff = { old, new -> viewModel.computeDiff(old, new) },
            onRestoreVersion = { viewModel.restoreVersion(it) },
            onDismiss = { viewModel.setShowVersionHistory(false) }
        )
    }
}

@Composable
fun SingleEditorCanvas(
    contentValue: androidx.compose.ui.text.input.TextFieldValue,
    onContentChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    textStyle: TextStyle,
    settings: com.example.data.repository.EditorSettings,
    colors: com.example.ui.theme.EditorColors,
    onZoomGesture: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = when (settings.margin) {
        EditorMargin.COMPACT -> 12.dp
        EditorMargin.COMFORTABLE -> 20.dp
        EditorMargin.FOCUS_READING -> 28.dp
    }

    val maxWidth = if (settings.margin == EditorMargin.FOCUS_READING) 720.dp else androidx.compose.ui.unit.Dp.Unspecified

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom > 1.05f) onZoomGesture(1f)
                    else if (zoom < 0.95f) onZoomGesture(-1f)
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = maxWidth)
                .padding(horizontal = horizontalPadding, vertical = 12.dp)
        ) {
            // Line numbers gutter (optional)
            if (settings.showLineNumbers) {
                val lines = contentValue.text.lines()
                Column(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .width(28.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lines.size.coerceAtMost(300)) {
                        Text(
                            text = "$i",
                            fontSize = (textStyle.fontSize.value * 0.85).sp,
                            fontFamily = FontFamily.Monospace,
                            color = colors.lineNumbers,
                            lineHeight = textStyle.lineHeight
                        )
                    }
                }
            }

            BasicTextField(
                value = contentValue,
                onValueChange = onContentChange,
                textStyle = textStyle,
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("editor_text_canvas")
            )
        }
    }
}

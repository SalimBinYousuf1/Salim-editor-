package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.automirrored.outlined.WrapText
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WrapText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.editor.EditorColorScheme
import com.example.ui.editor.EditorHistoryManager
import com.example.ui.editor.EditorLanguage
import com.example.ui.editor.EditorQuickBar
import com.example.ui.editor.EditorSettingsDialog
import com.example.ui.editor.EditorThemes
import com.example.ui.editor.GoToLineDialog
import com.example.ui.editor.LanguageSelectorDialog
import com.example.ui.editor.MarkdownPreview
import com.example.ui.editor.SyntaxHighlighterVisualTransformation
import com.example.ui.editor.ThemeSelectorDialog
import com.example.ui.editor.UnsavedChangesDialog
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimGreen
import com.example.ui.theme.SalimRed
import com.example.ui.theme.SalimThemeColors
import com.example.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun TextEditorDialog(
    file: File,
    onDismiss: () -> Unit,
    onSaved: ((File) -> Unit)? = null,
    readOnly: Boolean = false
) {
    val context = LocalContext.current
    val customColors = SalimThemeColors
    val scope = rememberCoroutineScope()

    // State: Editor Content & History
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var originalContent by remember { mutableStateOf("") }
    val history = remember { EditorHistoryManager() }

    // Loading & Saving states
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isSaveSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Preferences & Config
    var editorTheme by remember {
        mutableStateOf(
            if (customColors.isDark) EditorThemes.OneDarkPro else EditorThemes.GitHubLight
        )
    }
    var detectedLanguage by remember {
        mutableStateOf(EditorLanguage.fromFileName(file.name))
    }
    var fontSize by remember { mutableFloatStateOf(13f) }
    var wordWrap by remember { mutableStateOf(false) }
    var showLineNumbers by remember { mutableStateOf(true) }
    var tabSpaces by remember { mutableIntStateOf(4) }
    var isPreviewMode by remember { mutableStateOf(false) }

    // Search & Replace state
    var showSearch by remember { mutableStateOf(false) }
    var showReplace by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var activeMatchIndex by remember { mutableIntStateOf(0) }

    // Dialog sheets
    var showGoToLine by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val isModified = textFieldValue.text != originalContent
    val isMarkdownFile = remember(file.name) {
        val ext = FileUtils.getExtension(file.name)
        ext == "md" || ext == "markdown"
    }

    // Load file content
    fun loadFileContent() {
        isLoading = true
        errorMessage = null
        scope.launch(Dispatchers.IO) {
            try {
                val text = if (file.length() > 6 * 1024 * 1024) {
                    file.bufferedReader().useLines { lines ->
                        lines.take(4000).joinToString("\n") + "\n\n--- [File exceeds 6MB: Displaying first 4,000 lines] ---"
                    }
                } else {
                    file.readText()
                }
                withContext(Dispatchers.Main) {
                    textFieldValue = TextFieldValue(text, TextRange(0))
                    originalContent = text
                    history.pushInitial(textFieldValue)
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to read file"
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(file.absolutePath) {
        loadFileContent()
    }

    // Save action
    fun saveFile(onSuccessClose: Boolean = false) {
        if (!file.canWrite() && file.exists()) {
            Toast.makeText(context, "File is read-only", Toast.LENGTH_SHORT).show()
            return
        }
        isSaving = true
        scope.launch(Dispatchers.IO) {
            try {
                file.writeText(textFieldValue.text)
                withContext(Dispatchers.Main) {
                    originalContent = textFieldValue.text
                    isSaving = false
                    isSaveSuccess = true
                    onSaved?.invoke(file)
                }
                delay(1200)
                withContext(Dispatchers.Main) {
                    isSaveSuccess = false
                    if (onSuccessClose) {
                        onDismiss()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSaving = false
                    Toast.makeText(context, "Error saving: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Handle close request safely
    fun attemptClose() {
        if (isModified) {
            showUnsavedDialog = true
        } else {
            onDismiss()
        }
    }

    // Search matches calculation
    val searchMatches = remember(textFieldValue.text, searchQuery, matchCase) {
        if (searchQuery.isEmpty()) emptyList()
        else {
            try {
                val pattern = Regex.escape(searchQuery)
                val options = if (matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE)
                Regex(pattern, options).findAll(textFieldValue.text).map { it.range }.toList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    // Jump to match
    fun jumpToMatch(index: Int) {
        if (searchMatches.isNotEmpty()) {
            val validIndex = (index % searchMatches.size + searchMatches.size) % searchMatches.size
            activeMatchIndex = validIndex
            val matchRange = searchMatches[validIndex]
            textFieldValue = textFieldValue.copy(selection = TextRange(matchRange.first, matchRange.last + 1))
        }
    }

    // Cursor position calculation
    val cursorIndex = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
    val textBeforeCursor = remember(textFieldValue.text, cursorIndex) {
        textFieldValue.text.substring(0, cursorIndex)
    }
    val currentLine = textBeforeCursor.count { it == '\n' } + 1
    val lastNewline = textBeforeCursor.lastIndexOf('\n')
    val currentCol = if (lastNewline == -1) cursorIndex + 1 else cursorIndex - lastNewline
    val totalLines = remember(textFieldValue.text) {
        textFieldValue.text.lines().size.coerceAtLeast(1)
    }

    // Visual transformation for syntax + search highlights
    val visualTransformation = remember(
        detectedLanguage,
        editorTheme,
        searchQuery,
        activeMatchIndex,
        matchCase
    ) {
        SyntaxHighlighterVisualTransformation(
            language = detectedLanguage,
            theme = editorTheme,
            searchQuery = searchQuery,
            activeMatchIndex = activeMatchIndex,
            matchCase = matchCase
        )
    }

    Dialog(
        onDismissRequest = { attemptClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler { attemptClose() }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .background(editorTheme.background)
                .testTag("text_editor_dialog"),
            color = editorTheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ==================== TOP PRO HEADER BAR ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(editorTheme.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button
                    IconButton(
                        onClick = { attemptClose() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("text_editor_close")
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = editorTheme.text
                        )
                    }

                    // Center File Information Pill
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = file.name,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = editorTheme.text
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Language extension badge
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(editorTheme.gutterBackground)
                                    .clickable { showLanguageDialog = true }
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = detectedLanguage.name.take(6),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SalimBlue
                                )
                            }

                            // Unsaved modified indicator dot
                            if (isModified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SalimBlue)
                                )
                            }
                        }

                        Text(
                            text = if (isModified) "Unsaved · ${FileUtils.formatFileSize(file.length())}"
                            else FileUtils.formatFileSize(file.length()),
                            fontSize = 11.sp,
                            color = if (isModified) SalimBlue else editorTheme.gutterText
                        )
                    }

                    // Action buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Undo button
                        IconButton(
                            onClick = {
                                val prev = history.undo(textFieldValue)
                                if (prev != null) textFieldValue = prev
                            },
                            enabled = history.canUndo,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Undo,
                                contentDescription = "Undo",
                                tint = if (history.canUndo) editorTheme.text else editorTheme.gutterText.copy(alpha = 0.35f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Redo button
                        IconButton(
                            onClick = {
                                val next = history.redo(textFieldValue)
                                if (next != null) textFieldValue = next
                            },
                            enabled = history.canRedo,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Redo,
                                contentDescription = "Redo",
                                tint = if (history.canRedo) editorTheme.text else editorTheme.gutterText.copy(alpha = 0.35f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Search & Replace Toggle
                        IconButton(
                            onClick = {
                                showSearch = !showSearch
                                if (!showSearch) {
                                    searchQuery = ""
                                    showReplace = false
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Find",
                                tint = if (showSearch) SalimBlue else editorTheme.text,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Markdown / Document Preview Toggle
                        if (isMarkdownFile) {
                            IconButton(
                                onClick = { isPreviewMode = !isPreviewMode },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    if (isPreviewMode) Icons.Outlined.Edit else Icons.Outlined.Visibility,
                                    contentDescription = "Preview",
                                    tint = if (isPreviewMode) SalimBlue else editorTheme.text,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // Overflow Options Menu
                        Box {
                            IconButton(
                                onClick = { showMoreMenu = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = "More",
                                    tint = editorTheme.text,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Go to Line…") },
                                    leadingIcon = { Icon(Icons.Outlined.Navigation, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showGoToLine = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (wordWrap) "Disable Word Wrap" else "Enable Word Wrap") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.WrapText, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        wordWrap = !wordWrap
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Editor Theme…") },
                                    leadingIcon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showThemeDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Syntax Language…") },
                                    leadingIcon = { Icon(Icons.Outlined.Code, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showLanguageDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Preferences…") },
                                    leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showSettingsDialog = true
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Copy All") },
                                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText(file.name, textFieldValue.text))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share File") },
                                    leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, file.name)
                                            putExtra(Intent.EXTRA_TEXT, textFieldValue.text)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share text"))
                                    }
                                )
                                if (isModified) {
                                    DropdownMenuItem(
                                        text = { Text("Revert to Saved", color = SalimRed) },
                                        leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null, tint = SalimRed) },
                                        onClick = {
                                            showMoreMenu = false
                                            loadFileContent()
                                        }
                                    )
                                }
                            }
                        }

                        // Save Button
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(20.dp),
                                strokeWidth = 2.dp,
                                color = SalimBlue
                            )
                        } else if (isSaveSuccess) {
                            Icon(
                                Icons.Outlined.Done,
                                contentDescription = "Saved",
                                tint = SalimGreen,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(22.dp)
                            )
                        } else {
                            TextButton(
                                onClick = { saveFile() },
                                enabled = isModified && file.canWrite() && !readOnly,
                                modifier = Modifier.testTag("text_editor_save")
                            ) {
                                Text(
                                    text = "Save",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isModified && file.canWrite() && !readOnly) SalimBlue
                                    else editorTheme.gutterText.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = editorTheme.gutterDivider, thickness = 1.dp)

                // ==================== SEARCH & REPLACE TRAY ====================
                AnimatedVisibility(
                    visible = showSearch,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(editorTheme.surface)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Find Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(editorTheme.gutterBackground)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        activeMatchIndex = 0
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = editorTheme.text,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    cursorBrush = SolidColor(editorTheme.cursor),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Find in file…",
                                                color = editorTheme.gutterText,
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Match Counter
                            if (searchQuery.isNotEmpty()) {
                                val matchCount = searchMatches.size
                                val matchDisplay = if (matchCount > 0) "${activeMatchIndex + 1}/$matchCount" else "0/0"
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = matchDisplay,
                                    fontSize = 12.sp,
                                    color = if (matchCount > 0) editorTheme.text else SalimRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Previous Match
                            IconButton(
                                onClick = { jumpToMatch(activeMatchIndex - 1) },
                                enabled = searchMatches.isNotEmpty(),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.KeyboardArrowUp,
                                    contentDescription = "Previous Match",
                                    tint = if (searchMatches.isNotEmpty()) editorTheme.text else editorTheme.gutterText.copy(alpha = 0.3f)
                                )
                            }

                            // Next Match
                            IconButton(
                                onClick = { jumpToMatch(activeMatchIndex + 1) },
                                enabled = searchMatches.isNotEmpty(),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Next Match",
                                    tint = if (searchMatches.isNotEmpty()) editorTheme.text else editorTheme.gutterText.copy(alpha = 0.3f)
                                )
                            }

                            // Case sensitive toggle [Aa]
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (matchCase) SalimBlue else editorTheme.gutterBackground)
                                    .clickable { matchCase = !matchCase },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aa",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (matchCase) Color.White else editorTheme.text
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Toggle Replace Row
                            IconButton(
                                onClick = { showReplace = !showReplace },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.FindReplace,
                                    contentDescription = "Toggle Replace",
                                    tint = if (showReplace) SalimBlue else editorTheme.text
                                )
                            }
                        }

                        // Replace Row (if expanded)
                        if (showReplace) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(editorTheme.gutterBackground)
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = replaceQuery,
                                        onValueChange = { replaceQuery = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = editorTheme.text,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        cursorBrush = SolidColor(editorTheme.cursor),
                                        decorationBox = { innerTextField ->
                                            if (replaceQuery.isEmpty()) {
                                                Text(
                                                    text = "Replace with…",
                                                    color = editorTheme.gutterText,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            innerTextField()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Replace Button
                                TextButton(
                                    onClick = {
                                        if (searchMatches.isNotEmpty() && activeMatchIndex in searchMatches.indices) {
                                            val currentMatch = searchMatches[activeMatchIndex]
                                            val newText = textFieldValue.text.replaceRange(
                                                currentMatch.first,
                                                currentMatch.last + 1,
                                                replaceQuery
                                            )
                                            val newVal = textFieldValue.copy(
                                                text = newText,
                                                selection = TextRange(currentMatch.first + replaceQuery.length)
                                            )
                                            history.record(newVal, isAtomic = true)
                                            textFieldValue = newVal
                                        }
                                    },
                                    enabled = searchMatches.isNotEmpty() && !readOnly,
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Replace", fontSize = 12.sp, color = SalimBlue)
                                }

                                // Replace All Button
                                TextButton(
                                    onClick = {
                                        if (searchMatches.isNotEmpty()) {
                                            val opts = if (matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE)
                                            val count = searchMatches.size
                                            val newText = textFieldValue.text.replace(
                                                Regex(Regex.escape(searchQuery), opts),
                                                replaceQuery
                                            )
                                            val newVal = textFieldValue.copy(text = newText)
                                            history.record(newVal, isAtomic = true)
                                            textFieldValue = newVal
                                            Toast.makeText(context, "Replaced $count occurrences", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = searchMatches.isNotEmpty() && !readOnly,
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("All", fontSize = 12.sp, color = SalimBlue)
                                }
                            }
                        }
                    }
                }

                if (showSearch) {
                    HorizontalDivider(color = editorTheme.gutterDivider, thickness = 1.dp)
                }

                // ==================== MAIN EDITOR CANVAS ====================
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = SalimBlue)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Loading ${file.name}…",
                                    color = editorTheme.gutterText,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else if (errorMessage != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "Unable to display file",
                                    fontWeight = FontWeight.Bold,
                                    color = editorTheme.text,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = SalimRed,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else if (isPreviewMode && isMarkdownFile) {
                        // Live Markdown Preview
                        MarkdownPreview(
                            markdownText = textFieldValue.text,
                            theme = editorTheme
                        )
                    } else {
                        // Interactive Code / Text Editor Canvas
                        val verticalScroll = rememberScrollState()
                        val horizontalScroll = rememberScrollState()
                        val currentLineHeight = (fontSize * 1.55f).sp

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(editorTheme.background)
                                .verticalScroll(verticalScroll)
                        ) {
                            // Line Numbers Gutter
                            if (showLineNumbers) {
                                Column(
                                    modifier = Modifier
                                        .background(editorTheme.gutterBackground)
                                        .padding(horizontal = 10.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    for (i in 1..totalLines) {
                                        val isCurrent = i == currentLine
                                        Text(
                                            text = "$i",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = fontSize.sp,
                                                lineHeight = currentLineHeight,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrent) editorTheme.gutterActiveText else editorTheme.gutterText
                                            )
                                        )
                                    }
                                }

                                // Gutter separator border
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(editorTheme.gutterDivider)
                                )
                            }

                            // Editable Content Area
                            val codeContentModifier = if (wordWrap) {
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            } else {
                                Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalScroll)
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            }

                            Box(modifier = codeContentModifier) {
                                BasicTextField(
                                    value = textFieldValue,
                                    onValueChange = { newVal ->
                                        history.record(newVal)
                                        textFieldValue = newVal
                                    },
                                    readOnly = readOnly || !file.canWrite(),
                                    visualTransformation = visualTransformation,
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = fontSize.sp,
                                        color = editorTheme.text,
                                        lineHeight = currentLineHeight
                                    ),
                                    cursorBrush = SolidColor(editorTheme.cursor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("text_editor_input")
                                )
                            }
                        }
                    }
                }

                // ==================== QUICK ACCESSORY TOOLBAR ====================
                if (!isPreviewMode && !readOnly && file.canWrite()) {
                    HorizontalDivider(color = editorTheme.gutterDivider, thickness = 1.dp)
                    EditorQuickBar(
                        textFieldValue = textFieldValue,
                        onValueChange = { newVal ->
                            history.record(newVal, isAtomic = true)
                            textFieldValue = newVal
                        },
                        theme = editorTheme
                    )
                }

                // ==================== PRO FOOTER STATUS BAR ====================
                HorizontalDivider(color = editorTheme.gutterDivider, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(editorTheme.surface)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Line & Column indicators + Selection
                    val selLength = Math.abs(textFieldValue.selection.end - textFieldValue.selection.start)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ln $currentLine, Col $currentCol",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = editorTheme.text
                        )
                        if (selLength > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "($selLength selected)",
                                fontSize = 11.sp,
                                color = SalimBlue
                            )
                        }
                    }

                    // Word and character counts
                    val wordCount = remember(textFieldValue.text) {
                        if (textFieldValue.text.isBlank()) 0
                        else textFieldValue.text.trim().split(Regex("\\s+")).size
                    }
                    Text(
                        text = "$totalLines lines · $wordCount words",
                        fontSize = 11.sp,
                        color = editorTheme.gutterText
                    )

                    // Encoding & Language
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "UTF-8",
                            fontSize = 11.sp,
                            color = editorTheme.gutterText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(editorTheme.gutterBackground)
                                .clickable { showLanguageDialog = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = detectedLanguage.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SalimBlue
                            )
                        }
                    }
                }
            }
        }
    }

    // ==================== DIALOG SHEETS ====================

    // Go to Line Dialog
    if (showGoToLine) {
        GoToLineDialog(
            totalLines = totalLines,
            onDismiss = { showGoToLine = false },
            onConfirm = { targetLine ->
                showGoToLine = false
                val lines = textFieldValue.text.lines()
                var charOffset = 0
                for (i in 0 until (targetLine - 1).coerceAtMost(lines.size - 1)) {
                    charOffset += lines[i].length + 1
                }
                textFieldValue = textFieldValue.copy(
                    selection = TextRange(charOffset.coerceIn(0, textFieldValue.text.length))
                )
            },
            theme = editorTheme
        )
    }

    // Editor Preferences Dialog
    if (showSettingsDialog) {
        EditorSettingsDialog(
            fontSize = fontSize,
            onFontSizeChange = { fontSize = it },
            wordWrap = wordWrap,
            onWordWrapChange = { wordWrap = it },
            showLineNumbers = showLineNumbers,
            onShowLineNumbersChange = { showLineNumbers = it },
            tabSpaces = tabSpaces,
            onTabSpacesChange = { tabSpaces = it },
            theme = editorTheme,
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Editor Theme Dialog
    if (showThemeDialog) {
        ThemeSelectorDialog(
            currentTheme = editorTheme,
            onSelectTheme = { editorTheme = it },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Syntax Language Dialog
    if (showLanguageDialog) {
        LanguageSelectorDialog(
            currentLanguage = detectedLanguage,
            onSelectLanguage = { detectedLanguage = it },
            theme = editorTheme,
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Unsaved Changes Confirmation Dialog
    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            fileName = file.name,
            onSave = {
                showUnsavedDialog = false
                saveFile(onSuccessClose = true)
            },
            onDiscard = {
                showUnsavedDialog = false
                onDismiss()
            },
            onCancel = { showUnsavedDialog = false },
            theme = editorTheme
        )
    }
}

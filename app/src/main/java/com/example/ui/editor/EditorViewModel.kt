package com.example.ui.editor

import android.app.Application
import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.DocumentVersionEntity
import com.example.data.repository.DiffLine
import com.example.data.repository.DocumentRepository
import com.example.ui.editor.components.ToolbarMode
import com.example.util.MarkdownParser
import com.example.util.OutlineItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SplitMode {
    NONE,
    SIDE_BY_SIDE_PREVIEW,
    STACKED_PREVIEW
}

data class EditorUiState(
    val document: DocumentEntity? = null,
    val title: String = "",
    val contentValue: TextFieldValue = TextFieldValue(""),
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val toolbarMode: ToolbarMode = ToolbarMode.NORMAL,
    val splitMode: SplitMode = SplitMode.NONE,
    val isFocusMode: Boolean = false,
    // Find & Replace
    val isFindOpen: Boolean = false,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val matchCase: Boolean = false,
    val matchRanges: List<TextRange> = emptyList(),
    val currentMatchIndex: Int = 0,
    // Modals
    val showOutline: Boolean = false,
    val showStats: Boolean = false,
    val showVersionHistory: Boolean = false,
    val outline: List<OutlineItem> = emptyList(),
    val fontSizeZoomDelta: Float = 0f
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository = (application as SalimApplication).documentRepository

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<TextFieldValue>()
    private val redoStack = mutableListOf<TextFieldValue>()
    private var autoSaveJob: Job? = null
    private var currentDocId: Long? = null

    val versionsFlow: StateFlow<List<DocumentVersionEntity>> = MutableStateFlow<List<DocumentVersionEntity>>(emptyList())
        get() = currentDocId?.let { id ->
            repository.getVersions(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } ?: field

    fun loadDocument(docId: Long) {
        currentDocId = docId
        viewModelScope.launch {
            val doc = repository.getDocument(docId)
            if (doc != null) {
                val initialValue = TextFieldValue(
                    text = doc.content,
                    selection = TextRange(doc.lastCursorPosition.coerceIn(0, doc.content.length))
                )
                undoStack.clear()
                redoStack.clear()
                undoStack.add(initialValue)

                val initialOutline = MarkdownParser.extractOutline(doc.content)

                _uiState.update {
                    it.copy(
                        document = doc,
                        title = doc.title,
                        contentValue = initialValue,
                        isDirty = false,
                        canUndo = false,
                        canRedo = false,
                        outline = initialOutline,
                        toolbarMode = if (doc.syntax == "markdown") ToolbarMode.MARKDOWN else if (doc.syntax == "code") ToolbarMode.CODE else ToolbarMode.NORMAL
                    )
                }
            }
        }
    }

    fun onContentChange(newValue: TextFieldValue) {
        val old = _uiState.value.contentValue

        // Detect selection state change
        val isSelectionActive = !newValue.selection.collapsed
        val newToolbarMode = if (isSelectionActive) {
            ToolbarMode.SELECTION
        } else if (_uiState.value.toolbarMode == ToolbarMode.SELECTION) {
            val doc = _uiState.value.document
            if (doc?.syntax == "markdown") ToolbarMode.MARKDOWN else if (doc?.syntax == "code") ToolbarMode.CODE else ToolbarMode.NORMAL
        } else {
            _uiState.value.toolbarMode
        }

        // Push to undo stack if text actually changed
        if (old.text != newValue.text) {
            if (undoStack.isEmpty() || undoStack.last().text != old.text) {
                undoStack.add(old)
                if (undoStack.size > 80) undoStack.removeAt(0)
            }
            redoStack.clear()

            // Update outline
            val newOutline = MarkdownParser.extractOutline(newValue.text)

            _uiState.update {
                it.copy(
                    contentValue = newValue,
                    isDirty = true,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = false,
                    outline = newOutline,
                    toolbarMode = newToolbarMode
                )
            }

            // Update search matches if search is active
            if (_uiState.value.isFindOpen && _uiState.value.searchQuery.isNotEmpty()) {
                refreshSearchMatches(newValue.text, _uiState.value.searchQuery, _uiState.value.matchCase)
            }

            triggerAutoSave()
        } else {
            // Selection or cursor change only
            _uiState.update {
                it.copy(
                    contentValue = newValue,
                    toolbarMode = newToolbarMode
                )
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, isDirty = true) }
        triggerAutoSave()
    }

    fun setToolbarMode(mode: ToolbarMode) {
        _uiState.update { it.copy(toolbarMode = mode) }
    }

    fun toggleSplitMode() {
        _uiState.update {
            val next = when (it.splitMode) {
                SplitMode.NONE -> SplitMode.SIDE_BY_SIDE_PREVIEW
                SplitMode.SIDE_BY_SIDE_PREVIEW -> SplitMode.STACKED_PREVIEW
                SplitMode.STACKED_PREVIEW -> SplitMode.NONE
            }
            it.copy(splitMode = next)
        }
    }

    fun toggleFocusMode() {
        _uiState.update { it.copy(isFocusMode = !it.isFocusMode) }
    }

    fun setZoomDelta(delta: Float) {
        _uiState.update {
            it.copy(fontSizeZoomDelta = (it.fontSizeZoomDelta + delta).coerceIn(-6f, 14f))
        }
    }

    fun resetZoom() {
        _uiState.update { it.copy(fontSizeZoomDelta = 0f) }
    }

    // Modal controls
    fun setShowOutline(show: Boolean) { _uiState.update { it.copy(showOutline = show) } }
    fun setShowStats(show: Boolean) { _uiState.update { it.copy(showStats = show) } }
    fun setShowVersionHistory(show: Boolean) { _uiState.update { it.copy(showVersionHistory = show) } }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = _uiState.value.contentValue
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(current)

            _uiState.update {
                it.copy(
                    contentValue = previous,
                    isDirty = true,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true,
                    outline = MarkdownParser.extractOutline(previous.text)
                )
            }
            triggerAutoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = _uiState.value.contentValue
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(current)

            _uiState.update {
                it.copy(
                    contentValue = next,
                    isDirty = true,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty(),
                    outline = MarkdownParser.extractOutline(next.text)
                )
            }
            triggerAutoSave()
        }
    }

    fun saveDocument(manualSnapshot: Boolean = false, snapshotNote: String = "Manual Save") {
        val currentDoc = _uiState.value.document ?: return
        val text = _uiState.value.contentValue.text
        val cursor = _uiState.value.contentValue.selection.start
        val title = _uiState.value.title.ifBlank { "Untitled" }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val updated = currentDoc.copy(
                title = title,
                content = text,
                lastCursorPosition = cursor
            )
            repository.updateDocument(
                document = updated,
                createSnapshot = manualSnapshot,
                snapshotNote = snapshotNote
            )
            _uiState.update {
                it.copy(
                    document = updated,
                    isDirty = false,
                    isSaving = false
                )
            }
        }
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1200) // 1.2s debounce for smooth silky typing
            saveDocument(manualSnapshot = false)
        }
    }

    // Text manipulation operations
    fun insertText(insert: String) {
        val current = _uiState.value.contentValue
        val before = current.text.substring(0, current.selection.min)
        val after = current.text.substring(current.selection.max)
        val newText = before + insert + after
        val newCursor = before.length + insert.length
        onContentChange(TextFieldValue(newText, TextRange(newCursor)))
    }

    fun wrapSelection(open: String, close: String) {
        val current = _uiState.value.contentValue
        val start = current.selection.min
        val end = current.selection.max
        val selected = current.text.substring(start, end)
        val before = current.text.substring(0, start)
        val after = current.text.substring(end)
        val wrapped = open + selected + close
        val newText = before + wrapped + after
        val newRange = if (selected.isEmpty()) {
            TextRange(start + open.length)
        } else {
            TextRange(start + open.length, start + open.length + selected.length)
        }
        onContentChange(TextFieldValue(newText, newRange))
    }

    fun toggleLinePrefix(prefix: String) {
        val current = _uiState.value.contentValue
        val text = current.text
        val cursor = current.selection.start

        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        val newLine = if (line.startsWith(prefix)) {
            line.removePrefix(prefix)
        } else {
            prefix + line
        }

        val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
        val delta = newLine.length - line.length
        val newCursor = (cursor + delta).coerceIn(0, newText.length)
        onContentChange(TextFieldValue(newText, TextRange(newCursor)))
    }

    fun toggleHeading(level: Int) {
        val prefix = "#".repeat(level) + " "
        val current = _uiState.value.contentValue
        val text = current.text
        val cursor = current.selection.start

        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        // Remove any existing heading marks
        val stripped = line.replace(Regex("^#+\\s*"), "")
        val newLine = if (line.startsWith(prefix)) stripped else prefix + stripped

        val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
        val delta = newLine.length - line.length
        onContentChange(TextFieldValue(newText, TextRange((cursor + delta).coerceIn(0, newText.length))))
    }

    fun indent() {
        toggleLinePrefix("  ")
    }

    fun dedent() {
        val current = _uiState.value.contentValue
        val text = current.text
        val cursor = current.selection.start

        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        val newLine = if (line.startsWith("  ")) {
            line.substring(2)
        } else if (line.startsWith(" ")) {
            line.substring(1)
        } else {
            line
        }

        val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
        val delta = newLine.length - line.length
        onContentChange(TextFieldValue(newText, TextRange((cursor + delta).coerceIn(0, newText.length))))
    }

    fun toggleComment() {
        val doc = _uiState.value.document
        val prefix = if (doc?.extension == "py" || doc?.extension == "sh" || doc?.extension == "yaml" || doc?.extension == "yml") "# " else "// "
        toggleLinePrefix(prefix)
    }

    fun moveCursor(delta: Int) {
        val current = _uiState.value.contentValue
        val newPos = (current.selection.start + delta).coerceIn(0, current.text.length)
        _uiState.update { it.copy(contentValue = current.copy(selection = TextRange(newPos))) }
    }

    fun moveLineStart() {
        val current = _uiState.value.contentValue
        val cursor = current.selection.start
        val lineStart = current.text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        _uiState.update { it.copy(contentValue = current.copy(selection = TextRange(lineStart))) }
    }

    fun moveLineEnd() {
        val current = _uiState.value.contentValue
        val cursor = current.selection.start
        val lineEnd = current.text.indexOf('\n', cursor).let { if (it == -1) current.text.length else it }
        _uiState.update { it.copy(contentValue = current.copy(selection = TextRange(lineEnd))) }
    }

    // Find and Replace
    fun openFind() {
        _uiState.update { it.copy(isFindOpen = true) }
    }

    fun closeFind() {
        _uiState.update {
            it.copy(isFindOpen = false, searchQuery = "", replaceQuery = "", matchRanges = emptyList())
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshSearchMatches(_uiState.value.contentValue.text, query, _uiState.value.matchCase)
    }

    fun onReplaceQueryChange(query: String) {
        _uiState.update { it.copy(replaceQuery = query) }
    }

    fun toggleMatchCase() {
        val newCase = !_uiState.value.matchCase
        _uiState.update { it.copy(matchCase = newCase) }
        refreshSearchMatches(_uiState.value.contentValue.text, _uiState.value.searchQuery, newCase)
    }

    private fun refreshSearchMatches(text: String, query: String, matchCase: Boolean) {
        if (query.isEmpty()) {
            _uiState.update { it.copy(matchRanges = emptyList(), currentMatchIndex = 0) }
            return
        }

        val ranges = mutableListOf<TextRange>()
        var startIndex = 0
        while (startIndex < text.length) {
            val idx = text.indexOf(query, startIndex, ignoreCase = !matchCase)
            if (idx == -1) break
            ranges.add(TextRange(idx, idx + query.length))
            startIndex = idx + query.length
        }

        _uiState.update {
            it.copy(
                matchRanges = ranges,
                currentMatchIndex = if (ranges.isNotEmpty()) 0 else 0
            )
        }

        if (ranges.isNotEmpty()) {
            jumpToMatch(0)
        }
    }

    fun nextMatch() {
        val state = _uiState.value
        if (state.matchRanges.isNotEmpty()) {
            val nextIdx = (state.currentMatchIndex + 1) % state.matchRanges.size
            _uiState.update { it.copy(currentMatchIndex = nextIdx) }
            jumpToMatch(nextIdx)
        }
    }

    fun prevMatch() {
        val state = _uiState.value
        if (state.matchRanges.isNotEmpty()) {
            val prevIdx = if (state.currentMatchIndex - 1 < 0) state.matchRanges.size - 1 else state.currentMatchIndex - 1
            _uiState.update { it.copy(currentMatchIndex = prevIdx) }
            jumpToMatch(prevIdx)
        }
    }

    private fun jumpToMatch(index: Int) {
        val range = _uiState.value.matchRanges.getOrNull(index) ?: return
        val current = _uiState.value.contentValue
        _uiState.update {
            it.copy(contentValue = current.copy(selection = range))
        }
    }

    fun replaceOne() {
        val state = _uiState.value
        val range = state.matchRanges.getOrNull(state.currentMatchIndex) ?: return
        val text = state.contentValue.text
        val newText = text.substring(0, range.start) + state.replaceQuery + text.substring(range.end)
        val newSelection = TextRange(range.start + state.replaceQuery.length)
        onContentChange(TextFieldValue(newText, newSelection))
    }

    fun replaceAll() {
        val state = _uiState.value
        if (state.searchQuery.isEmpty()) return
        val text = state.contentValue.text
        val newText = text.replace(state.searchQuery, state.replaceQuery, ignoreCase = !state.matchCase)
        onContentChange(TextFieldValue(newText, TextRange(0)))
    }

    fun computeDiff(oldText: String, newText: String): List<DiffLine> {
        return repository.computeDiff(oldText, newText)
    }

    fun restoreVersion(content: String) {
        onContentChange(TextFieldValue(content, TextRange(0)))
        saveDocument(manualSnapshot = true, snapshotNote = "Restored from version history")
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}

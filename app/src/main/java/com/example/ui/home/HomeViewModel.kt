package com.example.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.TagEntity
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DocumentFilter(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    FAVORITES("Favorites"),
    MARKDOWN("Markdown"),
    CODE("Code"),
    PLAIN("Plain Text")
}

enum class ViewMode {
    LIST,
    COMPACT,
    GRID
}

enum class SortOption(val label: String) {
    MODIFIED_DESC("Newest First"),
    MODIFIED_ASC("Oldest First"),
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    WORDS_DESC("Word Count"),
    SIZE_DESC("File Size")
}

data class HomeUiState(
    val documents: List<DocumentEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: DocumentFilter = DocumentFilter.ALL,
    val selectedFolderId: Long? = null,
    val selectedTagName: String? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOption: SortOption = SortOption.MODIFIED_DESC,
    val isMultiSelectMode: Boolean = false,
    val selectedDocIds: Set<Long> = emptySet(),
    val totalDocumentCount: Int = 0,
    val totalWordCount: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository = (application as SalimApplication).documentRepository

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(DocumentFilter.ALL)
    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    private val _selectedTagName = MutableStateFlow<String?>(null)
    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    private val _sortOption = MutableStateFlow(SortOption.MODIFIED_DESC)
    private val _isMultiSelectMode = MutableStateFlow(false)
    private val _selectedDocIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allDocumentsFlow,
        repository.foldersFlow,
        repository.tagsFlow,
        _searchQuery,
        _activeFilter,
        _selectedFolderId,
        _selectedTagName,
        _viewMode,
        _sortOption,
        _isMultiSelectMode,
        _selectedDocIds
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allDocs = args[0] as List<DocumentEntity>
        @Suppress("UNCHECKED_CAST")
        val folders = args[1] as List<FolderEntity>
        @Suppress("UNCHECKED_CAST")
        val tags = args[2] as List<TagEntity>
        val query = args[3] as String
        val filter = args[4] as DocumentFilter
        val folderId = args[5] as Long?
        val tagName = args[6] as String?
        val viewMode = args[7] as ViewMode
        val sort = args[8] as SortOption
        val multiSelect = args[9] as Boolean
        @Suppress("UNCHECKED_CAST")
        val selectedIds = args[10] as Set<Long>

        // 1. Filter
        var filtered = allDocs.filter { doc ->
            // Search filter
            val matchesQuery = query.isBlank() ||
                    doc.title.contains(query, ignoreCase = true) ||
                    doc.content.contains(query, ignoreCase = true) ||
                    doc.tags.contains(query, ignoreCase = true)

            // Category filter
            val matchesCategory = when (filter) {
                DocumentFilter.ALL -> true
                DocumentFilter.PINNED -> doc.isPinned
                DocumentFilter.FAVORITES -> doc.isFavorite
                DocumentFilter.MARKDOWN -> doc.extension.equals("md", ignoreCase = true) || doc.syntax == "markdown"
                DocumentFilter.CODE -> doc.syntax == "code" || listOf("kt", "py", "js", "html", "css", "json", "sql", "java", "c", "cpp").contains(doc.extension.lowercase())
                DocumentFilter.PLAIN -> doc.extension.equals("txt", ignoreCase = true) || doc.syntax == "plaintext"
            }

            // Folder filter
            val matchesFolder = folderId == null || doc.folderId == folderId

            // Tag filter
            val matchesTag = tagName == null || doc.tags.split(",").map { it.trim() }.contains(tagName)

            matchesQuery && matchesCategory && matchesFolder && matchesTag
        }

        // 2. Sort (keep pinned on top for default views)
        filtered = when (sort) {
            SortOption.MODIFIED_DESC -> filtered.sortedWith(compareByDescending<DocumentEntity> { it.isPinned }.thenByDescending { it.updatedAt })
            SortOption.MODIFIED_ASC -> filtered.sortedWith(compareByDescending<DocumentEntity> { it.isPinned }.thenBy { it.updatedAt })
            SortOption.TITLE_ASC -> filtered.sortedWith(compareByDescending<DocumentEntity> { it.isPinned }.thenBy { it.title.lowercase() })
            SortOption.TITLE_DESC -> filtered.sortedWith(compareByDescending<DocumentEntity> { it.isPinned }.thenByDescending { it.title.lowercase() })
            SortOption.WORDS_DESC -> filtered.sortedWith(compareByDescending<DocumentEntity> { it.isPinned }.thenByDescending { it.wordCount })
            SortOption.SIZE_DESC -> filtered.sortedWith(compareByDescending<DocumentEntity> { it.isPinned }.thenByDescending { it.charCount })
        }

        val totalWords = allDocs.sumOf { it.wordCount }

        HomeUiState(
            documents = filtered,
            folders = folders,
            tags = tags,
            searchQuery = query,
            activeFilter = filter,
            selectedFolderId = folderId,
            selectedTagName = tagName,
            viewMode = viewMode,
            sortOption = sort,
            isMultiSelectMode = multiSelect,
            selectedDocIds = selectedIds,
            totalDocumentCount = allDocs.size,
            totalWordCount = totalWords,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: DocumentFilter) {
        _activeFilter.value = filter
        _selectedFolderId.value = null
        _selectedTagName.value = null
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
        if (folderId != null) {
            _activeFilter.value = DocumentFilter.ALL
            _selectedTagName.value = null
        }
    }

    fun selectTag(tagName: String?) {
        _selectedTagName.value = tagName
        if (tagName != null) {
            _activeFilter.value = DocumentFilter.ALL
            _selectedFolderId.value = null
        }
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleMultiSelect(docId: Long) {
        val current = _selectedDocIds.value.toMutableSet()
        if (current.contains(docId)) {
            current.remove(docId)
        } else {
            current.add(docId)
        }
        _selectedDocIds.value = current
        if (current.isEmpty()) {
            _isMultiSelectMode.value = false
        } else {
            _isMultiSelectMode.value = true
        }
    }

    fun startMultiSelect(docId: Long) {
        _isMultiSelectMode.value = true
        _selectedDocIds.value = setOf(docId)
    }

    fun clearMultiSelect() {
        _isMultiSelectMode.value = false
        _selectedDocIds.value = emptySet()
    }

    fun selectAll() {
        _selectedDocIds.value = uiState.value.documents.map { it.id }.toSet()
        _isMultiSelectMode.value = true
    }

    // Document operations
    fun createNewDocument(title: String = "Untitled", extension: String = "txt", syntax: String = "plaintext", onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createDocument(
                title = title,
                content = "",
                extension = extension,
                syntax = syntax,
                folderId = _selectedFolderId.value
            )
            onCreated(id)
        }
    }

    fun createQuickNote(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            val id = repository.createDocument(
                title = "Note ($dateStr)",
                content = "",
                extension = "txt",
                syntax = "plaintext",
                folderId = _selectedFolderId.value
            )
            onCreated(id)
        }
    }

    fun importFromUri(uri: Uri, onImported: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.importFromUri(uri)
            onImported(id)
        }
    }

    fun togglePin(docId: Long) {
        viewModelScope.launch { repository.togglePin(docId) }
    }

    fun toggleFavorite(docId: Long) {
        viewModelScope.launch { repository.toggleFavorite(docId) }
    }

    fun duplicate(doc: DocumentEntity) {
        viewModelScope.launch { repository.duplicateDocument(doc) }
    }

    fun moveToTrash(docId: Long) {
        viewModelScope.launch { repository.moveToTrash(docId) }
    }

    fun renameDocument(docId: Long, newTitle: String) {
        viewModelScope.launch {
            val doc = repository.getDocument(docId) ?: return@launch
            repository.updateDocument(doc.copy(title = newTitle))
        }
    }

    fun setDocumentFolder(docId: Long, folderId: Long?) {
        viewModelScope.launch {
            val doc = repository.getDocument(docId) ?: return@launch
            repository.updateDocument(doc.copy(folderId = folderId))
        }
    }

    fun setDocumentTags(docId: Long, tags: String) {
        viewModelScope.launch {
            val doc = repository.getDocument(docId) ?: return@launch
            repository.updateDocument(doc.copy(tags = tags))
        }
    }

    // Batch operations
    fun batchMoveToTrash() {
        val ids = _selectedDocIds.value
        viewModelScope.launch {
            ids.forEach { repository.moveToTrash(it) }
            clearMultiSelect()
        }
    }

    fun batchPin() {
        val ids = _selectedDocIds.value
        viewModelScope.launch {
            ids.forEach { repository.togglePin(it) }
            clearMultiSelect()
        }
    }

    fun batchFavorite() {
        val ids = _selectedDocIds.value
        viewModelScope.launch {
            ids.forEach { repository.toggleFavorite(it) }
            clearMultiSelect()
        }
    }

    // Folders & Tags
    fun createFolder(name: String, colorHex: String = "#2563EB") {
        viewModelScope.launch {
            repository.createFolder(name, colorHex)
        }
    }

    fun createTag(name: String, colorHex: String = "#8B5CF6") {
        viewModelScope.launch {
            repository.createTag(name, colorHex)
        }
    }
}

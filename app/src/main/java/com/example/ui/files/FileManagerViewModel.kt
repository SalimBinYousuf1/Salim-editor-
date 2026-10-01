package com.example.ui.files

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.repository.FileManagerRepository
import com.example.data.repository.FileItem
import com.example.data.repository.FileSortOption
import com.example.data.repository.StorageInfo
import com.example.ui.home.ViewMode
import com.example.util.FileCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class ClipboardMode {
    COPY,
    CUT
}

sealed interface FileDialogType {
    data object CreateFolder : FileDialogType
    data object CreateFile : FileDialogType
    data class Rename(val item: FileItem) : FileDialogType
    data class Details(val item: FileItem) : FileDialogType
    data class DeleteConfirm(val items: List<File>, val permanent: Boolean) : FileDialogType
    data class ZipDialog(val items: List<File>) : FileDialogType
}

data class FileManagerUiState(
    val currentDirectory: File,
    val breadcrumbs: List<File> = emptyList(),
    val files: List<FileItem> = emptyList(),
    val storageInfo: StorageInfo = StorageInfo(),
    val selectedCategory: FileCategory = FileCategory.ALL,
    val searchQuery: String = "",
    val showHiddenFiles: Boolean = false,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOption: FileSortOption = FileSortOption.NAME_ASC,
    val isMultiSelectMode: Boolean = false,
    val selectedPaths: Set<String> = emptySet(),
    val clipboardMode: ClipboardMode? = null,
    val clipboardFiles: List<File> = emptyList(),
    val activeDialog: FileDialogType? = null,
    val isLoading: Boolean = false,
    val hasStoragePermission: Boolean = true,
    val userMessage: String? = null
)

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FileManagerRepository = (application as SalimApplication).fileManagerRepository

    private val rootDir = repository.getRootStorageDirectory()

    private val _uiState = MutableStateFlow(
        FileManagerUiState(
            currentDirectory = rootDir,
            breadcrumbs = listOf(rootDir),
            hasStoragePermission = repository.hasStoragePermission()
        )
    )
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    init {
        loadDirectory(_uiState.value.currentDirectory)
        viewModelScope.launch {
            repository.storageInfo.collect { info ->
                _uiState.update { it.copy(storageInfo = info) }
            }
        }
    }

    fun checkPermissions() {
        val hasPerm = repository.hasStoragePermission()
        _uiState.update { it.copy(hasStoragePermission = hasPerm) }
        if (hasPerm) {
            refresh()
        }
    }

    fun loadDirectory(directory: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val items = repository.listFiles(
                directory = directory,
                showHidden = state.showHiddenFiles,
                category = state.selectedCategory,
                sortOption = state.sortOption,
                searchQuery = state.searchQuery
            )
            val crumbs = buildBreadcrumbs(directory)
            _uiState.update {
                it.copy(
                    currentDirectory = directory,
                    breadcrumbs = crumbs,
                    files = items,
                    isLoading = false,
                    selectedPaths = if (it.isMultiSelectMode) it.selectedPaths else emptySet()
                )
            }
            repository.refreshStorageInfo()
        }
    }

    private fun buildBreadcrumbs(dir: File): List<File> {
        val list = mutableListOf<File>()
        var current: File? = dir
        while (current != null) {
            list.add(0, current)
            if (current == rootDir || current.parentFile == null) break
            current = current.parentFile
        }
        return list
    }

    fun navigateTo(directory: File) {
        if (directory.isDirectory && directory.canRead()) {
            loadDirectory(directory)
        } else {
            showMessage("Cannot open directory")
        }
    }

    fun navigateUp(): Boolean {
        val current = _uiState.value.currentDirectory
        val parent = current.parentFile
        return if (parent != null && parent.canRead() && current != rootDir) {
            loadDirectory(parent)
            true
        } else {
            false
        }
    }

    fun navigateToRoot() = loadDirectory(rootDir)
    fun navigateToDownloads() = loadDirectory(repository.getDownloadsDirectory())
    fun navigateToDocuments() = loadDirectory(repository.getDocumentsDirectory())
    fun navigateToPictures() = loadDirectory(repository.getPicturesDirectory())
    fun navigateToMusic() = loadDirectory(repository.getMusicDirectory())
    fun navigateToDCIM() = loadDirectory(repository.getDCIMDirectory())

    fun refresh() {
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun setCategory(category: FileCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun toggleHiddenFiles() {
        val next = !_uiState.value.showHiddenFiles
        _uiState.update { it.copy(showHiddenFiles = next) }
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun setSortOption(sort: FileSortOption) {
        _uiState.update { it.copy(sortOption = sort) }
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun toggleSelection(path: String) {
        val current = _uiState.value.selectedPaths.toMutableSet()
        if (current.contains(path)) {
            current.remove(path)
        } else {
            current.add(path)
        }
        _uiState.update {
            it.copy(
                selectedPaths = current,
                isMultiSelectMode = current.isNotEmpty()
            )
        }
    }

    fun selectAll() {
        val allPaths = _uiState.value.files.map { it.path }.toSet()
        _uiState.update {
            it.copy(selectedPaths = allPaths, isMultiSelectMode = true)
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(selectedPaths = emptySet(), isMultiSelectMode = false)
        }
    }

    fun copySelected() {
        val files = getSelectedFiles()
        _uiState.update {
            it.copy(
                clipboardMode = ClipboardMode.COPY,
                clipboardFiles = files,
                isMultiSelectMode = false,
                selectedPaths = emptySet()
            )
        }
        showMessage("${files.size} items copied to clipboard")
    }

    fun cutSelected() {
        val files = getSelectedFiles()
        _uiState.update {
            it.copy(
                clipboardMode = ClipboardMode.CUT,
                clipboardFiles = files,
                isMultiSelectMode = false,
                selectedPaths = emptySet()
            )
        }
        showMessage("${files.size} items cut to clipboard")
    }

    fun copyFile(file: File) {
        _uiState.update {
            it.copy(
                clipboardMode = ClipboardMode.COPY,
                clipboardFiles = listOf(file)
            )
        }
        showMessage("Copied ${file.name}")
    }

    fun cutFile(file: File) {
        _uiState.update {
            it.copy(
                clipboardMode = ClipboardMode.CUT,
                clipboardFiles = listOf(file)
            )
        }
        showMessage("Cut ${file.name}")
    }

    fun paste() {
        val mode = _uiState.value.clipboardMode ?: return
        val files = _uiState.value.clipboardFiles
        val dest = _uiState.value.currentDirectory

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = if (mode == ClipboardMode.COPY) {
                repository.copyFiles(files, dest)
            } else {
                repository.moveFiles(files, dest)
            }

            result.onSuccess { count ->
                showMessage("Pasted $count items")
                _uiState.update { it.copy(clipboardMode = null, clipboardFiles = emptyList()) }
                loadDirectory(dest)
            }.onFailure { err ->
                showMessage("Paste failed: ${err.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun cancelClipboard() {
        _uiState.update { it.copy(clipboardMode = null, clipboardFiles = emptyList()) }
    }

    fun openDialog(dialog: FileDialogType) {
        _uiState.update { it.copy(activeDialog = dialog) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = null) }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val res = repository.createFolder(_uiState.value.currentDirectory, name.trim())
            res.onSuccess {
                showMessage("Folder created")
                dismissDialog()
                refresh()
            }.onFailure {
                showMessage(it.message ?: "Failed to create folder")
            }
        }
    }

    fun createFile(name: String, content: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val res = repository.createFile(_uiState.value.currentDirectory, name.trim(), content)
            res.onSuccess {
                showMessage("File created")
                dismissDialog()
                refresh()
            }.onFailure {
                showMessage(it.message ?: "Failed to create file")
            }
        }
    }

    fun rename(file: File, newName: String) {
        if (newName.isBlank() || newName == file.name) {
            dismissDialog()
            return
        }
        viewModelScope.launch {
            val res = repository.rename(file, newName.trim())
            res.onSuccess {
                showMessage("Renamed to $newName")
                dismissDialog()
                refresh()
            }.onFailure {
                showMessage(it.message ?: "Rename failed")
            }
        }
    }

    fun deleteItem(file: File, permanent: Boolean) {
        viewModelScope.launch {
            val res = if (permanent) {
                repository.deletePermanently(file)
            } else {
                repository.moveToTrash(file)
            }
            res.onSuccess {
                showMessage(if (permanent) "Deleted permanently" else "Moved to Recycle Bin")
                dismissDialog()
                refresh()
            }.onFailure {
                showMessage(it.message ?: "Delete failed")
            }
        }
    }

    fun deleteSelected(permanent: Boolean) {
        val files = getSelectedFiles()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var count = 0
            for (f in files) {
                val res = if (permanent) repository.deletePermanently(f) else repository.moveToTrash(f)
                if (res.isSuccess) count++
            }
            clearSelection()
            dismissDialog()
            showMessage("Deleted $count items")
            refresh()
        }
    }

    fun zipSelected(zipName: String) {
        val files = getSelectedFiles()
        if (files.isEmpty() || zipName.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.zipFiles(files, zipName.trim(), _uiState.value.currentDirectory)
            res.onSuccess {
                showMessage("Created ${it.name}")
                clearSelection()
                dismissDialog()
                refresh()
            }.onFailure {
                showMessage("Zip failed: ${it.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun extractZip(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val folderName = file.nameWithoutExtension
            val dest = File(_uiState.value.currentDirectory, folderName)
            val res = repository.unzipArchive(file, dest)
            res.onSuccess {
                showMessage("Extracted to $folderName/")
                refresh()
            }.onFailure {
                showMessage("Extraction failed: ${it.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(userMessage = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun getSelectedFiles(): List<File> {
        val paths = _uiState.value.selectedPaths
        return _uiState.value.files.filter { paths.contains(it.path) }.map { it.file }
    }
}

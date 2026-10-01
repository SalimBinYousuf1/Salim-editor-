package com.example.ui.trash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.DocumentEntity
import com.example.data.repository.DocumentRepository
import com.example.data.repository.FileManagerRepository
import com.example.data.repository.TrashedFileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class RecycleBinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository = (application as SalimApplication).documentRepository
    private val fileManagerRepository: FileManagerRepository = (application as SalimApplication).fileManagerRepository

    val trashedDocuments: StateFlow<List<DocumentEntity>> = repository.trashedDocumentsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _trashedFiles = MutableStateFlow<List<TrashedFileInfo>>(emptyList())
    val trashedFiles: StateFlow<List<TrashedFileInfo>> = _trashedFiles.asStateFlow()

    init {
        loadTrashedFiles()
    }

    fun loadTrashedFiles() {
        viewModelScope.launch {
            _trashedFiles.value = fileManagerRepository.getTrashedFiles()
        }
    }

    fun restoreDocument(docId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(docId)
        }
    }

    fun restoreFile(file: File) {
        viewModelScope.launch {
            fileManagerRepository.restoreFileFromTrash(file)
            loadTrashedFiles()
        }
    }

    fun restoreAll() {
        viewModelScope.launch {
            trashedDocuments.value.forEach { doc ->
                repository.restoreFromTrash(doc.id)
            }
            _trashedFiles.value.forEach { item ->
                fileManagerRepository.restoreFileFromTrash(item.file)
            }
            loadTrashedFiles()
        }
    }

    fun deletePermanently(docId: Long) {
        viewModelScope.launch {
            repository.deletePermanently(docId)
        }
    }

    fun deletePermanentlyFile(file: File) {
        viewModelScope.launch {
            fileManagerRepository.deletePermanently(file)
            loadTrashedFiles()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            fileManagerRepository.emptyTrash()
            loadTrashedFiles()
        }
    }
}

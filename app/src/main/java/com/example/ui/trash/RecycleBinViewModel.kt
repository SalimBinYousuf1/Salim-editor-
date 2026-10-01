package com.example.ui.trash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.DocumentEntity
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecycleBinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository = (application as SalimApplication).documentRepository

    val trashedDocuments: StateFlow<List<DocumentEntity>> = repository.trashedDocumentsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun restoreDocument(docId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(docId)
        }
    }

    fun restoreAll() {
        viewModelScope.launch {
            trashedDocuments.value.forEach { doc ->
                repository.restoreFromTrash(doc.id)
            }
        }
    }

    fun deletePermanently(docId: Long) {
        viewModelScope.launch {
            repository.deletePermanently(docId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }
}

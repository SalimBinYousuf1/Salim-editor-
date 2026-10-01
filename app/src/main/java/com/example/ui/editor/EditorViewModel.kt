package com.example.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.DocumentEntity
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorUiState(
    val document: DocumentEntity? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = true
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository = (application as SalimApplication).documentRepository

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun loadDocument(docId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val doc = repository.getDocument(docId)
            _uiState.value = EditorUiState(document = doc, isLoading = false)
        }
    }

    fun saveContent(docId: Long, newTitle: String, newContent: String) {
        viewModelScope.launch {
            val current = repository.getDocument(docId) ?: return@launch
            val updated = current.copy(
                title = newTitle,
                content = newContent,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateDocument(updated)
            repository.saveVersion(docId, newContent)
            _uiState.value = _uiState.value.copy(document = updated, isSaved = true)
        }
    }
}

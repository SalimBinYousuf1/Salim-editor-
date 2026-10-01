package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val extension: String = "txt",
    val syntax: String = "plaintext",
    val filePath: String? = null,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedTimestamp: Long? = null,
    val folderId: Long? = null,
    val tags: String = "",
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val lineCount: Int = 0,
    val readingTimeMinutes: Int = 1,
    val cursorPosition: Int = 0,
    val scrollPosition: Int = 0,
    val encoding: String = "UTF-8",
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val modifiedAt: Long get() = updatedAt
}

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val colorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val name: String,
    val colorHex: String = "#007AFF"
)

@Entity(tableName = "document_versions")
data class DocumentVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long,
    val content: String,
    val wordCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "Autosaved snapshot"
)

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["isTrashed"]),
        Index(value = ["isPinned"]),
        Index(value = ["isFavorite"]),
        Index(value = ["folderId"]),
        Index(value = ["updatedAt"]),
        Index(value = ["extension"])
    ]
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val filePath: String? = null,
    val uriString: String? = null,
    val extension: String = "txt",
    val encoding: String = "UTF-8",
    val lineEnding: String = "LF",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedTimestamp: Long? = null,
    val folderId: Long? = null,
    val tags: String = "",
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val lineCount: Int = 0,
    val lastCursorPosition: Int = 0,
    val lastScrollPosition: Int = 0,
    val readOnly: Boolean = false,
    val syntax: String = "plaintext",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#2563EB",
    val icon: String = "folder",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#8B5CF6"
)

@Entity(
    tableName = "document_versions",
    indices = [Index(value = ["documentId", "snapshotTimestamp"])]
)
data class DocumentVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val content: String,
    val snapshotTimestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Untitled",
    val content: String = "",
    val extension: String = "txt",
    val syntax: String = "plaintext",
    val folderId: Long? = null,
    val tags: String = "",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAt: Long? = null,
    val trashedTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val filePath: String? = null
)

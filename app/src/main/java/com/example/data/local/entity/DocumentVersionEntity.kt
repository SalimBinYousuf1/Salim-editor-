package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_versions")
data class DocumentVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

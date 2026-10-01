package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DocumentVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentVersionDao {

    @Query("SELECT * FROM document_versions WHERE documentId = :docId ORDER BY timestamp DESC")
    fun getVersionsForDocument(docId: Long): Flow<List<DocumentVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: DocumentVersionEntity): Long

    @Query("DELETE FROM document_versions WHERE documentId = :docId")
    suspend fun deleteVersionsForDocument(docId: Long)
}

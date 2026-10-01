package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE isTrashed = 0 ORDER BY updatedAt DESC")
    fun getAllActiveDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 1 ORDER BY trashedAt DESC")
    fun getTrashedDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentByIdFlow(id: Long): Flow<DocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePin(id: Long)

    @Query("UPDATE documents SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("UPDATE documents SET isTrashed = 1, trashedAt = :trashedAt WHERE id = :id")
    suspend fun moveToTrash(id: Long, trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isTrashed = 0, trashedAt = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM documents WHERE isTrashed = 1")
    suspend fun emptyTrash()

    @Query("SELECT COUNT(*) FROM documents WHERE isTrashed = 0")
    suspend fun getCount(): Int
}

package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.DocumentVersionEntity
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE isTrashed = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveFlow(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 1 ORDER BY trashedTimestamp DESC")
    fun getTrashedFlow(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND isFavorite = 1 ORDER BY isPinned DESC, updatedAt DESC")
    fun getFavoritesFlow(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedFlow(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND folderId = :folderId ORDER BY isPinned DESC, updatedAt DESC")
    fun getByFolderFlow(folderId: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY isPinned DESC, updatedAt DESC")
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentByIdFlow(id: Long): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentDocuments(limit: Int = 10): Flow<List<DocumentEntity>>

    @Query("SELECT COUNT(*) FROM documents WHERE isTrashed = 0")
    fun countActive(): Flow<Int>

    @Query("SELECT SUM(wordCount) FROM documents WHERE isTrashed = 0")
    fun sumWords(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: DocumentEntity): Long

    @Update
    suspend fun update(document: DocumentEntity)

    @Delete
    suspend fun delete(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM documents WHERE isTrashed = 1")
    suspend fun emptyTrash()
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFoldersFlow(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FolderEntity?
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsFlow(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Delete
    suspend fun delete(tag: TagEntity)
}

@Dao
interface DocumentVersionDao {
    @Query("SELECT * FROM document_versions WHERE documentId = :docId ORDER BY snapshotTimestamp DESC")
    fun getVersionsForDocument(docId: Long): Flow<List<DocumentVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(version: DocumentVersionEntity): Long

    @Query("DELETE FROM document_versions WHERE documentId = :docId")
    suspend fun deleteVersionsForDocument(docId: Long)
}

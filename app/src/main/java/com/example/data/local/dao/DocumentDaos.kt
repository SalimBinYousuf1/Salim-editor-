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
    fun getAllActiveDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 1 ORDER BY updatedAt DESC")
    fun getTrashedDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentFlow(id: Long): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND folderId = :folderId ORDER BY updatedAt DESC")
    fun getDocumentsInFolder(folderId: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET isTrashed = 1, trashedTimestamp = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isTrashed = 0, trashedTimestamp = NULL, updatedAt = :timestamp WHERE id = :id")
    suspend fun restoreFromTrash(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM documents WHERE isTrashed = 1")
    suspend fun emptyTrash()

    @Query("UPDATE documents SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePin(id: Long)

    @Query("UPDATE documents SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("UPDATE documents SET folderId = :folderId WHERE id = :id")
    suspend fun setFolder(id: Long, folderId: Long?)

    @Query("UPDATE documents SET tags = :tags WHERE id = :id")
    suspend fun setTags(id: Long, tags: String)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    suspend fun renameFolder(id: Long, name: String)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE name = :name")
    suspend fun deleteTag(name: String)
}

@Dao
interface DocumentVersionDao {
    @Query("SELECT * FROM document_versions WHERE documentId = :docId ORDER BY timestamp DESC LIMIT 30")
    fun getVersionsForDocument(docId: Long): Flow<List<DocumentVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: DocumentVersionEntity): Long

    @Query("DELETE FROM document_versions WHERE id = :id")
    suspend fun deleteVersion(id: Long)

    @Query("DELETE FROM document_versions WHERE documentId = :docId")
    suspend fun deleteAllVersionsForDocument(docId: Long)
}

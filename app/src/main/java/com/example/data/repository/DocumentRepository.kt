package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.dao.DocumentDao
import com.example.data.local.dao.DocumentVersionDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.dao.TagDao
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.DocumentVersionEntity
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val versionDao: DocumentVersionDao,
    private val context: Context
) {

    val allDocumentsFlow: Flow<List<DocumentEntity>> = documentDao.getAllActiveDocuments()
    val trashedDocumentsFlow: Flow<List<DocumentEntity>> = documentDao.getTrashedDocuments()
    val foldersFlow: Flow<List<FolderEntity>> = folderDao.getAllFolders()
    val tagsFlow: Flow<List<TagEntity>> = tagDao.getAllTags()

    suspend fun getDocument(id: Long): DocumentEntity? = documentDao.getDocumentById(id)

    fun getDocumentFlow(id: Long): Flow<DocumentEntity?> = documentDao.getDocumentByIdFlow(id)

    suspend fun createDocument(
        title: String,
        content: String = "",
        extension: String = "txt",
        syntax: String = "plaintext",
        folderId: Long? = null,
        tags: String = ""
    ): Long {
        val words = countWords(content)
        val entity = DocumentEntity(
            title = title,
            content = content,
            extension = extension,
            syntax = syntax,
            folderId = folderId,
            tags = tags,
            wordCount = words,
            charCount = content.length,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return documentDao.insertDocument(entity)
    }

    suspend fun updateDocument(document: DocumentEntity) {
        val words = countWords(document.content)
        val updated = document.copy(
            wordCount = words,
            charCount = document.content.length,
            updatedAt = System.currentTimeMillis()
        )
        documentDao.updateDocument(updated)
    }

    suspend fun togglePin(id: Long) = documentDao.togglePin(id)

    suspend fun toggleFavorite(id: Long) = documentDao.toggleFavorite(id)

    suspend fun duplicateDocument(doc: DocumentEntity): Long {
        val newDoc = doc.copy(
            id = 0,
            title = "${doc.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return documentDao.insertDocument(newDoc)
    }

    suspend fun moveToTrash(id: Long) = documentDao.moveToTrash(id)

    suspend fun restoreFromTrash(id: Long) = documentDao.restoreFromTrash(id)

    suspend fun deletePermanently(id: Long) {
        documentDao.deletePermanently(id)
        versionDao.deleteVersionsForDocument(id)
    }

    suspend fun emptyTrash() = documentDao.emptyTrash()

    suspend fun createFolder(name: String, colorHex: String = "#2563EB"): Long {
        return folderDao.insertFolder(FolderEntity(name = name, colorHex = colorHex))
    }

    suspend fun createTag(name: String, colorHex: String = "#8B5CF6"): Long {
        return tagDao.insertTag(TagEntity(name = name, colorHex = colorHex))
    }

    suspend fun saveVersion(docId: Long, content: String) {
        versionDao.insertVersion(
            DocumentVersionEntity(
                documentId = docId,
                content = content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getVersions(docId: Long): Flow<List<DocumentVersionEntity>> {
        return versionDao.getVersionsForDocument(docId)
    }

    suspend fun importFromUri(uri: Uri): Long = withContext(Dispatchers.IO) {
        var fileName = "Imported Document"
        var extension = "txt"

        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    val full = it.getString(nameIndex)
                    if (!full.isNullOrBlank()) {
                        val dot = full.lastIndexOf('.')
                        if (dot > 0) {
                            fileName = full.substring(0, dot)
                            extension = full.substring(dot + 1)
                        } else {
                            fileName = full
                        }
                    }
                }
            }
        }

        val content = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    content.append(line).append("\n")
                    line = reader.readLine()
                }
            }
        }

        val syntax = when (extension.lowercase()) {
            "md", "markdown" -> "markdown"
            "kt", "java", "py", "js", "html", "css", "json", "sql", "xml" -> "code"
            else -> "plaintext"
        }

        createDocument(
            title = fileName,
            content = content.toString(),
            extension = extension,
            syntax = syntax
        )
    }

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = documentDao.getCount()
        if (count == 0) {
            val personalFolderId = folderDao.insertFolder(FolderEntity(name = "Personal", colorHex = "#2563EB"))
            val workFolderId = folderDao.insertFolder(FolderEntity(name = "Work", colorHex = "#10B981"))
            val codeFolderId = folderDao.insertFolder(FolderEntity(name = "Projects", colorHex = "#8B5CF6"))

            tagDao.insertTag(TagEntity(name = "Important", colorHex = "#EF4444"))
            tagDao.insertTag(TagEntity(name = "Notes", colorHex = "#F59E0B"))
            tagDao.insertTag(TagEntity(name = "Draft", colorHex = "#3B82F6"))

            createDocument(
                title = "Welcome to Salim",
                content = """# Welcome to Salim File Manager

Salim is your ultra-fast, modern, all-in-one file manager for Android.

### Features
- 🚀 **Full Device Storage Browser** with breadcrumbs and instant search
- 📂 **Organize with ease**: Copy, Cut, Paste, Rename, Delete, and Batch Selection
- 🗜️ **Zip / Unzip**: Compress files to `.zip` and extract archives on-the-go
- 🗑️ **Recycle Bin**: Recover accidentally deleted files with 1-tap restore
- ⭐ **Quick Bookmarks**: Pin important folders for fast navigation
- 🔍 **Detailed File Inspector**: File size, permissions, hashes, and dates
- 🎨 **Material 3 Design**: Smooth gestures, dynamic themes, and one-handed control!
""".trimIndent(),
                extension = "md",
                syntax = "markdown",
                folderId = personalFolderId,
                tags = "Important,Notes"
            )

            createDocument(
                title = "Salim Quick Guide",
                content = """Salim File Manager Quick Tips:
1. Tap the storage card on top to view internal storage breakdown.
2. Long press any file to enter multi-select mode.
3. Use the bottom navigation to switch between Files, Notes, Trash, and Settings.
4. Use the '+' button to quickly create a folder or empty file.
""".trimIndent(),
                extension = "txt",
                syntax = "plaintext",
                folderId = workFolderId,
                tags = "Notes"
            )
        }
    }

    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
    }
}

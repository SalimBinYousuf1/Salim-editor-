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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class DiffLine(
    val type: DiffType,
    val text: String,
    val oldLineNumber: Int? = null,
    val newLineNumber: Int? = null
)

enum class DiffType {
    UNCHANGED, ADDED, REMOVED
}

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val versionDao: DocumentVersionDao,
    private val context: Context
) {

    val allDocumentsFlow: Flow<List<DocumentEntity>> = documentDao.getAllActiveFlow()
    val trashedDocumentsFlow: Flow<List<DocumentEntity>> = documentDao.getTrashedFlow()
    val favoritesFlow: Flow<List<DocumentEntity>> = documentDao.getFavoritesFlow()
    val pinnedFlow: Flow<List<DocumentEntity>> = documentDao.getPinnedFlow()
    val foldersFlow: Flow<List<FolderEntity>> = folderDao.getAllFoldersFlow()
    val tagsFlow: Flow<List<TagEntity>> = tagDao.getAllTagsFlow()
    val recentDocumentsFlow: Flow<List<DocumentEntity>> = documentDao.getRecentDocuments(10)
    val totalDocumentsFlow: Flow<Int> = documentDao.countActive()
    val totalWordsFlow: Flow<Int?> = documentDao.sumWords()

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingDocs = documentDao.getAllActiveFlow().firstOrNull()
        if (existingDocs.isNullOrEmpty()) {
            val workFolderId = folderDao.insert(
                FolderEntity(name = "Productivity", colorHex = "#2563EB", icon = "briefcase")
            )
            val notesFolderId = folderDao.insert(
                FolderEntity(name = "Personal", colorHex = "#10B981", icon = "book")
            )
            folderDao.insert(
                FolderEntity(name = "Code & Scripts", colorHex = "#8B5CF6", icon = "code")
            )

            tagDao.insert(TagEntity(name = "Guide", colorHex = "#2563EB"))
            tagDao.insert(TagEntity(name = "Ideas", colorHex = "#F59E0B"))
            tagDao.insert(TagEntity(name = "Important", colorHex = "#EF4444"))

            // 1. Welcome to Salim guide
            val welcomeText = """# Welcome to Salim Text Editor
A silky, fluid, and distraction-free writing environment built for Android.

Salim fuses three software philosophies into one cohesive experience:
- **Apple**: Pure typography, intentional hierarchy, gestures, and continuity.
- **vivo**: Ergonomic one-handed thumb reach and subtle fluid interactions.
- **Samsung**: Multitasking, split-screen editor, external keyboard shortcuts, and document outline.

---

## ⚡ Quick Start Tips

1. **Gestures**:
   - Pinch with two fingers to zoom text size on the fly.
   - Use the bottom dynamic toolbar to quickly format Markdown, indent code, or undo/redo.
   - Long-press any document on the Home screen for deep actions.

2. **Keyboard Accessory Strip**:
   - The strip right above your keyboard gives you instant symbols like `(`, `)`, `{`, `}`, `[`, `]`, quotes, hashtags, and arrow navigation.

3. **Split Editor & Markdown Preview**:
   - Tap the split icon in the top toolbar to view formatted Markdown side-by-side or stacked!

4. **Document Continuity**:
   - All files can be opened or saved directly to external storage via Android's document picker (SAF).
   - Your cursor and scroll positions are automatically restored.

Happy writing!
"""
            createDocument(
                title = "Welcome to Salim",
                content = welcomeText,
                extension = "md",
                isPinned = true,
                folderId = workFolderId,
                tags = "Guide, Important",
                syntax = "markdown"
            )

            // 2. Quick Note
            val quickNote = """Daily Standup & Priorities:

- [x] Review UI hierarchy and typography scale
- [ ] Configure custom accent colors in Settings
- [ ] Test split-screen productivity mode
- [ ] Verify external keyboard shortcuts (Ctrl+S, Ctrl+Z, Ctrl+F)

"Simplicity is prerequisite for reliability." — Edsger W. Dijkstra
"""
            createDocument(
                title = "Quick Note & Checklist",
                content = quickNote,
                extension = "txt",
                isPinned = false,
                isFavorite = true,
                folderId = notesFolderId,
                tags = "Ideas",
                syntax = "plaintext"
            )

            // 3. Kotlin Code Snippet
            val codeSample = """package com.example.demo

/**
 * Functional Kotlin snippet demonstrating
 * clean flow and data transformations.
 */
data class TextStats(
    val words: Int,
    val characters: Int,
    val lines: Int
)

fun calculateMetrics(text: String): TextStats {
    val lines = text.lines().size
    val words = text.trim()
        .split(Regex("\\s+"))
        .count { it.isNotBlank() }
    val characters = text.length

    return TextStats(
        words = words,
        characters = characters,
        lines = lines
    )
}
"""
            createDocument(
                title = "SampleCode",
                content = codeSample,
                extension = "kt",
                isPinned = false,
                tags = "Code",
                syntax = "code"
            )
        }
    }

    fun searchDocuments(query: String): Flow<List<DocumentEntity>> {
        return documentDao.searchDocuments(query)
    }

    fun getByFolder(folderId: Long): Flow<List<DocumentEntity>> {
        return documentDao.getByFolderFlow(folderId)
    }

    suspend fun getDocument(id: Long): DocumentEntity? = withContext(Dispatchers.IO) {
        documentDao.getDocumentById(id)
    }

    fun getDocumentFlow(id: Long): Flow<DocumentEntity?> {
        return documentDao.getDocumentByIdFlow(id)
    }

    suspend fun createDocument(
        title: String,
        content: String = "",
        extension: String = "txt",
        folderId: Long? = null,
        tags: String = "",
        isPinned: Boolean = false,
        isFavorite: Boolean = false,
        filePath: String? = null,
        uriString: String? = null,
        syntax: String = "plaintext"
    ): Long = withContext(Dispatchers.IO) {
        val wordCount = countWords(content)
        val charCount = content.length
        val lineCount = if (content.isEmpty()) 1 else content.lines().size

        val doc = DocumentEntity(
            title = title.ifBlank { "Untitled" },
            content = content,
            extension = extension.lowercase(),
            folderId = folderId,
            tags = tags,
            isPinned = isPinned,
            isFavorite = isFavorite,
            filePath = filePath,
            uriString = uriString,
            wordCount = wordCount,
            charCount = charCount,
            lineCount = lineCount,
            syntax = syntax,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = documentDao.insert(doc)
        // Create initial version snapshot
        versionDao.insert(
            DocumentVersionEntity(
                documentId = id,
                content = content,
                note = "Initial creation"
            )
        )
        id
    }

    suspend fun updateDocument(
        document: DocumentEntity,
        createSnapshot: Boolean = false,
        snapshotNote: String = "Auto-save"
    ) = withContext(Dispatchers.IO) {
        val wordCount = countWords(document.content)
        val charCount = document.content.length
        val lineCount = if (document.content.isEmpty()) 1 else document.content.lines().size

        val updated = document.copy(
            wordCount = wordCount,
            charCount = charCount,
            lineCount = lineCount,
            updatedAt = System.currentTimeMillis()
        )
        documentDao.update(updated)

        // Write to external URI if attached
        if (updated.uriString != null) {
            try {
                val uri = Uri.parse(updated.uriString)
                context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                    stream.write(updated.content.toByteArray(Charsets.UTF_8))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (createSnapshot) {
            versionDao.insert(
                DocumentVersionEntity(
                    documentId = updated.id,
                    content = updated.content,
                    note = snapshotNote
                )
            )
        }
    }

    suspend fun duplicateDocument(doc: DocumentEntity): Long = withContext(Dispatchers.IO) {
        val copy = doc.copy(
            id = 0,
            title = "${doc.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        documentDao.insert(copy)
    }

    suspend fun moveToTrash(docId: Long) = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentById(docId) ?: return@withContext
        documentDao.update(
            doc.copy(
                isTrashed = true,
                trashedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun restoreFromTrash(docId: Long) = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentById(docId) ?: return@withContext
        documentDao.update(
            doc.copy(
                isTrashed = false,
                trashedTimestamp = null
            )
        )
    }

    suspend fun deletePermanently(docId: Long) = withContext(Dispatchers.IO) {
        versionDao.deleteVersionsForDocument(docId)
        documentDao.deleteById(docId)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        documentDao.emptyTrash()
    }

    suspend fun togglePin(docId: Long) = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentById(docId) ?: return@withContext
        documentDao.update(doc.copy(isPinned = !doc.isPinned))
    }

    suspend fun toggleFavorite(docId: Long) = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentById(docId) ?: return@withContext
        documentDao.update(doc.copy(isFavorite = !doc.isFavorite))
    }

    fun getVersions(docId: Long): Flow<List<DocumentVersionEntity>> {
        return versionDao.getVersionsForDocument(docId)
    }

    suspend fun createVersionSnapshot(docId: Long, note: String) = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentById(docId) ?: return@withContext
        versionDao.insert(
            DocumentVersionEntity(
                documentId = docId,
                content = doc.content,
                note = note
            )
        )
    }

    // Folders
    suspend fun createFolder(name: String, colorHex: String = "#2563EB"): Long = withContext(Dispatchers.IO) {
        folderDao.insert(FolderEntity(name = name, colorHex = colorHex))
    }

    suspend fun deleteFolder(folder: FolderEntity) = withContext(Dispatchers.IO) {
        folderDao.delete(folder)
    }

    // Tags
    suspend fun createTag(name: String, colorHex: String = "#8B5CF6"): Long = withContext(Dispatchers.IO) {
        tagDao.insert(TagEntity(name = name, colorHex = colorHex))
    }

    suspend fun deleteTag(tag: TagEntity) = withContext(Dispatchers.IO) {
        tagDao.delete(tag)
    }

    // Import from External URI via SAF
    suspend fun importFromUri(uri: Uri): Long = withContext(Dispatchers.IO) {
        var fileName = "Imported Document"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) ?: fileName
            }
        }

        val extension = fileName.substringAfterLast('.', "txt")
        val title = fileName.substringBeforeLast('.')

        val contentBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    contentBuilder.append(line).append("\n")
                }
            }
        }

        val text = contentBuilder.toString().removeSuffix("\n")
        val syntax = when (extension.lowercase()) {
            "md", "markdown" -> "markdown"
            "kt", "java", "py", "js", "ts", "html", "css", "json", "xml", "c", "cpp", "sql", "yaml", "yml" -> "code"
            else -> "plaintext"
        }

        createDocument(
            title = title,
            content = text,
            extension = extension,
            uriString = uri.toString(),
            syntax = syntax
        )
    }

    // Helper: Compute word count
    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }

    fun computeDiff(oldText: String, newText: String): List<DiffLine> {
        return Companion.computeDiff(oldText, newText)
    }

    companion object {
        fun computeDiff(oldText: String, newText: String): List<DiffLine> {
            val oldLines = oldText.lines()
            val newLines = newText.lines()
            val result = mutableListOf<DiffLine>()

            var i = 0
            var j = 0
            while (i < oldLines.size || j < newLines.size) {
                when {
                    i < oldLines.size && j < newLines.size && oldLines[i] == newLines[j] -> {
                        result.add(DiffLine(DiffType.UNCHANGED, oldLines[i], i + 1, j + 1))
                        i++
                        j++
                    }
                    j < newLines.size && (i >= oldLines.size || !oldLines.contains(newLines[j])) -> {
                        result.add(DiffLine(DiffType.ADDED, newLines[j], null, j + 1))
                        j++
                    }
                    i < oldLines.size -> {
                        result.add(DiffLine(DiffType.REMOVED, oldLines[i], i + 1, null))
                        i++
                    }
                }
            }
            return result
        }
    }
}

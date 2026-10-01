package com.example.data.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.util.FileCategory
import com.example.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

enum class FileSortOption(val displayName: String) {
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    DATE_DESC("Date (Newest first)"),
    DATE_ASC("Date (Oldest first)"),
    SIZE_DESC("Size (Largest first)"),
    SIZE_ASC("Size (Smallest first)"),
    TYPE("Type / Extension")
}

data class TrashedFileInfo(
    val file: File,
    val originalName: String,
    val originalPath: String,
    val trashedAt: Long,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val formattedSize: String
)

data class StorageInfo(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercentage: Float = 0f,
    val formattedTotal: String = "0 GB",
    val formattedFree: String = "0 GB",
    val formattedUsed: String = "0 GB"
)

data class FileItem(
    val file: File,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val formattedSize: String,
    val formattedDate: String,
    val category: FileCategory,
    val isHidden: Boolean,
    val childCount: Int = 0
)

class FileManagerRepository(
    private val context: Context,
    private val documentRepository: DocumentRepository
) {

    private val _storageInfo = MutableStateFlow(StorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val trashDir: File by lazy {
        File(context.filesDir, ".salim_trash").apply { if (!exists()) mkdirs() }
    }

    private val trashMetaFile: File by lazy {
        File(context.filesDir, ".salim_trash_meta.json")
    }

    fun initializeStorage() {
        refreshStorageInfo()
    }

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    fun getRootStorageDirectory(): File {
        val external = Environment.getExternalStorageDirectory()
        return if (external != null && external.exists() && (external.canRead() || hasStoragePermission())) {
            external
        } else {
            // Fallback to app external or internal storage
            context.getExternalFilesDir(null) ?: context.filesDir
        }
    }

    fun getDownloadsDirectory(): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return if (dir.exists()) dir else File(getRootStorageDirectory(), "Download").apply { mkdirs() }
    }

    fun getDocumentsDirectory(): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        return if (dir.exists()) dir else File(getRootStorageDirectory(), "Documents").apply { mkdirs() }
    }

    fun getPicturesDirectory(): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return if (dir.exists()) dir else File(getRootStorageDirectory(), "Pictures").apply { mkdirs() }
    }

    fun getMusicDirectory(): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        return if (dir.exists()) dir else File(getRootStorageDirectory(), "Music").apply { mkdirs() }
    }

    fun getDCIMDirectory(): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        return if (dir.exists()) dir else File(getRootStorageDirectory(), "DCIM").apply { mkdirs() }
    }

    fun refreshStorageInfo() {
        try {
            val root = getRootStorageDirectory()
            val stat = StatFs(root.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val percent = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) else 0f

            _storageInfo.value = StorageInfo(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                usedPercentage = percent,
                formattedTotal = FileUtils.formatFileSize(totalBytes),
                formattedFree = FileUtils.formatFileSize(freeBytes),
                formattedUsed = FileUtils.formatFileSize(usedBytes)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun listFiles(
        directory: File,
        showHidden: Boolean = false,
        category: FileCategory = FileCategory.ALL,
        sortOption: FileSortOption = FileSortOption.NAME_ASC,
        searchQuery: String = ""
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val rawFiles = directory.listFiles() ?: return@withContext emptyList()

        val items = rawFiles.mapNotNull { file ->
            val isHidden = file.isHidden || file.name.startsWith(".")
            if (!showHidden && isHidden) return@mapNotNull null

            val isDir = file.isDirectory
            val cat = FileUtils.getFileCategory(file)

            if (category != FileCategory.ALL && !isDir && cat != category) {
                return@mapNotNull null
            }

            if (searchQuery.isNotBlank() && !file.name.contains(searchQuery, ignoreCase = true)) {
                return@mapNotNull null
            }

            val size = if (isDir) 0L else file.length()
            val childCount = if (isDir) file.list()?.size ?: 0 else 0

            FileItem(
                file = file,
                name = file.name,
                path = file.absolutePath,
                isDirectory = isDir,
                sizeBytes = size,
                lastModified = file.lastModified(),
                formattedSize = if (isDir) "$childCount items" else FileUtils.formatFileSize(size),
                formattedDate = FileUtils.formatDate(file.lastModified()),
                category = cat,
                isHidden = isHidden,
                childCount = childCount
            )
        }

        // Directories first, then sort
        val sorted = items.sortedWith { a, b ->
            if (a.isDirectory != b.isDirectory) {
                if (a.isDirectory) -1 else 1
            } else {
                when (sortOption) {
                    FileSortOption.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                    FileSortOption.NAME_DESC -> b.name.compareTo(a.name, ignoreCase = true)
                    FileSortOption.DATE_DESC -> b.lastModified.compareTo(a.lastModified)
                    FileSortOption.DATE_ASC -> a.lastModified.compareTo(b.lastModified)
                    FileSortOption.SIZE_DESC -> b.sizeBytes.compareTo(a.sizeBytes)
                    FileSortOption.SIZE_ASC -> a.sizeBytes.compareTo(b.sizeBytes)
                    FileSortOption.TYPE -> {
                        val extA = FileUtils.getExtension(a.name)
                        val extB = FileUtils.getExtension(b.name)
                        val cmp = extA.compareTo(extB, ignoreCase = true)
                        if (cmp != 0) cmp else a.name.compareTo(b.name, ignoreCase = true)
                    }
                }
            }
        }
        sorted
    }

    suspend fun createFolder(parentDir: File, name: String): Result<File> = withContext(Dispatchers.IO) {
        val target = File(parentDir, name)
        if (target.exists()) {
            return@withContext Result.failure(Exception("A folder with that name already exists"))
        }
        if (target.mkdirs()) {
            Result.success(target)
        } else {
            Result.failure(Exception("Failed to create folder"))
        }
    }

    suspend fun createFile(parentDir: File, name: String, content: String = ""): Result<File> = withContext(Dispatchers.IO) {
        val target = File(parentDir, name)
        if (target.exists()) {
            return@withContext Result.failure(Exception("A file with that name already exists"))
        }
        try {
            target.parentFile?.mkdirs()
            target.writeText(content)
            Result.success(target)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rename(file: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
        val parent = file.parentFile ?: return@withContext Result.failure(Exception("Cannot rename root item"))
        val target = File(parent, newName)
        if (target.exists() && !file.name.equals(newName, ignoreCase = true)) {
            return@withContext Result.failure(Exception("Target name already exists"))
        }
        if (file.renameTo(target)) {
            Result.success(target)
        } else {
            Result.failure(Exception("Failed to rename item"))
        }
    }

    suspend fun copyFiles(sources: List<File>, destinationDir: File): Result<Int> = withContext(Dispatchers.IO) {
        var count = 0
        val destCanonical = destinationDir.canonicalPath
        for (src in sources) {
            val srcCanonical = src.canonicalPath
            if (destCanonical == srcCanonical || (src.isDirectory && destCanonical.startsWith("$srcCanonical/"))) {
                continue
            }
            var dest = File(destinationDir, src.name)
            if (dest.exists()) {
                val base = src.nameWithoutExtension
                val ext = src.extension
                val extSuffix = if (ext.isNotEmpty()) ".$ext" else ""
                dest = File(destinationDir, "$base (copy)$extSuffix")
            }
            if (FileUtils.copyFile(src, dest)) {
                count++
            }
        }
        Result.success(count)
    }

    suspend fun moveFiles(sources: List<File>, destinationDir: File): Result<Int> = withContext(Dispatchers.IO) {
        var count = 0
        val destCanonical = destinationDir.canonicalPath
        for (src in sources) {
            val srcCanonical = src.canonicalPath
            if (src.parentFile?.canonicalPath == destCanonical) {
                continue
            }
            if (src.isDirectory && destCanonical.startsWith("$srcCanonical/")) {
                continue
            }
            val dest = File(destinationDir, src.name)
            if (src.renameTo(dest)) {
                count++
            } else if (FileUtils.copyFile(src, dest)) {
                FileUtils.deleteRecursively(src)
                count++
            }
        }
        Result.success(count)
    }

    suspend fun deletePermanently(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        if (FileUtils.deleteRecursively(file)) {
            Result.success(true)
        } else {
            Result.failure(Exception("Failed to delete ${file.name}"))
        }
    }

    suspend fun moveToTrash(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val trashedName = "${System.currentTimeMillis()}_${file.name}"
            val target = File(trashDir, trashedName)

            // Save metadata for restore
            val meta = loadTrashMeta()
            meta.put(trashedName, JSONObject().apply {
                put("originalPath", file.absolutePath)
                put("originalName", file.name)
                put("trashedAt", System.currentTimeMillis())
                put("isDirectory", file.isDirectory)
                put("sizeBytes", file.length())
            })
            saveTrashMeta(meta)

            val moved = file.renameTo(target) || (FileUtils.copyFile(file, target) && FileUtils.deleteRecursively(file))
            if (moved) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to move to trash"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFileFromTrash(trashedFile: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val meta = loadTrashMeta()
            val itemMeta = meta.optJSONObject(trashedFile.name)
            val originalPath = itemMeta?.optString("originalPath") ?: return@withContext Result.failure(Exception("No metadata found for restoration"))

            val originalFile = File(originalPath)
            originalFile.parentFile?.mkdirs()

            val restored = trashedFile.renameTo(originalFile) || (FileUtils.copyFile(trashedFile, originalFile) && trashedFile.delete())
            if (restored) {
                meta.remove(trashedFile.name)
                saveTrashMeta(meta)
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to restore file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrashedFiles(): List<TrashedFileInfo> = withContext(Dispatchers.IO) {
        val meta = loadTrashMeta()
        val files = trashDir.listFiles() ?: return@withContext emptyList()
        files.map { file ->
            val itemMeta = meta.optJSONObject(file.name)
            val origName = itemMeta?.optString("originalName", file.name) ?: file.name
            val origPath = itemMeta?.optString("originalPath", file.absolutePath) ?: file.absolutePath
            val time = itemMeta?.optLong("trashedAt", file.lastModified()) ?: file.lastModified()
            val isDir = file.isDirectory
            val size = if (isDir) 0L else file.length()
            TrashedFileInfo(
                file = file,
                originalName = origName,
                originalPath = origPath,
                trashedAt = time,
                isDirectory = isDir,
                sizeBytes = size,
                formattedSize = if (isDir) "Folder" else FileUtils.formatFileSize(size)
            )
        }.sortedByDescending { it.trashedAt }
    }

    suspend fun emptyTrash(): Result<Boolean> = withContext(Dispatchers.IO) {
        trashDir.listFiles()?.forEach { FileUtils.deleteRecursively(it) }
        saveTrashMeta(JSONObject())
        Result.success(true)
    }

    suspend fun zipFiles(sources: List<File>, zipName: String, destinationDir: File): Result<File> = withContext(Dispatchers.IO) {
        val safeName = if (zipName.endsWith(".zip", ignoreCase = true)) zipName else "$zipName.zip"
        val zipFile = File(destinationDir, safeName)
        if (FileUtils.zipFiles(sources, zipFile)) {
            Result.success(zipFile)
        } else {
            Result.failure(Exception("Failed to create zip archive"))
        }
    }

    suspend fun unzipArchive(zipFile: File, destinationDir: File): Result<Boolean> = withContext(Dispatchers.IO) {
        if (FileUtils.unzip(zipFile, destinationDir)) {
            Result.success(true)
        } else {
            Result.failure(Exception("Failed to extract zip archive"))
        }
    }

    private fun loadTrashMeta(): JSONObject {
        return try {
            if (trashMetaFile.exists()) {
                JSONObject(trashMetaFile.readText())
            } else {
                JSONObject()
            }
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun saveTrashMeta(meta: JSONObject) {
        try {
            trashMetaFile.writeText(meta.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

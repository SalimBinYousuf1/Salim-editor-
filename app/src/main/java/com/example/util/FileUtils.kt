package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

enum class FileCategory(val displayName: String) {
    ALL("All Files"),
    IMAGE("Images"),
    VIDEO("Videos"),
    AUDIO("Audio"),
    DOCUMENT("Documents"),
    APK("APKs"),
    ARCHIVE("Archives"),
    OTHER("Other")
}

object FileUtils {

    fun getExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex >= 0 && dotIndex < fileName.length - 1) {
            fileName.substring(dotIndex + 1).lowercase(Locale.ROOT)
        } else {
            ""
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[index]
    }

    fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
        return format.format(Date(timestamp))
    }

    fun getMimeType(file: File): String {
        if (file.isDirectory) return "resource/folder"
        val ext = getExtension(file.name)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        return mime ?: when (ext) {
            "json" -> "application/json"
            "md", "markdown" -> "text/markdown"
            "kt" -> "text/x-kotlin"
            "apk" -> "application/vnd.android.package-archive"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            "pdf" -> "application/pdf"
            "txt", "log", "cfg", "ini", "properties" -> "text/plain"
            else -> "*/*"
        }
    }

    fun getFileCategory(file: File): FileCategory {
        if (file.isDirectory) return FileCategory.OTHER
        val ext = getExtension(file.name)
        return when (ext) {
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic" -> FileCategory.IMAGE
            "mp4", "mkv", "avi", "mov", "webm", "3gp", "flv", "wmv" -> FileCategory.VIDEO
            "mp3", "wav", "m4a", "aac", "ogg", "flac", "opus", "wma" -> FileCategory.AUDIO
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "json", "rtf", "odt" -> FileCategory.DOCUMENT
            "apk", "aab", "apks" -> FileCategory.APK
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz" -> FileCategory.ARCHIVE
            else -> FileCategory.OTHER
        }
    }

    fun isTextOrCode(file: File): Boolean {
        if (file.isDirectory) return false
        val ext = getExtension(file.name)
        val textExtensions = setOf(
            "txt", "md", "markdown", "json", "xml", "html", "htm", "css", "js", "ts",
            "kt", "java", "py", "c", "cpp", "h", "cs", "sql", "sh", "yaml", "yml",
            "properties", "gradle", "kts", "env", "log", "conf", "ini", "csv", "svg"
        )
        return textExtensions.contains(ext)
    }

    fun isArchive(file: File): Boolean {
        return getExtension(file.name).lowercase() in listOf("zip", "jar")
    }

    fun copyFile(source: File, target: File): Boolean {
        return try {
            if (source.isDirectory) {
                copyDirectory(source, target)
            } else {
                target.parentFile?.mkdirs()
                source.inputStream().use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun copyDirectory(source: File, target: File): Boolean {
        return try {
            if (!target.exists()) target.mkdirs()
            source.listFiles()?.forEach { file ->
                val destChild = File(target, file.name)
                if (file.isDirectory) {
                    copyDirectory(file, destChild)
                } else {
                    file.inputStream().use { inStream ->
                        destChild.outputStream().use { outStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteRecursively(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    deleteRecursively(child)
                }
            }
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun countItems(directory: File): Pair<Int, Long> {
        var count = 0
        var totalSize = 0L
        fun traverse(dir: File) {
            dir.listFiles()?.forEach { item ->
                count++
                if (item.isDirectory) {
                    traverse(item)
                } else {
                    totalSize += item.length()
                }
            }
        }
        traverse(directory)
        return Pair(count, totalSize)
    }

    fun calculateHash(file: File, algorithm: String = "MD5"): String {
        return try {
            val digest = MessageDigest.getInstance(algorithm)
            val buffer = ByteArray(8192)
            FileInputStream(file).use { fis ->
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            val hashBytes = digest.digest()
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Unavailable"
        }
    }

    fun zipFiles(files: List<File>, zipFile: File): Boolean {
        return try {
            zipFile.parentFile?.mkdirs()
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                for (file in files) {
                    addFileToZip(file, file.name, zos)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun addFileToZip(file: File, entryPath: String, zos: ZipOutputStream) {
        if (file.isDirectory) {
            val dirEntry = if (entryPath.endsWith("/")) entryPath else "$entryPath/"
            zos.putNextEntry(ZipEntry(dirEntry))
            zos.closeEntry()
            file.listFiles()?.forEach { child ->
                addFileToZip(child, "$entryPath/${child.name}", zos)
            }
        } else {
            zos.putNextEntry(ZipEntry(entryPath))
            BufferedInputStream(FileInputStream(file)).use { bis ->
                bis.copyTo(zos)
            }
            zos.closeEntry()
        }
    }

    fun unzip(zipFile: File, destinationDir: File): Boolean {
        return try {
            if (!destinationDir.exists()) destinationDir.mkdirs()
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val file = File(destinationDir, entry.name)
                    // Security check against Zip Slip vulnerability
                    if (!file.canonicalPath.startsWith(destinationDir.canonicalPath)) {
                        throw SecurityException("Zip Slip detected: ${entry.name}")
                    }
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        BufferedOutputStream(FileOutputStream(file)).use { bos ->
                            zis.copyTo(bos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openWithIntent(context: Context, file: File) {
        try {
            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }
            val mimeType = getMimeType(file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Open with...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareFile(context: Context, file: File) {
        try {
            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }
            val mimeType = getMimeType(file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share file").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareMultipleFiles(context: Context, files: List<File>) {
        if (files.isEmpty()) return
        if (files.size == 1) {
            shareFile(context, files.first())
            return
        }
        try {
            val uris = ArrayList<Uri>()
            for (file in files) {
                try {
                    uris.add(FileProvider.getUriForFile(context, "${context.packageName}.provider", file))
                } catch (e: Exception) {
                    uris.add(Uri.fromFile(file))
                }
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share ${files.size} files").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

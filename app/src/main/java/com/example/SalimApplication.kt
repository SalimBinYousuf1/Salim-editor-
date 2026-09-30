package com.example

import android.app.Application
import com.example.data.local.SalimDatabase
import com.example.data.repository.DocumentRepository
import com.example.data.repository.EditorPreferencesRepository
import com.example.data.repository.FileManagerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SalimApplication : Application() {

    lateinit var database: SalimDatabase
        private set

    lateinit var documentRepository: DocumentRepository
        private set

    lateinit var editorPreferencesRepository: EditorPreferencesRepository
        private set

    lateinit var fileManagerRepository: FileManagerRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = SalimDatabase.getDatabase(this)

        documentRepository = DocumentRepository(
            documentDao = database.documentDao(),
            folderDao = database.folderDao(),
            tagDao = database.tagDao(),
            versionDao = database.documentVersionDao(),
            context = this
        )

        fileManagerRepository = FileManagerRepository(
            context = this,
            documentRepository = documentRepository
        )

        editorPreferencesRepository = EditorPreferencesRepository(this)

        applicationScope.launch {
            documentRepository.seedInitialDataIfNeeded()
            fileManagerRepository.initializeStorage()
        }
    }

    companion object {
        lateinit var instance: SalimApplication
            private set
    }
}

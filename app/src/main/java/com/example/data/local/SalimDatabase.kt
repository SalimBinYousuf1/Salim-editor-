package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DocumentDao
import com.example.data.local.dao.DocumentVersionDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.dao.TagDao
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.DocumentVersionEntity
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.TagEntity

@Database(
    entities = [
        DocumentEntity::class,
        FolderEntity::class,
        TagEntity::class,
        DocumentVersionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SalimDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun documentVersionDao(): DocumentVersionDao

    companion object {
        @Volatile
        private var INSTANCE: SalimDatabase? = null

        fun getDatabase(context: Context): SalimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalimDatabase::class.java,
                    "salim_editor.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

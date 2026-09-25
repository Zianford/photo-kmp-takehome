package com.example.photos.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(entities = [UploadRecordEntity::class], version = 1, exportSchema = true)
@ConstructedBy(PhotoDatabaseConstructor::class)
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun uploadRecordDao(): UploadRecordDao
}

@Suppress("KotlinNoActualForExpect")
expect object PhotoDatabaseConstructor : RoomDatabaseConstructor<PhotoDatabase> {
    override fun initialize(): PhotoDatabase
}

fun buildPhotoDatabase(builder: RoomDatabase.Builder<PhotoDatabase>): PhotoDatabase =
    builder.setDriver(BundledSQLiteDriver()).build()

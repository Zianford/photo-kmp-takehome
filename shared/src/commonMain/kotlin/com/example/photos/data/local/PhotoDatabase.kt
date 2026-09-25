package com.example.photos.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(
    entities = [UploadRecordEntity::class, CachedPhotoEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@ConstructedBy(PhotoDatabaseConstructor::class)
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun uploadRecordDao(): UploadRecordDao
    abstract fun cachedPhotoDao(): CachedPhotoDao
}

@Suppress("KotlinNoActualForExpect")
expect object PhotoDatabaseConstructor : RoomDatabaseConstructor<PhotoDatabase> {
    override fun initialize(): PhotoDatabase
}

fun buildPhotoDatabase(builder: RoomDatabase.Builder<PhotoDatabase>): PhotoDatabase =
    builder.setDriver(BundledSQLiteDriver()).build()

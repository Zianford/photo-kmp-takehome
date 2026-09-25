package com.example.photos.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import com.example.photos.data.local.PhotoDatabase
import com.example.photos.domain.model.Photo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomUploadHistoryRepositoryTest {
    @Test
    fun versionOneUploadHistorySurvivesPhotoCacheMigration() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val name = "photo-cache-migration-test.db"
            context.deleteDatabase(name)
            val path = context.getDatabasePath(name).absolutePath
            SQLiteDatabase.openOrCreateDatabase(path, null).use { legacy ->
                legacy.execSQL(
                    """CREATE TABLE upload_records (
                        fingerprint TEXT NOT NULL PRIMARY KEY,
                        clientUploadId TEXT NOT NULL,
                        photoId TEXT,
                        photoWidth INTEGER,
                        photoHeight INTEGER,
                        photoCreatedAtMillis INTEGER,
                        thumbnailUrl TEXT,
                        previewUrl TEXT,
                        imageUrl TEXT,
                        completedAtMillis INTEGER
                    )"""
                )
                legacy.execSQL("CREATE UNIQUE INDEX index_upload_records_clientUploadId ON upload_records(clientUploadId)")
                legacy.execSQL("INSERT INTO upload_records (fingerprint, clientUploadId) VALUES ('old', 'stable-id')")
                legacy.version = 1
            }

            val database = Room.databaseBuilder<PhotoDatabase>(context, path)
                .setDriver(AndroidSQLiteDriver())
                .build()
            try {
                val history = RoomUploadHistoryRepository(database.uploadRecordDao())
                assertEquals("stable-id", history.reserve("old", "new-id").clientUploadId)
                assertEquals(emptyList(), database.cachedPhotoDao().all())
            } finally {
                database.close()
                context.deleteDatabase(name)
            }
        }
    }

    @Test
    fun cachedPhotoMetadataSurvivesDatabaseReopen() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val name = "cached-photos-test.db"
            context.deleteDatabase(name)

            fun openDatabase() = Room.databaseBuilder<PhotoDatabase>(
                context,
                context.getDatabasePath(name).absolutePath,
            ).setDriver(AndroidSQLiteDriver()).build()

            var database = openDatabase()
            try {
                RoomPhotoSnapshotStore(database.cachedPhotoDao()).replace(listOf(photo()))
                database.close()
                database = openDatabase()
                assertEquals(listOf(photo()), RoomPhotoSnapshotStore(database.cachedPhotoDao()).photos())
            } finally {
                database.close()
                context.deleteDatabase(name)
            }
        }
    }

    @Test
    fun reservationAndCompletedPhotoSurviveDatabaseReopen() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val name = "upload-history-test.db"
            context.deleteDatabase(name)

            fun openDatabase() = Room.databaseBuilder<PhotoDatabase>(
                context,
                context.getDatabasePath(name).absolutePath,
            ).setDriver(AndroidSQLiteDriver()).build()

            var database = openDatabase()
            try {
                val first = RoomUploadHistoryRepository(database.uploadRecordDao())
                val pending = first.reserve("same-content", "upload-id-1")
                assertEquals("upload-id-1", pending.clientUploadId)
                assertNull(pending.photo)

                database.close()
                database = openDatabase()
                val reopened = RoomUploadHistoryRepository(database.uploadRecordDao())
                val retry = reopened.reserve("same-content", "upload-id-2")
                assertEquals("upload-id-1", retry.clientUploadId)
                assertNull(retry.photo)

                reopened.complete("same-content", photo())
                database.close()
                database = openDatabase()
                val completed = RoomUploadHistoryRepository(database.uploadRecordDao())
                val history = completed.observeCompleted().first()
                assertEquals(1, history.size)
                assertEquals("photo-1", history.single().photo.id)
                assertEquals("upload-id-1", history.single().clientUploadId)
                assertNotNull(completed.reserve("same-content", "upload-id-3").photo)
            } finally {
                database.close()
                context.deleteDatabase(name)
            }
        }
    }

    private fun photo() = Photo(
        id = "photo-1",
        width = 100,
        height = 80,
        createdAt = Instant.fromEpochMilliseconds(1_000),
        thumbnailUrl = "thumbnail",
        previewUrl = "preview",
        imageUrl = "image",
    )
}

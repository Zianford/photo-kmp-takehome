package com.example.photos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UploadRecordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(record: UploadRecordEntity): Long

    @Query("SELECT * FROM upload_records WHERE fingerprint = :fingerprint")
    abstract suspend fun find(fingerprint: String): UploadRecordEntity?

    @Query("SELECT * FROM upload_records WHERE completedAtMillis IS NOT NULL ORDER BY completedAtMillis DESC")
    abstract fun observeCompleted(): Flow<List<UploadRecordEntity>>

    @Query(
        """UPDATE upload_records SET
            photoId = :photoId,
            photoWidth = :photoWidth,
            photoHeight = :photoHeight,
            photoCreatedAtMillis = :photoCreatedAtMillis,
            thumbnailUrl = :thumbnailUrl,
            previewUrl = :previewUrl,
            imageUrl = :imageUrl,
            completedAtMillis = :completedAtMillis
            WHERE fingerprint = :fingerprint"""
    )
    abstract suspend fun complete(
        fingerprint: String,
        photoId: String,
        photoWidth: Int,
        photoHeight: Int,
        photoCreatedAtMillis: Long,
        thumbnailUrl: String,
        previewUrl: String,
        imageUrl: String,
        completedAtMillis: Long,
    ): Int

    @Transaction
    open suspend fun reserve(candidate: UploadRecordEntity): UploadRecordEntity {
        insert(candidate)
        return requireNotNull(find(candidate.fingerprint))
    }
}

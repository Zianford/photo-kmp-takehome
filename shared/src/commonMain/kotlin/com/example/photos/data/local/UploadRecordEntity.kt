package com.example.photos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "upload_records",
    indices = [Index(value = ["clientUploadId"], unique = true)],
)
data class UploadRecordEntity(
    @PrimaryKey val fingerprint: String,
    val clientUploadId: String,
    val photoId: String? = null,
    val photoWidth: Int? = null,
    val photoHeight: Int? = null,
    val photoCreatedAtMillis: Long? = null,
    val thumbnailUrl: String? = null,
    val previewUrl: String? = null,
    val imageUrl: String? = null,
    val completedAtMillis: Long? = null,
)

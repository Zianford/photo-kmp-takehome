package com.example.photos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.photos.domain.model.Photo
import kotlin.time.Instant

@Entity(tableName = "cached_photos")
data class CachedPhotoEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val width: Int,
    val height: Int,
    val createdAtMillis: Long,
    val thumbnailUrl: String,
    val previewUrl: String,
    val imageUrl: String,
)

fun Photo.toCachedEntity(position: Int) = CachedPhotoEntity(
    id = id,
    position = position,
    width = width,
    height = height,
    createdAtMillis = createdAt.toEpochMilliseconds(),
    thumbnailUrl = thumbnailUrl,
    previewUrl = previewUrl,
    imageUrl = imageUrl,
)

fun CachedPhotoEntity.toDomain() = Photo(
    id = id,
    width = width,
    height = height,
    createdAt = Instant.fromEpochMilliseconds(createdAtMillis),
    thumbnailUrl = thumbnailUrl,
    previewUrl = previewUrl,
    imageUrl = imageUrl,
)

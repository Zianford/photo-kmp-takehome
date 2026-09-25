package com.example.photos.domain.model

import kotlin.time.Instant

data class Photo(
    val id: String,
    val width: Int,
    val height: Int,
    val createdAt: Instant,
    val thumbnailUrl: String,
    val imageUrl: String,
)

data class PhotoPage(
    val photos: List<Photo>,
    val nextCursor: String?,
)

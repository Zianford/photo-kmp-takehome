@file:OptIn(ExperimentalTime::class)

package com.example.photos.api

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Photo(
    val id: String,
    val width: Int,
    val height: Int,
    val createdAt: Instant,
) {
    /** A tiny version of the image (a few KB) that loads fast; useful as a placeholder. */
    val thumbnailUrl: String get() = imageUrl(THUMBNAIL_WIDTH)

    /** The server resizes on request; ask for roughly the pixel width you'll display. */
    fun imageUrl(targetWidth: Int): String {
        val targetHeight = (targetWidth.toLong() * height / width).toInt().coerceAtLeast(1)
        return "https://picsum.photos/seed/$id/$targetWidth/$targetHeight"
    }

    private companion object {
        const val THUMBNAIL_WIDTH = 32
    }
}

data class PhotoPage(
    val photos: List<Photo>,
    val nextCursor: String?,
)

sealed interface UploadEvent {
    data class Progress(val fraction: Float) : UploadEvent
    data class Completed(val photo: Photo) : UploadEvent
}

/**
 * [retryable] is true for transient failures (timeouts, dropped connections, 503s)
 * and false when retrying the same request cannot succeed (e.g. the file is too large).
 */
class ApiException(message: String, val retryable: Boolean) : Exception(message)

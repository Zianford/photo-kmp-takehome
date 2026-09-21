package com.example.photos.api

import kotlinx.coroutines.flow.Flow

/**
 * Remote photo service.
 *
 * Every call behaves like real network I/O: it may be slow, and it may fail at any
 * time with [ApiException]. Callers should not assume the happy path.
 */
interface PhotoApi {

    /**
     * Returns one page of the feed, newest first.
     *
     * Pass the previous page's [PhotoPage.nextCursor] to continue; a null cursor starts
     * from the top. Cursors stay valid when new photos are added to the feed, so paging
     * never skips or repeats items.
     */
    suspend fun feed(cursor: String? = null, pageSize: Int = 30): PhotoPage

    /**
     * Uploads a photo, emitting [UploadEvent.Progress] updates followed by a single
     * [UploadEvent.Completed]. The flow may fail partway through with [ApiException].
     *
     * [clientUploadId] makes uploads idempotent: uploading an ID that has already
     * succeeded completes immediately with the existing photo instead of creating
     * a duplicate. Generate it on the client and reuse it for retries.
     */
    fun upload(clientUploadId: String, bytes: ByteArray): Flow<UploadEvent>
}

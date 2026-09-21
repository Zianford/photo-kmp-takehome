@file:OptIn(ExperimentalTime::class)

package com.example.photos.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

/**
 * In-memory stand-in for the real photo service. State lives only as long as this
 * instance: when the app process dies, uploads disappear from the feed and upload
 * dedup is forgotten. A [Photo] returned from an upload stays valid, though; its image
 * URLs keep working across launches.
 *
 * Uploaded bytes are not stored or served back; a completed upload is represented by a
 * placeholder image.
 *
 * @param latency range of simulated round-trip delay per request.
 * @param failureRate probability (0.0–1.0) that any given request fails.
 * @param uploadBytesPerSecond simulated upload bandwidth.
 * @param seed fixes the sequence of random delays and failures, for reproducing a bug.
 */
class FakePhotoApi(
    private val latency: ClosedRange<Duration> = 300.milliseconds..1500.milliseconds,
    private val failureRate: Double = 0.15,
    private val uploadBytesPerSecond: Long = 2_000_000,
    seed: Long? = null,
) : PhotoApi {

    private val random = seed?.let { Random(it) } ?: Random.Default
    private val mutex = Mutex()
    private val photos: MutableList<Photo> = seedCatalog().toMutableList()
    private val completedUploads = mutableMapOf<String, Photo>()

    override suspend fun feed(cursor: String?, pageSize: Int): PhotoPage {
        require(pageSize in 1..100) { "pageSize must be between 1 and 100" }
        simulateRoundTrip()
        return mutex.withLock {
            val start = if (cursor == null) {
                0
            } else {
                val index = photos.indexOfFirst { it.id == cursor }
                if (index == -1) throw ApiException("400 Bad Request: unknown cursor", retryable = false)
                index + 1
            }
            val page = photos.drop(start).take(pageSize)
            val hasMore = start + page.size < photos.size
            PhotoPage(page, nextCursor = if (hasMore) page.last().id else null)
        }
    }

    override fun upload(clientUploadId: String, bytes: ByteArray): Flow<UploadEvent> = flow {
        mutex.withLock { completedUploads[clientUploadId] }?.let {
            delay(roll { nextDuration(latency) })
            emit(UploadEvent.Completed(it))
            return@flow
        }
        if (bytes.size > MAX_UPLOAD_BYTES) {
            delay(roll { nextDuration(latency) })
            throw ApiException("413 Payload Too Large", retryable = false)
        }

        // Decide up front whether (and where) this attempt drops, so failures land mid-transfer.
        val failAt: Float? = roll { if (nextDouble() < failureRate) nextDouble(0.05, 0.95).toFloat() else null }
        val transferTime = (bytes.size * 1000L / uploadBytesPerSecond).milliseconds
            .coerceAtLeast(MIN_TRANSFER_TIME)
        val stepDelay = transferTime / PROGRESS_STEPS

        delay(roll { nextDuration(latency) } / 2)
        for (step in 1..PROGRESS_STEPS) {
            delay(stepDelay)
            val fraction = step.toFloat() / PROGRESS_STEPS
            if (failAt != null && fraction >= failAt) {
                throw ApiException("Connection reset during upload", retryable = true)
            }
            emit(UploadEvent.Progress(fraction))
        }
        delay(roll { nextDuration(latency) } / 2)

        val photo = mutex.withLock {
            completedUploads.getOrPut(clientUploadId) {
                Photo(
                    id = "upload-$clientUploadId",
                    width = 1000,
                    height = 1000,
                    createdAt = Clock.System.now(),
                ).also { photos.add(0, it) }
            }
        }
        emit(UploadEvent.Completed(photo))
    }

    private suspend fun simulateRoundTrip() {
        delay(roll { nextDuration(latency) })
        val failure = roll { if (nextDouble() < failureRate) TRANSIENT_FAILURES.random(this) else null }
        if (failure != null) throw ApiException(failure, retryable = true)
    }

    // kotlin.random.Random isn't thread-safe, and seeded runs need a stable draw order.
    private suspend fun <T> roll(block: Random.() -> T): T = mutex.withLock { random.block() }

    private fun Random.nextDuration(range: ClosedRange<Duration>): Duration =
        nextLong(range.start.inWholeMilliseconds, range.endInclusive.inWholeMilliseconds + 1).milliseconds

    private companion object {
        const val MAX_UPLOAD_BYTES = 25 * 1024 * 1024
        const val PROGRESS_STEPS = 20
        const val CATALOG_SIZE = 500
        const val CATALOG_SEED = 42
        val MIN_TRANSFER_TIME = 800.milliseconds
        val TRANSIENT_FAILURES = listOf("Request timed out", "503 Service Unavailable", "Connection reset")

        // Common camera aspect ratios, landscape and portrait.
        val DIMENSIONS = listOf(4032 to 3024, 3024 to 4032, 4000 to 4000, 1920 to 1080, 1080 to 1920, 3000 to 2000)

        fun seedCatalog(): List<Photo> {
            val catalogRandom = Random(CATALOG_SEED)
            val newest = Clock.System.now()
            return List(CATALOG_SIZE) { index ->
                val (width, height) = DIMENSIONS.random(catalogRandom)
                Photo(
                    id = "p${index + 1}",
                    width = width,
                    height = height,
                    createdAt = newest - (index * 3).hours,
                )
            }
        }
    }
}

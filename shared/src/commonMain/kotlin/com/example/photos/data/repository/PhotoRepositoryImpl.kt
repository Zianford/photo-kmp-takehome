package com.example.photos.data.repository

import com.example.photos.api.PhotoApi
import com.example.photos.data.mapper.toDomain
import com.example.photos.domain.model.PhotoPage
import com.example.photos.domain.repository.PhotoRepository
import kotlinx.coroutines.CancellationException

class PhotoRepositoryImpl(
    private val api: PhotoApi,
    private val snapshots: PhotoSnapshotStore,
) : PhotoRepository {
    override suspend fun feed(cursor: String?, pageSize: Int): PhotoPage {
        val page = try {
            api.feed(cursor, pageSize).toDomain()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            if (cursor != null) throw error
            val cached = snapshots.photos()
            if (cached.isEmpty()) throw error
            return PhotoPage(cached, nextCursor = null)
        }

        if (cursor == null) {
            val cached = try {
                snapshots.photos()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                emptyList()
            }
            val remoteIds = page.photos.mapTo(mutableSetOf()) { it.id }
            val merged = page.photos + cached.filter { remoteIds.add(it.id) }
            saveSnapshot {
                snapshots.replace(merged)
            }
            return page.copy(photos = merged)
        }

        saveSnapshot { snapshots.append(page.photos) }
        return page
    }

    private suspend fun saveSnapshot(save: suspend () -> Unit) {
        try {
            save()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            return
        }
    }
}

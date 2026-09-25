package com.example.photos.data.repository

import com.example.photos.data.local.CachedPhotoDao
import com.example.photos.data.local.toDomain
import com.example.photos.domain.model.Photo

interface PhotoSnapshotStore {
    suspend fun photos(): List<Photo>
    suspend fun replace(photos: List<Photo>)
    suspend fun append(photos: List<Photo>)
}

class RoomPhotoSnapshotStore(private val dao: CachedPhotoDao) : PhotoSnapshotStore {
    override suspend fun photos(): List<Photo> = dao.all().map { it.toDomain() }
    override suspend fun replace(photos: List<Photo>) = dao.replace(photos)
    override suspend fun append(photos: List<Photo>) = dao.append(photos)
}

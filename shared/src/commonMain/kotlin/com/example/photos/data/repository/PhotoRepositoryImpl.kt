package com.example.photos.data.repository

import com.example.photos.api.PhotoApi
import com.example.photos.data.mapper.toDomain
import com.example.photos.domain.model.PhotoPage
import com.example.photos.domain.repository.PhotoRepository

class PhotoRepositoryImpl(private val api: PhotoApi) : PhotoRepository {
    override suspend fun feed(cursor: String?, pageSize: Int): PhotoPage =
        api.feed(cursor, pageSize).toDomain()
}

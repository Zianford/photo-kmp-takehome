package com.example.photos.domain.repository

import com.example.photos.domain.model.PhotoPage

interface PhotoRepository {
    suspend fun feed(cursor: String? = null, pageSize: Int = 30): PhotoPage
}

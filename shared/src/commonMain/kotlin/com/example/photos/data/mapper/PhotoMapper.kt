package com.example.photos.data.mapper

import com.example.photos.api.Photo as ApiPhoto
import com.example.photos.api.PhotoPage as ApiPhotoPage
import com.example.photos.domain.model.Photo
import com.example.photos.domain.model.PhotoPage

internal fun ApiPhoto.toDomain(): Photo = Photo(
    id = id,
    width = width,
    height = height,
    createdAt = createdAt,
    thumbnailUrl = thumbnailUrl,
    imageUrl = imageUrl(width),
)

internal fun ApiPhotoPage.toDomain(): PhotoPage = PhotoPage(
    photos = photos.map(ApiPhoto::toDomain),
    nextCursor = nextCursor,
)

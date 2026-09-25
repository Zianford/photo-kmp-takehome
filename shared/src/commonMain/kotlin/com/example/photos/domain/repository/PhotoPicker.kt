package com.example.photos.domain.repository

import com.example.photos.domain.model.PickedPhoto

fun interface PhotoPicker {
    suspend fun pickPhotos(): List<PickedPhoto>
}

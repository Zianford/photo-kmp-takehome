package com.example.photos.data.repository

import com.example.photos.domain.usecase.ContentHasher
import com.example.photos.domain.usecase.UploadIdFactory
import okio.ByteString.Companion.toByteString
import kotlin.uuid.Uuid

class Sha256ContentHasher : ContentHasher {
    override fun sha256(bytes: ByteArray): String = bytes.toByteString().sha256().hex()
}

class RandomUploadIdFactory : UploadIdFactory {
    override fun create(): String = Uuid.random().toString()
}

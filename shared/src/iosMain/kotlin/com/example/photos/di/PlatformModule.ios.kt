package com.example.photos.di

import androidx.room.Room
import com.example.photos.data.local.PhotoDatabase
import com.example.photos.data.local.buildPhotoDatabase
import com.example.photos.domain.repository.PhotoPicker
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun platformModule(picker: PhotoPicker) = module {
    single<PhotoPicker> { picker }
    single {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val path = requireNotNull(directory?.path) + "/photo_history.db"
        buildPhotoDatabase(Room.databaseBuilder<PhotoDatabase>(name = path))
    }
}

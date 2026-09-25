package com.example.photos.di

import android.content.Context
import androidx.room.Room
import com.example.photos.data.local.PhotoDatabase
import com.example.photos.data.local.buildPhotoDatabase
import com.example.photos.domain.repository.PhotoPicker
import org.koin.dsl.module

fun platformModule(context: Context, picker: PhotoPicker) = module {
    single<PhotoPicker> { picker }
    single {
        val appContext = context.applicationContext
        val path = appContext.getDatabasePath("photo_history.db").absolutePath
        buildPhotoDatabase(Room.databaseBuilder<PhotoDatabase>(appContext, path))
    }
}

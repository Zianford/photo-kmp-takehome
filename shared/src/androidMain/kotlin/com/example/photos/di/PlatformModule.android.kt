package com.example.photos.di

import android.content.Context
import androidx.room.Room
import com.example.photos.data.local.PhotoDatabase
import com.example.photos.data.local.buildPhotoDatabase
import org.koin.dsl.module

fun platformModule(context: Context) = module {
    single {
        val appContext = context.applicationContext
        val path = appContext.getDatabasePath("photo_history.db").absolutePath
        buildPhotoDatabase(Room.databaseBuilder<PhotoDatabase>(appContext, path))
    }
}

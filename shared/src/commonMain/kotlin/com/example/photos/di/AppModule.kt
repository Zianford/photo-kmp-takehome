package com.example.photos.di

import com.example.photos.api.FakePhotoApi
import com.example.photos.api.PhotoApi
import com.example.photos.data.repository.PhotoRepositoryImpl
import com.example.photos.domain.repository.PhotoRepository
import com.example.photos.ui.features.feed.FeedViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<PhotoApi> { FakePhotoApi() }
    single<PhotoRepository> { PhotoRepositoryImpl(get()) }
    viewModelOf(::FeedViewModel)
}

package com.example.photos.di

import com.example.photos.api.FakePhotoApi
import com.example.photos.api.PhotoApi
import com.example.photos.data.repository.PhotoRepositoryImpl
import com.example.photos.data.repository.PhotoUploadRepositoryImpl
import com.example.photos.data.repository.RoomUploadHistoryRepository
import com.example.photos.data.repository.Sha256ContentHasher
import com.example.photos.data.repository.RandomUploadIdFactory
import com.example.photos.data.local.PhotoDatabase
import com.example.photos.domain.repository.PhotoRepository
import com.example.photos.domain.repository.PhotoUploadRepository
import com.example.photos.domain.repository.UploadHistoryRepository
import com.example.photos.domain.usecase.ContentHasher
import com.example.photos.domain.usecase.UploadIdFactory
import com.example.photos.domain.usecase.PrepareUploadUseCase
import com.example.photos.domain.usecase.UploadPhotoUseCase
import com.example.photos.ui.features.feed.FeedViewModel
import com.example.photos.ui.features.uploads.UploadViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<PhotoApi> { FakePhotoApi() }
    single<PhotoRepository> { PhotoRepositoryImpl(get()) }
    single<PhotoUploadRepository> { PhotoUploadRepositoryImpl(get()) }
    single { get<PhotoDatabase>().uploadRecordDao() }
    single<UploadHistoryRepository> { RoomUploadHistoryRepository(get()) }
    single<ContentHasher> { Sha256ContentHasher() }
    single<UploadIdFactory> { RandomUploadIdFactory() }
    factory { PrepareUploadUseCase(get(), get(), get()) }
    factory { UploadPhotoUseCase(get(), get(), get()) }
    viewModelOf(::FeedViewModel)
    viewModelOf(::UploadViewModel)
}

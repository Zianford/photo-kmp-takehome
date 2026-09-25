package com.example.photos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.photos.domain.model.Photo

@Dao
abstract class CachedPhotoDao {
    @Query("SELECT * FROM cached_photos ORDER BY position ASC")
    abstract suspend fun all(): List<CachedPhotoEntity>

    @Query("SELECT COALESCE(MAX(position), -1) FROM cached_photos")
    abstract suspend fun lastPosition(): Int

    @Query("DELETE FROM cached_photos")
    abstract suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(photos: List<CachedPhotoEntity>)

    @Transaction
    open suspend fun replace(photos: List<Photo>) {
        clear()
        insert(photos.mapIndexed { index, photo -> photo.toCachedEntity(index) })
    }

    @Transaction
    open suspend fun append(photos: List<Photo>) {
        val firstPosition = lastPosition() + 1
        insert(photos.mapIndexed { index, photo -> photo.toCachedEntity(firstPosition + index) })
    }
}

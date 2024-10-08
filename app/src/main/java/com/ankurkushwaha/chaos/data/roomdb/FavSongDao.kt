package com.ankurkushwaha.chaos.data.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ankurkushwaha.chaos.data.model.FavSong
import kotlinx.coroutines.flow.Flow


@Dao
interface FavSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteSong(song: FavSong)

    @Delete
    suspend fun deleteFavoriteSong(song: FavSong)

    @Query("SELECT * FROM favorite_songs")
    fun getAllFavoriteSongs(): Flow<List<FavSong>>
}
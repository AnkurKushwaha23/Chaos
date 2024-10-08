package com.ankurkushwaha.chaos.data.repository

import com.ankurkushwaha.chaos.data.model.FavSong
import com.ankurkushwaha.chaos.data.roomdb.FavSongDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class FavSongRepository @Inject constructor(
    private val favSongDao: FavSongDao
) {
    // Insert a song into the favorites
    suspend fun addFavoriteSong(song: FavSong) {
        favSongDao.insertFavoriteSong(song)
    }

    // Delete a song from the favorites
    suspend fun removeFavoriteSong(song: FavSong) {
        favSongDao.deleteFavoriteSong(song)
    }

    // Get all favorite songs
    fun getAllFavoriteSongs(): Flow<List<FavSong>> {
        return favSongDao.getAllFavoriteSongs()
    }
}
package com.ankurkushwaha.chaos.data.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ankurkushwaha.chaos.data.model.FavSong
import com.ankurkushwaha.chaos.data.model.Playlist
import com.ankurkushwaha.chaos.data.model.PlaylistSongCrossRef
import com.ankurkushwaha.chaos.data.model.Song


@Database(
    entities = [Song::class, FavSong::class, Playlist::class, PlaylistSongCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class SongDatabase : RoomDatabase() {
    abstract fun favSongDao(): FavSongDao
    abstract fun playlistDao(): PlaylistDao
}
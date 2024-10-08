package com.ankurkushwaha.chaos.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavSong(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist") val artist: String?,
    @ColumnInfo(name = "album") val album: String,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "image_uri") val imageUri: String?
)

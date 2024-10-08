package com.ankurkushwaha.chaos.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,  // Embedded Playlist object
    @Relation(
        parentColumn = "playlistId",           // References the primary key of Playlist
        entityColumn = "id",           // References the primary key of Song
        associateBy = Junction(PlaylistSongCrossRef::class) // Junction to link Playlist and Song
    )
    val songs: List<Song> // List of Songs in the Playlist
)

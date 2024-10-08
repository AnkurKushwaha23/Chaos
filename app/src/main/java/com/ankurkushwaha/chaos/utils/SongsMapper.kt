package com.ankurkushwaha.chaos.utils

import com.ankurkushwaha.chaos.data.model.FavSong
import com.ankurkushwaha.chaos.data.model.Song

// Extension function to convert Song to FavSong
fun Song.toFavSong(): FavSong {
    return FavSong(
        id = this.id,
        title = this.title,
        artist = this.artist,
        album = this.album,
        duration = this.duration,
        path = this.path,
        imageUri = this.imageUri
    )
}

// Extension function to convert FavSong to Song
fun FavSong.toSong(isFavorite: Boolean): Song {
    return Song(
        id = this.id,
        title = this.title,
        artist = this.artist,
        album = this.album,
        duration = this.duration,
        path = this.path,
        imageUri = this.imageUri,
        isFavorite = isFavorite
    )
}

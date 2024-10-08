package com.ankurkushwaha.chaos.data.repository

import com.ankurkushwaha.chaos.data.model.Playlist
import com.ankurkushwaha.chaos.data.model.PlaylistSongCrossRef
import com.ankurkushwaha.chaos.data.model.PlaylistWithSongs
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.data.roomdb.PlaylistDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    // Insert a new playlist
    suspend fun insertPlaylist(playlist: Playlist): Long {
        return playlistDao.insertPlaylist(playlist)
    }

    // Remove a song from a playlist
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    // Get all playlists
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()


    // Get songs for a specific playlist using Flow
    fun getSongsForPlaylist(playlistId: Long): Flow<PlaylistWithSongs> {
        return playlistDao.getSongsForPlaylist(playlistId)
    }

    suspend fun getAllPlaylistCrossRef(): List<PlaylistSongCrossRef> {
        return playlistDao.getAllPlaylistSongCrossRefs()
    }

    suspend fun addSingleSongToPlaylist(playlistId: Long, song: Song) {
        addSong(song)
        addSongToPlaylist(playlistId, song.id)
    }

    //add song in songs table
    private suspend fun addSong(song: Song) {
        playlistDao.insertSongs(song)
    }

    // Add a song to a playlist
    private suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val crossRef = PlaylistSongCrossRef(playlistId = playlistId, id = songId)
        playlistDao.insertSongToPlaylist(crossRef)
    }

    // Delete a playlist
    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }
}

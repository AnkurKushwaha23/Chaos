package com.ankurkushwaha.chaos.data.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ankurkushwaha.chaos.data.model.Playlist
import com.ankurkushwaha.chaos.data.model.PlaylistSongCrossRef
import com.ankurkushwaha.chaos.data.model.PlaylistWithSongs
import com.ankurkushwaha.chaos.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    // Corrected function to fetch songs for a specific playlist
    @Transaction
    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    fun getSongsForPlaylist(playlistId: Long): Flow<PlaylistWithSongs>

    @Query("SELECT * FROM song_playlist_cross_ref")
    suspend fun getAllPlaylistSongCrossRefs(): List<PlaylistSongCrossRef>

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM song_playlist_cross_ref WHERE playlistId = :playlistId AND id = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM songs WHERE id = :songId)")
    suspend fun isSongExists(songId: Long): Boolean

}

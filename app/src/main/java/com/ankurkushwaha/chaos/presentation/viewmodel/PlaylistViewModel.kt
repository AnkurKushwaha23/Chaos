package com.ankurkushwaha.chaos.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ankurkushwaha.chaos.data.model.Playlist
import com.ankurkushwaha.chaos.data.model.PlaylistWithSongs
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val repository: PlaylistRepository
) : ViewModel() {
    // LiveData to observe all playlists
    val allPlaylists: LiveData<List<Playlist>> = repository.allPlaylists.asLiveData()

    // LiveData to observe songs in a specific playlist
    private val _playlistWithSongs = MutableLiveData<PlaylistWithSongs>()
    val playlistWithSongs: LiveData<PlaylistWithSongs> = _playlistWithSongs

    // Create a new playlist
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val playlist = Playlist(name = name)
            repository.insertPlaylist(playlist)
        }
    }

    // Add a song to a playlist
    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSingleSongToPlaylist(playlistId, song)
        }
    }

    // Remove a song from a playlist
    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    // Fetch songs for a specific playlist
    fun fetchSongsForPlaylist(playlistId: Long) {
        viewModelScope.launch {
            // Fetch the playlist with songs
            repository.getSongsForPlaylist(playlistId).collect { playlistWithSongs ->
                playlistWithSongs.songs.forEach { song ->
                    val file = File(song.path)
                    if (!file.exists()) {
                        // Song file doesn't exist, remove from playlist
                        repository.removeSongFromPlaylist(playlistId, song.id)
                    }
                }
                // Update the playlist with remaining valid songs
                _playlistWithSongs.postValue(playlistWithSongs)
            }
        }
    }

    // Delete a playlist
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun fetchCrossRef() {
        viewModelScope.launch {
            val crossRefs = repository.getAllPlaylistCrossRef()
            Log.d("CrossRefs", crossRefs.toString())
        }
    }
}

// Fetch songs for a specific playlist
//    fun fetchSongsForPlaylist(playlistId: Long) {
//        viewModelScope.launch {
//            repository.getSongsForPlaylist(playlistId).collect { playlistWithSongs ->
//                _playlistWithSongs.postValue(playlistWithSongs)
//            }
//        }
//    }
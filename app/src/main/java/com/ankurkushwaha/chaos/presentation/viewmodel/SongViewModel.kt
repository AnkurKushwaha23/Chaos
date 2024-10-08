package com.ankurkushwaha.chaos.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankurkushwaha.chaos.data.model.FavSong
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.data.repository.FavSongRepository
import com.ankurkushwaha.chaos.domain.repository.MusicRepository
import com.ankurkushwaha.chaos.utils.toFavSong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val favSongRepository: FavSongRepository
) : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _filteredSongs = MutableLiveData<List<Song>>()
    val filteredSongs: LiveData<List<Song>> = _filteredSongs

    /** Retrieve favorite songs from RoomDB */
    val favSongsList: StateFlow<List<FavSong>> = favSongRepository.getAllFavoriteSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchFavSongList()
    }

    // Fetch songs from MediaStore and merge with favorite status
    private fun fetchFavSongList() {
        viewModelScope.launch {
            // Fetch all songs from MediaStore
            val allSongs = repository.getAllSongs()

            // Collect favorite songs flow
            favSongRepository.getAllFavoriteSongs().collect { favoriteSongs ->
                favoriteSongs.forEach { favSong ->
                    // Check if the song file exists on the device
                    val file = File(favSong.path)
                    if (!file.exists()) {
                        // If the song file doesn't exist, remove it from the favorites
                        favSongRepository.removeFavoriteSong(favSong)
                    }
                }

                // Merge songs with favorite status
                val mergedList = allSongs.map { song ->
                    val isFavorite = favoriteSongs.any { fav -> fav.id == song.id }
                    song.copy(isFavorite = isFavorite)
                }

                _songs.postValue(mergedList)
            }
        }
    }

    // Add a song to favorites
    fun addFavorite(song: Song) {
        viewModelScope.launch {
            // Convert Song to FavSong and save to RoomDB
            val favSong = song.toFavSong()
            favSongRepository.addFavoriteSong(favSong)
        }
    }

    // Remove a song from favorites
    fun removeFavorite(song: Song) {
        viewModelScope.launch {
            // Convert Song to FavSong and remove from RoomDB
            val favSong = song.toFavSong()
            favSongRepository.removeFavoriteSong(favSong)
        }
    }

    fun searchSongs(query: String) {
        val allSongs = _songs.value ?: return

        val filteredList = if (query.isNotEmpty()) {
            allSongs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                        (song.artist?.contains(query, ignoreCase = true) == true) ||
                        song.album.contains(query, ignoreCase = true)
            }
        } else {
            allSongs  // If the query is empty, return the full list
        }
        _filteredSongs.postValue(filteredList)
    }
}

package com.ankurkushwaha.chaos.presentation.adapter

import com.ankurkushwaha.chaos.data.model.Song

interface OnSongMenuClickListener {
    fun onSongClick(song: Song)  // Handles the song item click
    fun onPlayNext(song: Song)   // Handles play next action
    fun onAddToPlaylist(song: Song)  // Handles add to playlist action
    fun onDetails(song: Song)    // Handles song details action
}


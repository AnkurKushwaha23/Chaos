package com.ankurkushwaha.chaos.presentation.adapter

import com.ankurkushwaha.chaos.data.model.Playlist

interface OnPlaylistMenuClickListener {
    fun onPlaylistClick(playlist: Playlist)
    fun onRemoveClick(playlist: Playlist)
}
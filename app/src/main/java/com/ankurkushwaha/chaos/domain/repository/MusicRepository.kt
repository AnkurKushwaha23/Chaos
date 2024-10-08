package com.ankurkushwaha.chaos.domain.repository

import com.ankurkushwaha.chaos.data.model.Song

interface MusicRepository {
    suspend fun getAllSongs(): List<Song>
}
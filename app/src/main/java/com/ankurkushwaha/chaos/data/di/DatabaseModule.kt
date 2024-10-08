package com.ankurkushwaha.chaos.data.di

import android.app.Application
import androidx.room.Room
import com.ankurkushwaha.chaos.data.roomdb.FavSongDao
import com.ankurkushwaha.chaos.data.roomdb.PlaylistDao
import com.ankurkushwaha.chaos.data.roomdb.SongDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        application: Application
    ): SongDatabase {
        return Room
            .databaseBuilder(
                application,
                SongDatabase::class.java,
                "Chaos_music_player.db"
            ).build()
    }

    @Provides
    @Singleton
    fun provideFavDao(database: SongDatabase): FavSongDao {
        return database.favSongDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: SongDatabase): PlaylistDao {
        return database.playlistDao()
    }
}
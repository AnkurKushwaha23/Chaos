package com.ankurkushwaha.chaos.data.di

import android.content.Context
import com.ankurkushwaha.chaos.data.repository.MusicRepositoryImpl
import com.ankurkushwaha.chaos.domain.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext app: Context): Context = app

    @Singleton
    @Provides
    fun provideMusicRepository(musicRepositoryImpl: MusicRepositoryImpl): MusicRepository {
        return musicRepositoryImpl
    }

}
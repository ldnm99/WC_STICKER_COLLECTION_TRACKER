package com.example.wc2026stickers.di

import android.content.Context
import androidx.room.Room
import com.example.wc2026stickers.data.db.AppDatabase
import com.example.wc2026stickers.data.db.dao.StickerDao
import com.example.wc2026stickers.data.db.dao.TeamDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wc2026_stickers.db")
            .build()

    @Provides
    fun provideTeamDao(db: AppDatabase): TeamDao = db.teamDao()

    @Provides
    fun provideStickerDao(db: AppDatabase): StickerDao = db.stickerDao()
}

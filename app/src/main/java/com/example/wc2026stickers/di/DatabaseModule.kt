package com.wc2026stickers.app.di

import android.content.Context
import androidx.room.Room
import com.wc2026stickers.app.data.db.AppDatabase
import com.wc2026stickers.app.data.db.dao.StickerDao
import com.wc2026stickers.app.data.db.dao.TeamDao
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
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .addMigrations(AppDatabase.MIGRATION_4_5)
            .build()

    @Provides
    fun provideTeamDao(db: AppDatabase): TeamDao = db.teamDao()

    @Provides
    fun provideStickerDao(db: AppDatabase): StickerDao = db.stickerDao()
}

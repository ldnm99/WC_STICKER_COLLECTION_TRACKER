package com.wc2026stickers.app.di

import com.wc2026stickers.app.data.backup.AndroidCollectionBackupStorage
import com.wc2026stickers.app.data.backup.CollectionBackupStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    @Binds
    @Singleton
    abstract fun bindCollectionBackupStorage(
        impl: AndroidCollectionBackupStorage
    ): CollectionBackupStorage
}

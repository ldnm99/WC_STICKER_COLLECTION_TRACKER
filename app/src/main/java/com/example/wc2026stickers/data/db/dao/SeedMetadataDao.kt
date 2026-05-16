package com.wc2026stickers.app.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wc2026stickers.app.data.db.entities.SeedMetadata

@Dao
interface SeedMetadataDao {
    @Query("SELECT seedVersion FROM seed_metadata WHERE id = :id")
    suspend fun getSeedVersion(id: String): Int?

    @Upsert
    suspend fun upsertSeedMetadata(seedMetadata: SeedMetadata)
}

package com.wc2026stickers.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seed_metadata")
data class SeedMetadata(
    @PrimaryKey val id: String,
    val seedVersion: Int
)

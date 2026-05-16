package com.wc2026stickers.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey val code: String,        // e.g. "ARG", "FWC"
    val name: String,                    // e.g. "Argentina"
    val flagEmoji: String,               // e.g. "🇦🇷"
    val confederation: String,           // e.g. "CONMEBOL"
    val sortOrder: Int,                  // for display ordering
    val isFavorite: Boolean = false
)

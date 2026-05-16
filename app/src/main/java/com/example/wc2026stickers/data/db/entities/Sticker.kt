package com.wc2026stickers.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StickerType { BADGE, TEAM_PHOTO, PLAYER, SPECIAL }

@Entity(
    tableName = "stickers",
    foreignKeys = [ForeignKey(
        entity = Team::class,
        parentColumns = ["code"],
        childColumns = ["teamCode"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("teamCode")]
)
data class Sticker(
    @PrimaryKey val id: String,          // e.g. "ARG-1", "FWC-9"
    val teamCode: String,                // FK → Team.code
    val number: Int,                     // sticker number within team (1–20, or 1–19 for FWC)
    val label: String,                   // e.g. "Badge", "Team Photo", "Lionel Messi"
    val stickerType: StickerType,
    val isShiny: Boolean = false
)

package com.wc2026stickers.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_stickers",
    foreignKeys = [ForeignKey(
        entity = Sticker::class,
        parentColumns = ["id"],
        childColumns = ["stickerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class UserSticker(
    @PrimaryKey val stickerId: String,   // FK → Sticker.id
    val quantityOwned: Int = 0,          // 0=missing, 1=have it, 2+=duplicates
    val firstCollectedAt: Long? = null,
    val lastUpdatedAt: Long? = null
)

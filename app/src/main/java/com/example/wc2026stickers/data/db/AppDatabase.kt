package com.example.wc2026stickers.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wc2026stickers.data.db.dao.StickerDao
import com.example.wc2026stickers.data.db.dao.TeamDao
import com.example.wc2026stickers.data.db.entities.Sticker
import com.example.wc2026stickers.data.db.entities.Team
import com.example.wc2026stickers.data.db.entities.UserSticker

@Database(
    entities = [Team::class, Sticker::class, UserSticker::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
    abstract fun stickerDao(): StickerDao
}

package com.example.wc2026stickers.data.db

import androidx.room.TypeConverter
import com.example.wc2026stickers.data.db.entities.StickerType

class Converters {
    @TypeConverter
    fun fromStickerType(type: StickerType): String = type.name

    @TypeConverter
    fun toStickerType(value: String): StickerType = StickerType.valueOf(value)
}

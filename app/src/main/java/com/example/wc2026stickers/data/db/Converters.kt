package com.wc2026stickers.app.data.db

import androidx.room.TypeConverter
import com.wc2026stickers.app.data.db.entities.StickerType

class Converters {
    @TypeConverter
    fun fromStickerType(type: StickerType): String = type.name

    @TypeConverter
    fun toStickerType(value: String): StickerType = StickerType.valueOf(value)
}

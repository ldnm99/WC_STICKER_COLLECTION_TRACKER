package com.example.wc2026stickers.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.wc2026stickers.data.db.entities.Sticker
import com.example.wc2026stickers.data.db.entities.StickerType
import com.example.wc2026stickers.data.db.entities.UserSticker
import kotlinx.coroutines.flow.Flow

data class StickerWithQuantity(
    val id: String,
    val teamCode: String,
    val number: Int,
    val label: String,
    val stickerType: StickerType,
    val isShiny: Boolean,
    val quantityOwned: Int
)

@Dao
interface StickerDao {

    @Query("""
        SELECT s.id, s.teamCode, s.number, s.label, s.stickerType, s.isShiny,
               COALESCE(us.quantityOwned, 0) AS quantityOwned
        FROM stickers s
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE s.teamCode = :teamCode
        ORDER BY s.number ASC
    """)
    fun getStickersForTeam(teamCode: String): Flow<List<StickerWithQuantity>>

    @Query("""
        SELECT s.id, s.teamCode, s.number, s.label, s.stickerType, s.isShiny,
               COALESCE(us.quantityOwned, 0) AS quantityOwned
        FROM stickers s
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE COALESCE(us.quantityOwned, 0) = 0
        ORDER BY s.teamCode ASC, s.number ASC
    """)
    fun getMissingStickers(): Flow<List<StickerWithQuantity>>

    @Query("""
        SELECT s.id, s.teamCode, s.number, s.label, s.stickerType, s.isShiny,
               COALESCE(us.quantityOwned, 0) AS quantityOwned
        FROM stickers s
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE COALESCE(us.quantityOwned, 0) > 1
        ORDER BY s.teamCode ASC, s.number ASC
    """)
    fun getDuplicateStickers(): Flow<List<StickerWithQuantity>>

    @Query("""
        SELECT COUNT(*) FROM stickers s
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE COALESCE(us.quantityOwned, 0) > 0
    """)
    fun getCollectedCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM stickers s
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE COALESCE(us.quantityOwned, 0) = 0
    """)
    fun getMissingCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM stickers s
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE COALESCE(us.quantityOwned, 0) > 1
    """)
    fun getDuplicatesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM stickers")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSticker(sticker: Sticker)

    @Upsert
    suspend fun upsertUserSticker(userSticker: UserSticker)

    @Query("SELECT * FROM user_stickers WHERE stickerId = :stickerId")
    suspend fun getUserSticker(stickerId: String): UserSticker?

    /** Lookup a sticker by its display code e.g. "ARG1" → teamCode="ARG", number=1 */
    @Query("SELECT * FROM stickers WHERE teamCode = :teamCode AND number = :number")
    suspend fun findSticker(teamCode: String, number: Int): Sticker?
}

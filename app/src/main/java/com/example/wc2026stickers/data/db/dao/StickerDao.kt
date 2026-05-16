package com.wc2026stickers.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.wc2026stickers.app.data.db.entities.Sticker
import com.wc2026stickers.app.data.db.entities.StickerType
import com.wc2026stickers.app.data.db.entities.UserSticker
import kotlinx.coroutines.flow.Flow

data class StickerWithQuantity(
    val id: String,
    val teamCode: String,
    val teamName: String,
    val teamFlagEmoji: String,
    val confederation: String,
    val number: Int,
    val label: String,
    val stickerType: StickerType,
    val isShiny: Boolean,
    val quantityOwned: Int
)

data class StickerCollectionHistoryRecord(
    val stickerId: String,
    val teamCode: String,
    val teamName: String,
    val teamFlagEmoji: String,
    val number: Int,
    val label: String,
    val quantityOwned: Int,
    val firstCollectedAt: Long?,
    val lastUpdatedAt: Long?
)

@Dao
interface StickerDao {

    @Query("""
        SELECT s.id, s.teamCode, t.name AS teamName, t.flagEmoji AS teamFlagEmoji, t.confederation,
               s.number, s.label, s.stickerType, s.isShiny,
               COALESCE(us.quantityOwned, 0) AS quantityOwned
        FROM stickers s
        INNER JOIN teams t ON t.code = s.teamCode
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE s.teamCode = :teamCode
        ORDER BY s.number ASC
    """)
    fun getStickersForTeam(teamCode: String): Flow<List<StickerWithQuantity>>

    @Query("""
        SELECT s.id, s.teamCode, t.name AS teamName, t.flagEmoji AS teamFlagEmoji, t.confederation,
               s.number, s.label, s.stickerType, s.isShiny,
               COALESCE(us.quantityOwned, 0) AS quantityOwned
        FROM stickers s
        INNER JOIN teams t ON t.code = s.teamCode
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE COALESCE(us.quantityOwned, 0) = 0
        ORDER BY s.teamCode ASC, s.number ASC
    """)
    fun getMissingStickers(): Flow<List<StickerWithQuantity>>

    @Query("""
        SELECT s.id, s.teamCode, t.name AS teamName, t.flagEmoji AS teamFlagEmoji, t.confederation,
               s.number, s.label, s.stickerType, s.isShiny,
               COALESCE(us.quantityOwned, 0) AS quantityOwned
        FROM stickers s
        INNER JOIN teams t ON t.code = s.teamCode
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

    @Query("SELECT COUNT(*) FROM stickers")
    suspend fun getStickerCount(): Int

    @Query("UPDATE stickers SET label = :label WHERE id = :id")
    suspend fun updateStickerLabel(id: String, label: String)

    @Upsert
    suspend fun upsertUserSticker(userSticker: UserSticker)

    @Upsert
    suspend fun upsertUserStickers(userStickers: List<UserSticker>)

    @Query("SELECT * FROM user_stickers WHERE stickerId = :stickerId")
    suspend fun getUserSticker(stickerId: String): UserSticker?

    @Query("SELECT * FROM user_stickers WHERE quantityOwned > 0 ORDER BY stickerId ASC")
    suspend fun getOwnedUserStickers(): List<UserSticker>

    @Query("""
        SELECT s.id AS stickerId, s.teamCode, t.name AS teamName, t.flagEmoji AS teamFlagEmoji,
               s.number, s.label, us.quantityOwned, us.firstCollectedAt, us.lastUpdatedAt
        FROM user_stickers us
        INNER JOIN stickers s ON s.id = us.stickerId
        INNER JOIN teams t ON t.code = s.teamCode
        WHERE us.lastUpdatedAt IS NOT NULL
        ORDER BY us.lastUpdatedAt DESC, s.id ASC
        LIMIT :limit
    """)
    fun getRecentStickerHistory(limit: Int): Flow<List<StickerCollectionHistoryRecord>>

    @Query("""
        SELECT s.id AS stickerId, s.teamCode, t.name AS teamName, t.flagEmoji AS teamFlagEmoji,
               s.number, s.label, us.quantityOwned, us.firstCollectedAt, us.lastUpdatedAt
        FROM user_stickers us
        INNER JOIN stickers s ON s.id = us.stickerId
        INNER JOIN teams t ON t.code = s.teamCode
        WHERE us.quantityOwned > 0 AND us.firstCollectedAt IS NOT NULL
        ORDER BY us.firstCollectedAt ASC, s.id ASC
    """)
    fun getCollectedStickerHistory(): Flow<List<StickerCollectionHistoryRecord>>

    @Query("SELECT id FROM stickers")
    suspend fun getAllStickerIds(): List<String>

    @Query("DELETE FROM user_stickers")
    suspend fun clearUserStickers()

    /** Lookup a sticker by its display code e.g. "ARG1" → teamCode="ARG", number=1 */
    @Query("SELECT * FROM stickers WHERE teamCode = :teamCode AND number = :number")
    suspend fun findSticker(teamCode: String, number: Int): Sticker?

    /** Full-text search across sticker id, label, team code, and team name */
    @Query("""
        SELECT s.id, s.teamCode, t.name AS teamName, t.flagEmoji AS teamFlagEmoji, t.confederation,
               s.number, s.label, s.stickerType, s.isShiny,
               COALESCE(us.quantityOwned, 0) AS quantityOwned
        FROM stickers s
        INNER JOIN teams t ON t.code = s.teamCode
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE s.id LIKE :query OR s.label LIKE :query OR s.teamCode LIKE :query OR t.name LIKE :query
        ORDER BY s.teamCode ASC, s.number ASC
    """)
    fun searchStickers(query: String): Flow<List<StickerWithQuantity>>
}

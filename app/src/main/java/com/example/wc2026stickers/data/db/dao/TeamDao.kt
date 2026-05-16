package com.wc2026stickers.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wc2026stickers.app.data.db.entities.Team
import kotlinx.coroutines.flow.Flow

data class TeamWithProgress(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val confederation: String,
    val sortOrder: Int,
    val isFavorite: Boolean,
    val collectedCount: Int,
    val totalCount: Int
)

data class TeamKpiStats(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val confederation: String,
    val totalCount: Int,
    val collectedCount: Int,
    val duplicateStickerCount: Int,
    val duplicateExtraCount: Int,
    val badgeCollected: Int,      // 1 if badge sticker owned, 0 if missing
    val teamPhotoCollected: Int   // 1 if team photo sticker owned, 0 if missing
)

@Dao
interface TeamDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeam(team: Team)

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun getTeamCount(): Int

    @Query("""
        SELECT t.code, t.name, t.flagEmoji, t.confederation, t.sortOrder, t.isFavorite,
               COALESCE(SUM(CASE WHEN us.quantityOwned > 0 THEN 1 ELSE 0 END), 0) AS collectedCount,
               COUNT(s.id) AS totalCount
        FROM teams t
        LEFT JOIN stickers s ON s.teamCode = t.code
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        GROUP BY t.code
        ORDER BY t.sortOrder ASC
    """)
    fun getAllTeamsWithProgress(): Flow<List<TeamWithProgress>>

    @Query("""
        SELECT t.code, t.name, t.flagEmoji, t.confederation, t.sortOrder, t.isFavorite,
               COALESCE(SUM(CASE WHEN us.quantityOwned > 0 THEN 1 ELSE 0 END), 0) AS collectedCount,
               COUNT(s.id) AS totalCount
        FROM teams t
        LEFT JOIN stickers s ON s.teamCode = t.code
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        WHERE t.isFavorite = 1
        GROUP BY t.code
        ORDER BY t.sortOrder ASC
    """)
    fun getFavoriteTeamsWithProgress(): Flow<List<TeamWithProgress>>

    @Query("""
        SELECT t.code, t.name, t.flagEmoji, t.confederation,
               COUNT(s.id) AS totalCount,
               COALESCE(SUM(CASE WHEN COALESCE(us.quantityOwned, 0) > 0 THEN 1 ELSE 0 END), 0) AS collectedCount,
               COALESCE(SUM(CASE WHEN COALESCE(us.quantityOwned, 0) > 1 THEN 1 ELSE 0 END), 0) AS duplicateStickerCount,
               COALESCE(SUM(CASE WHEN COALESCE(us.quantityOwned, 0) > 1 THEN us.quantityOwned - 1 ELSE 0 END), 0) AS duplicateExtraCount,
               COALESCE(MAX(CASE WHEN s.stickerType = 'BADGE' AND COALESCE(us.quantityOwned, 0) > 0 THEN 1 ELSE 0 END), 0) AS badgeCollected,
               COALESCE(MAX(CASE WHEN s.stickerType = 'TEAM_PHOTO' AND COALESCE(us.quantityOwned, 0) > 0 THEN 1 ELSE 0 END), 0) AS teamPhotoCollected
        FROM teams t
        LEFT JOIN stickers s ON s.teamCode = t.code
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        GROUP BY t.code
        ORDER BY t.sortOrder ASC
    """)
    fun getAllTeamKpiStats(): Flow<List<TeamKpiStats>>

    @Query("SELECT * FROM teams WHERE code = :code")
    suspend fun getTeamByCode(code: String): Team?

    @Query("SELECT * FROM teams WHERE code = :code")
    fun observeTeamByCode(code: String): Flow<Team?>

    @Query("UPDATE teams SET isFavorite = :isFavorite WHERE code = :code")
    suspend fun setTeamFavorite(code: String, isFavorite: Boolean)
}

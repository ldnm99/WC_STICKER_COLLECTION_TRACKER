package com.example.wc2026stickers.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wc2026stickers.data.db.entities.Team
import kotlinx.coroutines.flow.Flow

data class TeamWithProgress(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val confederation: String,
    val sortOrder: Int,
    val collectedCount: Int,
    val totalCount: Int
)

@Dao
interface TeamDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTeam(team: Team)

    @Query("""
        SELECT t.code, t.name, t.flagEmoji, t.confederation, t.sortOrder,
               COALESCE(SUM(CASE WHEN us.quantityOwned > 0 THEN 1 ELSE 0 END), 0) AS collectedCount,
               COUNT(s.id) AS totalCount
        FROM teams t
        LEFT JOIN stickers s ON s.teamCode = t.code
        LEFT JOIN user_stickers us ON us.stickerId = s.id
        GROUP BY t.code
        ORDER BY t.sortOrder ASC
    """)
    fun getAllTeamsWithProgress(): Flow<List<TeamWithProgress>>

    @Query("SELECT * FROM teams WHERE code = :code")
    suspend fun getTeamByCode(code: String): Team?
}

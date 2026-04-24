package com.example.wc2026stickers.data.repository

import com.example.wc2026stickers.data.db.dao.StickerDao
import com.example.wc2026stickers.data.db.dao.StickerWithQuantity
import com.example.wc2026stickers.data.db.dao.TeamDao
import com.example.wc2026stickers.data.db.dao.TeamWithProgress
import com.example.wc2026stickers.data.db.entities.UserSticker
import com.example.wc2026stickers.data.seed.DatabaseSeeder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StickerRepository @Inject constructor(
    private val teamDao: TeamDao,
    private val stickerDao: StickerDao,
    private val seeder: DatabaseSeeder
) {
    suspend fun ensureSeeded() = seeder.seedIfNeeded()

    fun getAllTeamsWithProgress(): Flow<List<TeamWithProgress>> =
        teamDao.getAllTeamsWithProgress()

    fun getStickersForTeam(teamCode: String): Flow<List<StickerWithQuantity>> =
        stickerDao.getStickersForTeam(teamCode)

    fun getMissingStickers(): Flow<List<StickerWithQuantity>> =
        stickerDao.getMissingStickers()

    fun getDuplicateStickers(): Flow<List<StickerWithQuantity>> =
        stickerDao.getDuplicateStickers()

    fun getCollectedCount(): Flow<Int> = stickerDao.getCollectedCount()
    fun getMissingCount(): Flow<Int> = stickerDao.getMissingCount()
    fun getDuplicatesCount(): Flow<Int> = stickerDao.getDuplicatesCount()
    fun getTotalCount(): Flow<Int> = stickerDao.getTotalCount()

    suspend fun setQuantity(stickerId: String, quantity: Int) {
        stickerDao.upsertUserSticker(UserSticker(stickerId, quantity.coerceAtLeast(0)))
    }

    suspend fun getTeamByCode(code: String) = teamDao.getTeamByCode(code)

    /**
     * Increment a sticker's quantity by 1 (used by Quick Add).
     */
    suspend fun incrementSticker(stickerId: String) {
        val current = stickerDao.getUserSticker(stickerId)?.quantityOwned ?: 0
        stickerDao.upsertUserSticker(UserSticker(stickerId, current + 1))
    }

    /**
     * Parse a user-typed sticker ID like "ARG1", "MEX20", "FWC9".
     * Returns the canonical sticker id (e.g. "ARG-1") or null if not found.
     */
    suspend fun resolveStickerId(input: String): String? {
        val trimmed = input.trim().uppercase()
        // Match: letters = team code, digits = number
        val match = Regex("^([A-Z]{2,3})(\\d{1,2})$").find(trimmed) ?: return null
        val teamCode = match.groupValues[1]
        val number = match.groupValues[2].toIntOrNull() ?: return null
        return stickerDao.findSticker(teamCode, number)?.id
    }
}

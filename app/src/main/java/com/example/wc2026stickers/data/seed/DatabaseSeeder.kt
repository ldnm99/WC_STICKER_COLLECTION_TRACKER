package com.wc2026stickers.app.data.seed

import androidx.room.withTransaction
import com.wc2026stickers.app.data.db.AppDatabase
import com.wc2026stickers.app.data.db.dao.StickerDao
import com.wc2026stickers.app.data.db.dao.TeamDao
import com.wc2026stickers.app.data.db.entities.SeedMetadata
import com.wc2026stickers.app.data.db.entities.Sticker
import com.wc2026stickers.app.data.db.entities.StickerType
import com.wc2026stickers.app.data.db.entities.Team
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val teamDao: TeamDao,
    private val stickerDao: StickerDao,
    private val db: AppDatabase
) {
    suspend fun seedIfNeeded() {
        db.withTransaction {
            val seedPlan = SeedPlan.create(
                storedSeedVersion = db.seedMetadataDao().getSeedVersion(SEED_STATE_ID) ?: 0,
                currentTeamCount = teamDao.getTeamCount(),
                currentStickerCount = stickerDao.getStickerCount(),
                expectedTeamCount = SeedCatalog.expectedTeamCount,
                expectedStickerCount = SeedCatalog.expectedStickerCount,
                targetSeedVersion = SEED_VERSION
            )

            if (!seedPlan.requiresWork) {
                return@withTransaction
            }

            if (seedPlan.requiresBaseSeed) {
                seedTeams()
                seedStickers()
            }

            if (seedPlan.requiresLabelRefresh) {
                updateStickerLabels()
            }

            db.seedMetadataDao().upsertSeedMetadata(
                SeedMetadata(
                    id = SEED_STATE_ID,
                    seedVersion = SEED_VERSION
                )
            )
        }
    }

    private suspend fun updateStickerLabels() {
        SeedLabelCatalog.labels.forEach { (id, label) ->
            stickerDao.updateStickerLabel(id, label)
        }
    }

    private suspend fun seedTeams() {
        SeedCatalog.teams.forEach { (code, name, flag, conf, sort) ->
            teamDao.insertTeam(Team(code, name, flag, conf, sort))
        }
    }

    private suspend fun seedStickers() {
        // FWC special section (FWC-0 through FWC-19)
        for (num in 0..19) {
            val id = "FWC-$num"
            stickerDao.insertSticker(
                Sticker(
                    id = id,
                    teamCode = "FWC",
                    number = num,
                    label = SeedLabelCatalog.labels[id] ?: "FWC $num",
                    stickerType = StickerType.SPECIAL,
                    isShiny = true
                )
            )
        }

        // 48 team sections x 20 stickers = 960 stickers
        // Layout per team: 1=Badge (shiny), 2-12=Player 1-11, 13=Team Photo, 14-20=Player 12-18
        SeedCatalog.teams.forEach { (code, _, _, _, _) ->
            if (code == SeedCatalog.specialTeamCode) return@forEach
            for (num in 1..20) {
                val id = "$code-$num"
                val type = when (num) {
                    1 -> StickerType.BADGE
                    13 -> StickerType.TEAM_PHOTO
                    else -> StickerType.PLAYER
                }
                val fallbackLabel = when (num) {
                    1 -> "Badge"
                    13 -> "Team Photo"
                    else -> "Player ${if (num <= 12) num - 1 else num - 2}"
                }
                stickerDao.insertSticker(
                    Sticker(
                        id = id,
                        teamCode = code,
                        number = num,
                        label = SeedLabelCatalog.labels[id] ?: fallbackLabel,
                        stickerType = type,
                        isShiny = num == 1
                    )
                )
            }
        }
    }

    companion object {
        private const val SEED_VERSION = 3
        private const val SEED_STATE_ID = "catalog"
    }
}

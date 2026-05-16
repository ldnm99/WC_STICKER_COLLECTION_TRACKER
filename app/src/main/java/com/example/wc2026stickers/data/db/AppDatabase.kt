package com.wc2026stickers.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wc2026stickers.app.data.db.dao.SeedMetadataDao
import com.wc2026stickers.app.data.db.dao.StickerDao
import com.wc2026stickers.app.data.db.dao.TeamDao
import com.wc2026stickers.app.data.db.entities.SeedMetadata
import com.wc2026stickers.app.data.db.entities.Sticker
import com.wc2026stickers.app.data.db.entities.Team
import com.wc2026stickers.app.data.db.entities.UserSticker

@Database(
    entities = [Team::class, Sticker::class, UserSticker::class, SeedMetadata::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
    abstract fun stickerDao(): StickerDao
    abstract fun seedMetadataDao(): SeedMetadataDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Disable FK enforcement so we can rename team code safely
                db.execSQL("PRAGMA foreign_keys = OFF")

                // SAU → KSA: update user_stickers first (no team FK, only sticker FK)
                db.execSQL("UPDATE user_stickers SET stickerId = 'KSA-' || SUBSTR(stickerId, 5) WHERE stickerId LIKE 'SAU-%'")
                // Update stickers table (id and teamCode)
                db.execSQL("UPDATE stickers SET id = 'KSA-' || SUBSTR(id, 5), teamCode = 'KSA' WHERE teamCode = 'SAU'")
                // Update teams table
                db.execSQL("UPDATE teams SET code = 'KSA' WHERE code = 'SAU'")

                db.execSQL("PRAGMA foreign_keys = ON")

                // Fix team photo: was num=2, now num=13
                // num=2: Team Photo → Player 1
                db.execSQL("UPDATE stickers SET label = 'Player 1', stickerType = 'PLAYER' WHERE number = 2 AND teamCode != 'FWC'")
                // num=3–12: Player labels shift up by 1 (was num-2, now num-1)
                db.execSQL("UPDATE stickers SET label = 'Player ' || (number - 1), stickerType = 'PLAYER' WHERE number BETWEEN 3 AND 12 AND teamCode != 'FWC'")
                // num=13: Player 11 → Team Photo
                db.execSQL("UPDATE stickers SET label = 'Team Photo', stickerType = 'TEAM_PHOTO' WHERE number = 13 AND teamCode != 'FWC'")
                // num=14–20: Player 12–18 labels are unchanged (formula num-2 is the same)

                // Add FWC-0 Panini intro sticker
                db.execSQL("INSERT OR IGNORE INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('FWC-0', 'FWC', 0, 'Panini Intro', 'SPECIAL', 1)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `seed_metadata` (
                        `id` TEXT NOT NULL,
                        `seedVersion` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE teams
                    ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE user_stickers
                    ADD COLUMN firstCollectedAt INTEGER
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE user_stickers
                    ADD COLUMN lastUpdatedAt INTEGER
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE user_stickers
                    SET firstCollectedAt = CASE
                            WHEN quantityOwned > 0 THEN CAST(strftime('%s', 'now') AS INTEGER) * 1000
                            ELSE NULL
                        END,
                        lastUpdatedAt = CAST(strftime('%s', 'now') AS INTEGER) * 1000
                    """.trimIndent()
                )
            }
        }
    }
}

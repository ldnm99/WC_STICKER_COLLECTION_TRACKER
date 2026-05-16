package com.wc2026stickers.app.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wc2026stickers.app.data.db.entities.StickerType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @After
    fun tearDown() {
        targetContext.deleteDatabase(TEST_DB)
    }

    @Test
    fun migration1To2_updatesCatalogData() {
        createDatabase(version = 1, createSchema = ::createVersion2Schema) { db ->
            db.execSQL(
                "INSERT INTO teams (code, name, flagEmoji, confederation, sortOrder) VALUES ('SAU', 'Saudi Arabia', '🇸🇦', 'AFC', 1)"
            )
            db.execSQL(
                "INSERT INTO teams (code, name, flagEmoji, confederation, sortOrder) VALUES ('FWC', 'FIFA World Cup', '🏆', 'FIFA', 2)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('SAU-1', 'SAU', 1, 'Badge', 'BADGE', 1)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('SAU-2', 'SAU', 2, 'Team Photo', 'TEAM_PHOTO', 0)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('SAU-3', 'SAU', 3, 'Player 1', 'PLAYER', 0)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('SAU-13', 'SAU', 13, 'Player 11', 'PLAYER', 0)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('SAU-14', 'SAU', 14, 'Player 12', 'PLAYER', 0)"
            )
            db.execSQL(
                "INSERT INTO user_stickers (stickerId, quantityOwned) VALUES ('SAU-3', 2)"
            )
        }

        val migratedDb = openDatabase(
            version = 2,
            onUpgrade = { db, oldVersion, newVersion ->
                assertEquals(1, oldVersion)
                assertEquals(2, newVersion)
                AppDatabase.MIGRATION_1_2.migrate(db)
            }
        )

        migratedDb.use {
            assertFalse(it.hasRow("SELECT code FROM teams WHERE code = 'SAU'"))
            assertTrue(it.hasRow("SELECT code FROM teams WHERE code = 'KSA'"))
            assertEquals("KSA-3", it.singleString("SELECT stickerId FROM user_stickers"))
            assertSticker(it, number = 2, expectedId = "KSA-2", expectedLabel = "Player 1", expectedType = StickerType.PLAYER)
            assertSticker(it, number = 3, expectedId = "KSA-3", expectedLabel = "Player 2", expectedType = StickerType.PLAYER)
            assertSticker(it, number = 13, expectedId = "KSA-13", expectedLabel = "Team Photo", expectedType = StickerType.TEAM_PHOTO)
            assertSticker(it, number = 14, expectedId = "KSA-14", expectedLabel = "Player 12", expectedType = StickerType.PLAYER)
            assertEquals("SPECIAL", it.singleString("SELECT stickerType FROM stickers WHERE id = 'FWC-0'"))
            assertEquals(1, it.singleInt("SELECT isShiny FROM stickers WHERE id = 'FWC-0'"))
        }
    }

    @Test
    fun migration2To3_createsSeedMetadataTable() {
        createDatabase(version = 2, createSchema = ::createVersion2Schema) { db ->
            db.execSQL(
                "INSERT INTO teams (code, name, flagEmoji, confederation, sortOrder) VALUES ('ARG', 'Argentina', '🇦🇷', 'CONMEBOL', 1)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('ARG-1', 'ARG', 1, 'Badge', 'BADGE', 1)"
            )
            db.execSQL(
                "INSERT INTO user_stickers (stickerId, quantityOwned) VALUES ('ARG-1', 1)"
            )
        }

        val migratedDb = openDatabase(
            version = 3,
            onUpgrade = { db, oldVersion, newVersion ->
                assertEquals(2, oldVersion)
                assertEquals(3, newVersion)
                AppDatabase.MIGRATION_2_3.migrate(db)
            }
        )

        migratedDb.use {
            assertTrue(
                it.hasRow(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'seed_metadata'"
                )
            )
            assertTableColumn(it, tableName = "seed_metadata", columnName = "id", expectedType = "TEXT", expectedNotNull = 1, expectedPrimaryKey = 1)
            assertTableColumn(it, tableName = "seed_metadata", columnName = "seedVersion", expectedType = "INTEGER", expectedNotNull = 1, expectedPrimaryKey = 0)
            assertEquals("ARG-1", it.singleString("SELECT stickerId FROM user_stickers"))
            assertEquals(1, it.singleInt("SELECT COUNT(*) FROM teams WHERE code = 'ARG'"))
        }
    }

    @Test
    fun migration3To4_addsFavoriteFlagToTeams() {
        createDatabase(version = 3, createSchema = ::createVersion3Schema) { db ->
            db.execSQL(
                "INSERT INTO teams (code, name, flagEmoji, confederation, sortOrder) VALUES ('ARG', 'Argentina', '🇦🇷', 'CONMEBOL', 1)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('ARG-1', 'ARG', 1, 'Badge', 'BADGE', 1)"
            )
            db.execSQL(
                "INSERT INTO seed_metadata (id, seedVersion) VALUES ('catalog', 3)"
            )
        }

        val migratedDb = openDatabase(
            version = 4,
            onUpgrade = { db, oldVersion, newVersion ->
                assertEquals(3, oldVersion)
                assertEquals(4, newVersion)
                AppDatabase.MIGRATION_3_4.migrate(db)
            }
        )

        migratedDb.use {
            assertTableColumn(it, tableName = "teams", columnName = "isFavorite", expectedType = "INTEGER", expectedNotNull = 1, expectedPrimaryKey = 0)
            assertEquals(0, it.singleInt("SELECT isFavorite FROM teams WHERE code = 'ARG'"))
            assertEquals("catalog", it.singleString("SELECT id FROM seed_metadata"))
        }
    }

    @Test
    fun migration4To5_addsCollectionHistoryTimestamps() {
        createDatabase(version = 4, createSchema = ::createVersion4Schema) { db ->
            db.execSQL(
                "INSERT INTO teams (code, name, flagEmoji, confederation, sortOrder, isFavorite) VALUES ('ARG', 'Argentina', '🇦🇷', 'CONMEBOL', 1, 0)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('ARG-1', 'ARG', 1, 'Badge', 'BADGE', 1)"
            )
            db.execSQL(
                "INSERT INTO stickers (id, teamCode, number, label, stickerType, isShiny) VALUES ('ARG-2', 'ARG', 2, 'Player 1', 'PLAYER', 0)"
            )
            db.execSQL(
                "INSERT INTO user_stickers (stickerId, quantityOwned) VALUES ('ARG-1', 2)"
            )
            db.execSQL(
                "INSERT INTO user_stickers (stickerId, quantityOwned) VALUES ('ARG-2', 0)"
            )
        }

        val migratedDb = openDatabase(
            version = 5,
            onUpgrade = { db, oldVersion, newVersion ->
                assertEquals(4, oldVersion)
                assertEquals(5, newVersion)
                AppDatabase.MIGRATION_4_5.migrate(db)
            }
        )

        migratedDb.use {
            assertTableColumn(it, tableName = "user_stickers", columnName = "firstCollectedAt", expectedType = "INTEGER", expectedNotNull = 0, expectedPrimaryKey = 0)
            assertTableColumn(it, tableName = "user_stickers", columnName = "lastUpdatedAt", expectedType = "INTEGER", expectedNotNull = 0, expectedPrimaryKey = 0)
            assertTrue(it.singleLong("SELECT firstCollectedAt FROM user_stickers WHERE stickerId = 'ARG-1'") > 0L)
            assertTrue(it.singleLong("SELECT lastUpdatedAt FROM user_stickers WHERE stickerId = 'ARG-1'") > 0L)
            assertTrue(it.singleLong("SELECT lastUpdatedAt FROM user_stickers WHERE stickerId = 'ARG-2'") > 0L)
            assertTrue(it.singleNullableLong("SELECT firstCollectedAt FROM user_stickers WHERE stickerId = 'ARG-2'") == null)
        }
    }

    private fun createDatabase(
        version: Int,
        createSchema: (SupportSQLiteDatabase) -> Unit,
        seedData: (SupportSQLiteDatabase) -> Unit
    ) {
        targetContext.deleteDatabase(TEST_DB)
        openDatabase(
            version = version,
            onCreate = { db ->
                createSchema(db)
                seedData(db)
            }
        ).use { }
    }

    private fun openDatabase(
        version: Int,
        onCreate: (SupportSQLiteDatabase) -> Unit = {},
        onUpgrade: (SupportSQLiteDatabase, Int, Int) -> Unit = { _, _, _ -> }
    ): SupportSQLiteDatabase {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(targetContext)
            .name(TEST_DB)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(version) {
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        super.onConfigure(db)
                        db.execSQL("PRAGMA foreign_keys = ON")
                    }

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        onCreate(db)
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                        onUpgrade(db, oldVersion, newVersion)
                    }
                }
            )
            .build()

        return FrameworkSQLiteOpenHelperFactory()
            .create(configuration)
            .writableDatabase
    }

    private fun createVersion2Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `teams` (
                `code` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `flagEmoji` TEXT NOT NULL,
                `confederation` TEXT NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                PRIMARY KEY(`code`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `stickers` (
                `id` TEXT NOT NULL,
                `teamCode` TEXT NOT NULL,
                `number` INTEGER NOT NULL,
                `label` TEXT NOT NULL,
                `stickerType` TEXT NOT NULL,
                `isShiny` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`teamCode`) REFERENCES `teams`(`code`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_stickers_teamCode` ON `stickers` (`teamCode`)"
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `user_stickers` (
                `stickerId` TEXT NOT NULL,
                `quantityOwned` INTEGER NOT NULL,
                PRIMARY KEY(`stickerId`),
                FOREIGN KEY(`stickerId`) REFERENCES `stickers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    private fun createVersion3Schema(db: SupportSQLiteDatabase) {
        createVersion2Schema(db)
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

    private fun createVersion4Schema(db: SupportSQLiteDatabase) {
        createVersion3Schema(db)
        db.execSQL(
            """
            ALTER TABLE teams
            ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }

    private fun assertSticker(
        db: SupportSQLiteDatabase,
        number: Int,
        expectedId: String,
        expectedLabel: String,
        expectedType: StickerType
    ) {
        db.query(
            "SELECT id, label, stickerType FROM stickers WHERE teamCode = 'KSA' AND number = ?",
            arrayOf(number)
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(expectedId, cursor.getString(0))
            assertEquals(expectedLabel, cursor.getString(1))
            assertEquals(expectedType.name, cursor.getString(2))
        }
    }

    private fun assertTableColumn(
        db: SupportSQLiteDatabase,
        tableName: String,
        columnName: String,
        expectedType: String,
        expectedNotNull: Int,
        expectedPrimaryKey: Int
    ) {
        db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == columnName) {
                    assertEquals(expectedType, cursor.getString(2))
                    assertEquals(expectedNotNull, cursor.getInt(3))
                    assertEquals(expectedPrimaryKey, cursor.getInt(5))
                    return
                }
            }
        }

        throw AssertionError("Column $columnName not found in $tableName")
    }

    private fun SupportSQLiteDatabase.hasRow(query: String): Boolean =
        query(query).use { cursor -> cursor.moveToFirst() }

    private fun SupportSQLiteDatabase.singleInt(query: String): Int =
        query(query).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getInt(0)
        }

    private fun SupportSQLiteDatabase.singleString(query: String): String =
        query(query).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getString(0)
        }

    private fun SupportSQLiteDatabase.singleLong(query: String): Long =
        query(query).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getLong(0)
        }

    private fun SupportSQLiteDatabase.singleNullableLong(query: String): Long? =
        query(query).use { cursor ->
            assertTrue(cursor.moveToFirst())
            if (cursor.isNull(0)) null else cursor.getLong(0)
        }

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private companion object {
        const val TEST_DB = "app-database-migration-test.db"
    }
}

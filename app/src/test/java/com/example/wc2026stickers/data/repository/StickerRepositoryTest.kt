package com.wc2026stickers.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.wc2026stickers.app.data.backup.BackupStickerQuantity
import com.wc2026stickers.app.data.backup.CollectionBackupPayload
import com.wc2026stickers.app.data.backup.CollectionRestoreMode
import com.wc2026stickers.app.data.backup.CollectionRestorePreview
import com.wc2026stickers.app.data.db.AppDatabase
import com.wc2026stickers.app.data.db.entities.Sticker
import com.wc2026stickers.app.data.db.entities.StickerType
import com.wc2026stickers.app.data.db.entities.Team
import com.wc2026stickers.app.data.seed.DatabaseSeeder
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StickerRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: StickerRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StickerRepository(
            database = database,
            teamDao = database.teamDao(),
            stickerDao = database.stickerDao(),
            seeder = DatabaseSeeder(database.teamDao(), database.stickerDao(), database)
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `setQuantity coerces negatives to zero`() = runTest {
        insertTestSticker(id = "ARG-1", number = 1)

        repository.setQuantity("ARG-1", -4)

        assertNull(database.stickerDao().getUserSticker("ARG-1"))
    }

    @Test
    fun `incrementSticker starts at zero and keeps growing`() = runTest {
        insertTestSticker(id = "ARG-2", number = 2)

        repository.incrementSticker("ARG-2")
        repository.incrementSticker("ARG-2")

        val sticker = database.stickerDao().getUserSticker("ARG-2")
        assertEquals(2, sticker?.quantityOwned)
        assertNotNull(sticker?.firstCollectedAt)
        assertNotNull(sticker?.lastUpdatedAt)
    }

    @Test
    fun `setQuantity preserves first collected timestamp and updates last updated timestamp`() = runTest {
        insertTestSticker(id = "ARG-3", number = 3)

        repository.setQuantity("ARG-3", 1)
        val collected = requireNotNull(database.stickerDao().getUserSticker("ARG-3"))
        repository.setQuantity("ARG-3", 0)
        val removed = requireNotNull(database.stickerDao().getUserSticker("ARG-3"))

        assertEquals(0, removed.quantityOwned)
        assertEquals(collected.firstCollectedAt, removed.firstCollectedAt)
        assertTrue((removed.lastUpdatedAt ?: 0L) >= (collected.lastUpdatedAt ?: Long.MAX_VALUE))
    }

    @Test
    fun `resolveStickerId returns canonical ids for valid codes only`() = runTest {
        insertTestSticker(id = "ARG-17", number = 17)

        assertEquals("ARG-17", repository.resolveStickerId(" arg17 "))
        assertNull(repository.resolveStickerId("ARG99"))
        assertNull(repository.resolveStickerId("not-a-code"))
    }

    @Test
    fun `setTeamFavorite persists favorite flag`() = runTest {
        insertTestSticker(id = "ARG-4", number = 4)

        repository.setTeamFavorite("ARG", true)

        assertTrue(database.teamDao().getTeamByCode("ARG")?.isFavorite == true)
    }

    @Test
    fun `prepareRestore filters out unknown sticker ids`() = runTest {
        insertTestSticker(id = "ARG-1", number = 1)

        val preview = repository.prepareRestore(
            CollectionBackupPayload(
                exportedAt = "2026-01-01T12:00:00Z",
                stickers = listOf(
                    BackupStickerQuantity("ARG-1", 2),
                    BackupStickerQuantity("XXX-9", 1)
                )
            )
        )

        assertEquals(listOf(BackupStickerQuantity("ARG-1", 2)), preview.validEntries)
        assertEquals(listOf("XXX-9"), preview.skippedStickerIds)
    }

    @Test
    fun `restoreBackup merge keeps higher local quantities`() = runTest {
        insertTestSticker(id = "ARG-1", number = 1)
        insertTestSticker(id = "ARG-2", number = 2)
        repository.setQuantity("ARG-1", 3)

        val result = repository.restoreBackup(
            preview = CollectionRestorePreview(
                backup = CollectionBackupPayload(
                    exportedAt = "2026-01-01T12:00:00Z",
                    stickers = listOf(
                        BackupStickerQuantity("ARG-1", 2),
                        BackupStickerQuantity("ARG-2", 4)
                    )
                ),
                validEntries = listOf(
                    BackupStickerQuantity("ARG-1", 2),
                    BackupStickerQuantity("ARG-2", 4)
                ),
                skippedStickerIds = emptyList()
            ),
            mode = CollectionRestoreMode.MERGE
        )

        assertEquals(3, database.stickerDao().getUserSticker("ARG-1")?.quantityOwned)
        assertEquals(4, database.stickerDao().getUserSticker("ARG-2")?.quantityOwned)
        assertEquals(1, result.importedStickerCount)
        assertEquals(1, result.unchangedStickerCount)
    }

    @Test
    fun `restoreBackup replace clears stickers missing from backup`() = runTest {
        insertTestSticker(id = "ARG-1", number = 1)
        insertTestSticker(id = "ARG-2", number = 2)
        repository.setQuantity("ARG-1", 2)
        repository.setQuantity("ARG-2", 1)

        repository.restoreBackup(
            preview = CollectionRestorePreview(
                backup = CollectionBackupPayload(
                    exportedAt = "2026-01-01T12:00:00Z",
                    stickers = listOf(BackupStickerQuantity("ARG-2", 5))
                ),
                validEntries = listOf(BackupStickerQuantity("ARG-2", 5)),
                skippedStickerIds = emptyList()
            ),
            mode = CollectionRestoreMode.REPLACE
        )

        assertNull(database.stickerDao().getUserSticker("ARG-1"))
        assertEquals(5, database.stickerDao().getUserSticker("ARG-2")?.quantityOwned)
    }

    private suspend fun insertTestSticker(id: String, number: Int) {
        database.teamDao().insertTeam(
            Team(
                code = "ARG",
                name = "Argentina",
                flagEmoji = "AR",
                confederation = "CONMEBOL",
                sortOrder = 1
            )
        )
        database.stickerDao().insertSticker(
            Sticker(
                id = id,
                teamCode = "ARG",
                number = number,
                label = "Sticker $number",
                stickerType = StickerType.PLAYER
            )
        )
    }
}

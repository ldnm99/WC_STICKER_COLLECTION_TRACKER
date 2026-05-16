package com.wc2026stickers.app.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.wc2026stickers.app.data.db.AppDatabase
import com.wc2026stickers.app.data.db.entities.Sticker
import com.wc2026stickers.app.data.db.entities.StickerType
import com.wc2026stickers.app.data.db.entities.Team
import com.wc2026stickers.app.data.db.entities.UserSticker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StickerDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var stickerDao: StickerDao
    private lateinit var teamDao: TeamDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        stickerDao = database.stickerDao()
        teamDao = database.teamDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getStickersForTeam defaults missing quantities to zero`() = runTest {
        insertTeam(code = "ARG", name = "Argentina", sortOrder = 1)
        insertSticker(id = "ARG-1", teamCode = "ARG", number = 1, label = "Badge", stickerType = StickerType.BADGE)
        insertSticker(id = "ARG-2", teamCode = "ARG", number = 2, label = "Captain", stickerType = StickerType.PLAYER)
        stickerDao.upsertUserSticker(UserSticker(stickerId = "ARG-2", quantityOwned = 2))

        val stickers = stickerDao.getStickersForTeam("ARG").first()

        assertEquals(listOf("ARG-1", "ARG-2"), stickers.map { it.id })
        assertEquals(listOf(0, 2), stickers.map { it.quantityOwned })
        assertEquals(listOf("Argentina", "Argentina"), stickers.map { it.teamName })
    }

    @Test
    fun `searchStickers matches label id team code and team name`() = runTest {
        insertTeam(code = "ARG", name = "Argentina", sortOrder = 1)
        insertTeam(code = "BRA", name = "Brazil", sortOrder = 2)
        insertSticker(id = "ARG-1", teamCode = "ARG", number = 1, label = "Badge", stickerType = StickerType.BADGE)
        insertSticker(id = "ARG-17", teamCode = "ARG", number = 17, label = "Lionel Messi", stickerType = StickerType.PLAYER)
        insertSticker(id = "BRA-10", teamCode = "BRA", number = 10, label = "Playmaker", stickerType = StickerType.PLAYER)
        stickerDao.upsertUserSticker(UserSticker(stickerId = "ARG-17", quantityOwned = 2))

        assertEquals(listOf("ARG-17"), stickerDao.searchStickers("%MESSI%").first().map { it.id })
        assertEquals(listOf("ARG-17"), stickerDao.searchStickers("%ARG-17%").first().map { it.id })
        assertEquals(listOf("ARG-1", "ARG-17"), stickerDao.searchStickers("%ARG%").first().map { it.id })
        assertEquals(listOf("ARG-1", "ARG-17"), stickerDao.searchStickers("%ARGENTINA%").first().map { it.id })
        assertEquals(2, stickerDao.searchStickers("%MESSI%").first().single().quantityOwned)
        assertEquals("Argentina", stickerDao.searchStickers("%MESSI%").first().single().teamName)
    }

    private suspend fun insertTeam(code: String, name: String, sortOrder: Int) {
        teamDao.insertTeam(
            Team(
                code = code,
                name = name,
                flagEmoji = code,
                confederation = "TEST",
                sortOrder = sortOrder
            )
        )
    }

    private suspend fun insertSticker(
        id: String,
        teamCode: String,
        number: Int,
        label: String,
        stickerType: StickerType
    ) {
        stickerDao.insertSticker(
            Sticker(
                id = id,
                teamCode = teamCode,
                number = number,
                label = label,
                stickerType = stickerType,
                isShiny = stickerType == StickerType.BADGE
            )
        )
    }
}

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TeamDaoTest {
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
    fun `getAllTeamKpiStats counts duplicate stickers and extra copies separately`() = runTest {
        teamDao.insertTeam(
            Team(
                code = "ARG",
                name = "Argentina",
                flagEmoji = "AR",
                confederation = "CONMEBOL",
                sortOrder = 1
            )
        )
        stickerDao.insertSticker(Sticker("ARG-1", "ARG", 1, "Badge", StickerType.BADGE, isShiny = true))
        stickerDao.insertSticker(Sticker("ARG-2", "ARG", 2, "Player 1", StickerType.PLAYER))
        stickerDao.insertSticker(Sticker("ARG-3", "ARG", 3, "Player 2", StickerType.PLAYER))
        stickerDao.insertSticker(Sticker("ARG-13", "ARG", 13, "Team Photo", StickerType.TEAM_PHOTO))
        stickerDao.upsertUserSticker(UserSticker("ARG-1", 1))
        stickerDao.upsertUserSticker(UserSticker("ARG-2", 3))
        stickerDao.upsertUserSticker(UserSticker("ARG-3", 2))
        stickerDao.upsertUserSticker(UserSticker("ARG-13", 0))

        val stats = teamDao.getAllTeamKpiStats().first().single()

        assertEquals(4, stats.totalCount)
        assertEquals(3, stats.collectedCount)
        assertEquals(2, stats.duplicateStickerCount)
        assertEquals(3, stats.duplicateExtraCount)
        assertEquals(1, stats.badgeCollected)
        assertEquals(0, stats.teamPhotoCollected)
    }

    @Test
    fun `favorite team queries only include teams marked as favorite`() = runTest {
        teamDao.insertTeam(
            Team(
                code = "ARG",
                name = "Argentina",
                flagEmoji = "AR",
                confederation = "CONMEBOL",
                sortOrder = 1,
                isFavorite = true
            )
        )
        teamDao.insertTeam(
            Team(
                code = "BRA",
                name = "Brazil",
                flagEmoji = "BR",
                confederation = "CONMEBOL",
                sortOrder = 2
            )
        )
        stickerDao.insertSticker(Sticker("ARG-1", "ARG", 1, "Badge", StickerType.BADGE, isShiny = true))
        stickerDao.insertSticker(Sticker("BRA-1", "BRA", 1, "Badge", StickerType.BADGE, isShiny = true))
        stickerDao.upsertUserSticker(UserSticker("ARG-1", 1))

        val favorites = teamDao.getFavoriteTeamsWithProgress().first()

        assertEquals(1, favorites.size)
        assertEquals("ARG", favorites.single().code)
        assertTrue(favorites.single().isFavorite)
        assertEquals(1, favorites.single().collectedCount)
    }

    @Test
    fun `setTeamFavorite updates observed team state`() = runTest {
        teamDao.insertTeam(
            Team(
                code = "USA",
                name = "United States",
                flagEmoji = "US",
                confederation = "CONCACAF",
                sortOrder = 1
            )
        )

        assertFalse(teamDao.observeTeamByCode("USA").first()?.isFavorite ?: true)

        teamDao.setTeamFavorite("USA", true)

        assertTrue(teamDao.observeTeamByCode("USA").first()?.isFavorite == true)
    }
}

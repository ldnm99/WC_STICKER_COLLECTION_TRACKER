package com.wc2026stickers.app.ui.kpi

import com.wc2026stickers.app.data.db.dao.TeamKpiStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KpiRankingPresentationTest {

    @Test
    fun `almost complete presentation groups teams by stickers remaining`() {
        val presentation = listOf(
            teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 19),
            teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 18),
            teamStats(code = "ESP", name = "Spain", totalCount = 20, collectedCount = 17),
            teamStats(code = "USA", name = "USA", totalCount = 20, collectedCount = 12),
            teamStats(code = "FRA", name = "France", totalCount = 20, collectedCount = 20)
        ).toKpiRankingPresentation(KpiType.ALMOST_COMPLETE)

        assertEquals(
            listOf("1 sticker away", "2 stickers away", "3 stickers away", "Already complete", "More than 3 away"),
            presentation.sections.map { it.title }
        )
        assertEquals(listOf("ARG"), presentation.sections[0].teams.map { it.code })
        assertEquals(listOf("FRA"), presentation.sections[3].teams.map { it.code })
        assertEquals(listOf("USA"), presentation.sections[4].teams.map { it.code })
    }

    @Test
    fun `confederation presentation creates regional sections with summaries`() {
        val presentation = listOf(
            teamStats(code = "BRA", name = "Brazil", confederation = "CONMEBOL", totalCount = 20, collectedCount = 18),
            teamStats(code = "ARG", name = "Argentina", confederation = "CONMEBOL", totalCount = 20, collectedCount = 10),
            teamStats(code = "ESP", name = "Spain", confederation = "UEFA", totalCount = 20, collectedCount = 15)
        ).toKpiRankingPresentation(KpiType.CONFEDERATION)

        assertEquals(listOf("🌎 CONMEBOL", "🌍 UEFA"), presentation.sections.map { it.title })
        assertEquals("28/40 collected • 70%", presentation.sections.first().summary)
        assertEquals(listOf("BRA", "ARG"), presentation.sections.first().teams.map { it.code })
    }

    @Test
    fun `missing badges presentation celebrates when every badge is collected`() {
        val presentation = listOf(
            teamStats(code = "ARG", name = "Argentina", badgeCollected = 1, totalCount = 20, collectedCount = 18),
            teamStats(code = "BRA", name = "Brazil", badgeCollected = 1, totalCount = 20, collectedCount = 16)
        ).toKpiRankingPresentation(KpiType.MISSING_BADGES)

        assertEquals("Every badge is collected", presentation.emptyState?.title)
        assertEquals(listOf("Already collected"), presentation.sections.map { it.title })
        assertTrue(presentation.sections.first().teams.all { it.badgeCollected == 1 })
    }

    private fun teamStats(
        code: String,
        name: String,
        confederation: String = "UEFA",
        totalCount: Int,
        collectedCount: Int,
        duplicateStickerCount: Int = 0,
        duplicateExtraCount: Int = 0,
        badgeCollected: Int = 0,
        teamPhotoCollected: Int = 0
    ) = TeamKpiStats(
        code = code,
        name = name,
        flagEmoji = when (code) {
            "ARG" -> "🇦🇷"
            "BRA" -> "🇧🇷"
            "ESP" -> "🇪🇸"
            "USA" -> "🇺🇸"
            "FRA" -> "🇫🇷"
            else -> "🏳️"
        },
        confederation = confederation,
        totalCount = totalCount,
        collectedCount = collectedCount,
        duplicateStickerCount = duplicateStickerCount,
        duplicateExtraCount = duplicateExtraCount,
        badgeCollected = badgeCollected,
        teamPhotoCollected = teamPhotoCollected
    )
}

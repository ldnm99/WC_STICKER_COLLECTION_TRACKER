package com.wc2026stickers.app.ui.kpi

import com.wc2026stickers.app.data.db.dao.TeamKpiStats
import org.junit.Assert.assertEquals
import org.junit.Test

class KpiModelsTest {

    @Test
    fun `sortedByKpi ranks almost-complete teams first then falls back to completion ratio`() {
        val teams = listOf(
            teamStats(code = "USA", name = "USA", totalCount = 20, collectedCount = 6),
            teamStats(code = "ISL", name = "Iceland", totalCount = 10, collectedCount = 5),
            teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 17),
            teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 19)
        )

        val sorted = teams.sortedByKpi(KpiType.ALMOST_COMPLETE)

        assertEquals(listOf("ARG", "BRA", "ISL", "USA"), sorted.map { it.code })
    }

    @Test
    fun `sortedByKpi keeps tied most-completed teams ahead of trailing teams`() {
        val teams = listOf(
            teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 12),
            teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 12),
            teamStats(code = "ESP", name = "Spain", totalCount = 20, collectedCount = 8)
        )

        val sorted = teams.sortedByKpi(KpiType.MOST_COMPLETED)

        assertEquals(setOf("BRA", "ARG"), sorted.take(2).map { it.code }.toSet())
        assertEquals("ESP", sorted.last().code)
    }

    @Test
    fun `sortedByKpi groups missing badges and photos first with alphabetical ties`() {
        val teams = listOf(
            teamStats(code = "ESP", name = "Spain", badgeCollected = 0, teamPhotoCollected = 1),
            teamStats(code = "BRA", name = "Brazil", badgeCollected = 1, teamPhotoCollected = 1),
            teamStats(code = "ARG", name = "Argentina", badgeCollected = 0, teamPhotoCollected = 0),
            teamStats(code = "USA", name = "USA", badgeCollected = 1, teamPhotoCollected = 0)
        )

        val badgeSorted = teams.sortedByKpi(KpiType.MISSING_BADGES)
        val photoSorted = teams.sortedByKpi(KpiType.MISSING_TEAM_PHOTOS)

        assertEquals(listOf("ARG", "ESP", "BRA", "USA"), badgeSorted.map { it.code })
        assertEquals(listOf("ARG", "USA", "BRA", "ESP"), photoSorted.map { it.code })
    }

    @Test
    fun `kpiRankingActionLabel gives purposeful next-step guidance`() {
        assertEquals(
            "Finish this team",
            teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 19)
                .kpiRankingActionLabel(KpiType.ALMOST_COMPLETE)
        )
        assertEquals(
            "Add badge sticker",
            teamStats(code = "BRA", name = "Brazil", badgeCollected = 0)
                .kpiRankingActionLabel(KpiType.MISSING_BADGES)
        )
        assertEquals(
            "Review extras",
            teamStats(code = "ESP", name = "Spain", duplicateExtraCount = 3)
                .kpiRankingActionLabel(KpiType.MOST_DUPLICATES)
        )
    }

    private fun teamStats(
        code: String,
        name: String,
        confederation: String = "UEFA",
        totalCount: Int = 20,
        collectedCount: Int = 0,
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
            "ISL" -> "🇮🇸"
            "USA" -> "🇺🇸"
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

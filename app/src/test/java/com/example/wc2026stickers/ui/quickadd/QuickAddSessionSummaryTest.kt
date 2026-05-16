package com.wc2026stickers.app.ui.quickadd

import com.wc2026stickers.app.data.db.dao.TeamKpiStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickAddSessionSummaryTest {

    @Test
    fun `calculateQuickAddSessionSummary separates first-time stickers duplicates and progress`() {
        val summary = calculateQuickAddSessionSummary(
            stickerSnapshots = listOf(
                QuickAddStickerSnapshot(stickerId = "ARG-1", quantityBefore = 0, quantityAdded = 2),
                QuickAddStickerSnapshot(stickerId = "BRA-2", quantityBefore = 1, quantityAdded = 1),
                QuickAddStickerSnapshot(stickerId = "ESP-3", quantityBefore = 2, quantityAdded = 1)
            ),
            teamStatsBefore = listOf(
                teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 18),
                teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 10),
                teamStats(code = "ESP", name = "Spain", totalCount = 20, collectedCount = 20)
            ),
            teamStatsAfter = listOf(
                teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 19),
                teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 10),
                teamStats(code = "ESP", name = "Spain", totalCount = 20, collectedCount = 20)
            )
        )

        assertEquals(listOf(QuickAddStickerSessionDelta("ARG-1", 1)), summary.newStickers)
        assertEquals(
            listOf(
                QuickAddStickerSessionDelta("ARG-1", 1),
                QuickAddStickerSessionDelta("BRA-2", 1),
                QuickAddStickerSessionDelta("ESP-3", 1)
            ),
            summary.duplicateStickers
        )
        assertEquals(
            listOf(
                QuickAddTeamSessionDelta(
                    code = "ARG",
                    name = "Argentina",
                    flagEmoji = "🇦🇷",
                    collectedDelta = 1,
                    remainingBefore = 2,
                    remainingAfter = 1
                )
            ),
            summary.progressedTeams
        )
    }

    @Test
    fun `calculateQuickAddSessionSummary sorts progressed teams by closeness to completion`() {
        val summary = calculateQuickAddSessionSummary(
            stickerSnapshots = listOf(
                QuickAddStickerSnapshot(stickerId = "ARG-1", quantityBefore = 0, quantityAdded = 1),
                QuickAddStickerSnapshot(stickerId = "BRA-1", quantityBefore = 0, quantityAdded = 2)
            ),
            teamStatsBefore = listOf(
                teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 19),
                teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 16)
            ),
            teamStatsAfter = listOf(
                teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 20),
                teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 18)
            )
        )

        assertEquals(listOf("ARG", "BRA"), summary.progressedTeams.map { it.code })
        assertTrue(summary.progressedTeams.first().isCompleted)
    }

    private fun teamStats(
        code: String,
        name: String,
        totalCount: Int,
        collectedCount: Int
    ) = TeamKpiStats(
        code = code,
        name = name,
        flagEmoji = when (code) {
            "ARG" -> "🇦🇷"
            "BRA" -> "🇧🇷"
            "ESP" -> "🇪🇸"
            else -> "🏳️"
        },
        confederation = "TEST",
        totalCount = totalCount,
        collectedCount = collectedCount,
        duplicateStickerCount = 0,
        duplicateExtraCount = 0,
        badgeCollected = 0,
        teamPhotoCollected = 0
    )
}

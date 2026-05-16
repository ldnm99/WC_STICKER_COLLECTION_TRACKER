package com.wc2026stickers.app.ui.history

import com.wc2026stickers.app.data.db.dao.StickerCollectionHistoryRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionHistorySummaryTest {

    @Test
    fun `calculateCollectionHistorySummary classifies recent activity and milestones`() {
        val summary = calculateCollectionHistorySummary(
            totalCount = 8,
            recentUpdates = listOf(
                record(
                    stickerId = "ARG-4",
                    quantityOwned = 0,
                    firstCollectedAt = 1_000L,
                    lastUpdatedAt = 4_000L
                ),
                record(
                    stickerId = "ARG-3",
                    quantityOwned = 2,
                    firstCollectedAt = 3_000L,
                    lastUpdatedAt = 3_500L
                ),
                record(
                    stickerId = "ARG-2",
                    quantityOwned = 1,
                    firstCollectedAt = 2_000L,
                    lastUpdatedAt = 2_000L
                )
            ),
            collectedStickers = listOf(
                record(stickerId = "ARG-1", quantityOwned = 1, firstCollectedAt = 1_000L, lastUpdatedAt = 1_000L),
                record(stickerId = "ARG-2", quantityOwned = 1, firstCollectedAt = 2_000L, lastUpdatedAt = 2_000L),
                record(stickerId = "ARG-3", quantityOwned = 2, firstCollectedAt = 3_000L, lastUpdatedAt = 3_500L),
                record(stickerId = "ARG-5", quantityOwned = 1, firstCollectedAt = 5_000L, lastUpdatedAt = 5_000L)
            )
        )

        assertEquals(4, summary.collectedCount)
        assertEquals(listOf("ARG-4", "ARG-3", "ARG-2"), summary.recentActivity.map { it.stickerId })
        assertTrue(summary.recentActivity.first().isRemoved)
        assertFalse(summary.recentActivity[1].isNewSticker)
        assertTrue(summary.recentActivity[2].isNewSticker)
        assertEquals(listOf("First sticker", "25% collected", "50% collected"), summary.reachedMilestones.map { it.label })
        assertEquals("75% collected", summary.nextMilestone?.label)
        assertEquals(2, summary.nextMilestone?.remainingCount)
    }

    @Test
    fun `calculateCollectionHistorySummary handles empty history`() {
        val summary = calculateCollectionHistorySummary(
            totalCount = 0,
            recentUpdates = emptyList(),
            collectedStickers = emptyList()
        )

        assertEquals(0, summary.collectedCount)
        assertTrue(summary.recentActivity.isEmpty())
        assertTrue(summary.reachedMilestones.isEmpty())
        assertNull(summary.nextMilestone)
    }

    private fun record(
        stickerId: String,
        quantityOwned: Int,
        firstCollectedAt: Long?,
        lastUpdatedAt: Long?
    ) = StickerCollectionHistoryRecord(
        stickerId = stickerId,
        teamCode = "ARG",
        teamName = "Argentina",
        teamFlagEmoji = "🇦🇷",
        number = 1,
        label = "Sticker",
        quantityOwned = quantityOwned,
        firstCollectedAt = firstCollectedAt,
        lastUpdatedAt = lastUpdatedAt
    )
}

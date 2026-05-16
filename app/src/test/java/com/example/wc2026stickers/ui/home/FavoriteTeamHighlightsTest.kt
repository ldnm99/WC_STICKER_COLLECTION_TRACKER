package com.wc2026stickers.app.ui.home

import com.wc2026stickers.app.data.db.dao.TeamWithProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteTeamHighlightsTest {

    @Test
    fun `toFavoriteTeamHighlights keeps favorites and prioritizes active teams`() {
        val highlights = listOf(
            team(code = "BRA", collected = 0, total = 20, isFavorite = true, sortOrder = 2),
            team(code = "ARG", collected = 18, total = 20, isFavorite = true, sortOrder = 1),
            team(code = "USA", collected = 20, total = 20, isFavorite = true, sortOrder = 3),
            team(code = "MEX", collected = 5, total = 20, isFavorite = false, sortOrder = 4)
        ).toFavoriteTeamHighlights(limit = 2)

        assertEquals(listOf("ARG", "BRA"), highlights.map { it.code })
        assertEquals("2 stickers left", highlights.first().summary)
    }

    private fun team(
        code: String,
        collected: Int,
        total: Int,
        isFavorite: Boolean,
        sortOrder: Int
    ) = TeamWithProgress(
        code = code,
        name = code,
        flagEmoji = code,
        confederation = "CONF",
        sortOrder = sortOrder,
        isFavorite = isFavorite,
        collectedCount = collected,
        totalCount = total
    )
}

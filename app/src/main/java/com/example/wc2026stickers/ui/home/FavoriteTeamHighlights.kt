package com.wc2026stickers.app.ui.home

import com.wc2026stickers.app.data.db.dao.TeamWithProgress

data class FavoriteTeamHighlight(
    val code: String,
    val flagEmoji: String,
    val name: String,
    val collectedCount: Int,
    val totalCount: Int,
    val summary: String
)

fun List<TeamWithProgress>.toFavoriteTeamHighlights(limit: Int = 3): List<FavoriteTeamHighlight> = this
    .filter { it.isFavorite }
    .sortedWith(
        compareBy<TeamWithProgress>(
            {
                when {
                    it.totalCount == 0 -> 3
                    it.collectedCount in 1 until it.totalCount -> 0
                    it.collectedCount == 0 -> 1
                    else -> 2
                }
            },
            { (it.totalCount - it.collectedCount).coerceAtLeast(0) },
            { it.sortOrder }
        )
    )
    .take(limit)
    .map { team ->
        FavoriteTeamHighlight(
            code = team.code,
            flagEmoji = team.flagEmoji,
            name = team.name,
            collectedCount = team.collectedCount,
            totalCount = team.totalCount,
            summary = team.favoriteSummary()
        )
    }

private fun TeamWithProgress.favoriteSummary(): String {
    val remaining = (totalCount - collectedCount).coerceAtLeast(0)
    return when {
        totalCount == 0 -> "Ready to review"
        collectedCount == totalCount -> "Complete"
        collectedCount == 0 -> "Not started"
        remaining == 1 -> "1 sticker left"
        remaining in 2..3 -> "$remaining stickers left"
        else -> "$collectedCount / $totalCount collected"
    }
}

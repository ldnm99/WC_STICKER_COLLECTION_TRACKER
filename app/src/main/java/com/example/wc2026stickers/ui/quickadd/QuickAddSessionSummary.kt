package com.wc2026stickers.app.ui.quickadd

import com.wc2026stickers.app.data.db.dao.TeamKpiStats

data class QuickAddStickerSnapshot(
    val stickerId: String,
    val quantityBefore: Int,
    val quantityAdded: Int
) {
    val quantityAfter: Int = quantityBefore + quantityAdded
}

data class QuickAddStickerSessionDelta(
    val stickerId: String,
    val count: Int
)

data class QuickAddTeamSessionDelta(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val collectedDelta: Int,
    val remainingBefore: Int,
    val remainingAfter: Int
) {
    val isCompleted: Boolean = remainingAfter == 0
}

data class QuickAddSessionSummary(
    val newStickers: List<QuickAddStickerSessionDelta> = emptyList(),
    val duplicateStickers: List<QuickAddStickerSessionDelta> = emptyList(),
    val progressedTeams: List<QuickAddTeamSessionDelta> = emptyList()
) {
    val newStickerCount: Int = newStickers.sumOf { it.count }
    val duplicateStickerCount: Int = duplicateStickers.sumOf { it.count }
}

fun calculateQuickAddSessionSummary(
    stickerSnapshots: List<QuickAddStickerSnapshot>,
    teamStatsBefore: List<TeamKpiStats>,
    teamStatsAfter: List<TeamKpiStats>
): QuickAddSessionSummary {
    val newStickers = stickerSnapshots.mapNotNull { snapshot ->
        if (snapshot.quantityBefore == 0 && snapshot.quantityAdded > 0) {
            QuickAddStickerSessionDelta(
                stickerId = snapshot.stickerId,
                count = 1
            )
        } else {
            null
        }
    }

    val duplicateStickers = stickerSnapshots.mapNotNull { snapshot ->
        val duplicateCountBefore = (snapshot.quantityBefore - 1).coerceAtLeast(0)
        val duplicateCountAfter = (snapshot.quantityAfter - 1).coerceAtLeast(0)
        val duplicateIncrease = duplicateCountAfter - duplicateCountBefore
        if (duplicateIncrease > 0) {
            QuickAddStickerSessionDelta(
                stickerId = snapshot.stickerId,
                count = duplicateIncrease
            )
        } else {
            null
        }
    }

    val teamStatsBeforeByCode = teamStatsBefore.associateBy { it.code }
    val progressedTeams = teamStatsAfter.mapNotNull { after ->
        val before = teamStatsBeforeByCode[after.code] ?: return@mapNotNull null
        val collectedDelta = after.collectedCount - before.collectedCount
        val remainingBefore = (before.totalCount - before.collectedCount).coerceAtLeast(0)
        val remainingAfter = (after.totalCount - after.collectedCount).coerceAtLeast(0)
        if (collectedDelta > 0 && remainingAfter < remainingBefore) {
            QuickAddTeamSessionDelta(
                code = after.code,
                name = after.name,
                flagEmoji = after.flagEmoji,
                collectedDelta = collectedDelta,
                remainingBefore = remainingBefore,
                remainingAfter = remainingAfter
            )
        } else {
            null
        }
    }.sortedWith(
        compareBy<QuickAddTeamSessionDelta> { it.remainingAfter }
            .thenByDescending { it.collectedDelta }
            .thenBy { it.name }
    )

    return QuickAddSessionSummary(
        newStickers = newStickers,
        duplicateStickers = duplicateStickers,
        progressedTeams = progressedTeams
    )
}

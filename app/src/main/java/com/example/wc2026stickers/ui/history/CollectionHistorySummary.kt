package com.wc2026stickers.app.ui.history

import com.wc2026stickers.app.data.db.dao.StickerCollectionHistoryRecord
import kotlin.math.ceil

data class RecentCollectionActivity(
    val stickerId: String,
    val teamName: String,
    val teamFlagEmoji: String,
    val label: String,
    val quantityOwned: Int,
    val lastUpdatedAt: Long,
    val isNewSticker: Boolean,
    val isRemoved: Boolean
)

data class CollectionMilestone(
    val label: String,
    val targetCount: Int,
    val reachedAt: Long
)

data class CollectionMilestoneTarget(
    val label: String,
    val targetCount: Int,
    val remainingCount: Int
)

data class CollectionHistorySummary(
    val collectedCount: Int = 0,
    val recentActivity: List<RecentCollectionActivity> = emptyList(),
    val reachedMilestones: List<CollectionMilestone> = emptyList(),
    val nextMilestone: CollectionMilestoneTarget? = null
)

fun calculateCollectionHistorySummary(
    totalCount: Int,
    recentUpdates: List<StickerCollectionHistoryRecord>,
    collectedStickers: List<StickerCollectionHistoryRecord>
): CollectionHistorySummary {
    val milestones = buildMilestoneTargets(totalCount)
    val collectedWithHistory = collectedStickers.filter { it.firstCollectedAt != null }
    val reachedMilestones = milestones.mapNotNull { target ->
        if (target > collectedWithHistory.size) {
            null
        } else {
            CollectionMilestone(
                label = milestoneLabel(target, totalCount),
                targetCount = target,
                reachedAt = checkNotNull(collectedWithHistory[target - 1].firstCollectedAt)
            )
        }
    }
    val nextMilestone = milestones
        .firstOrNull { it > collectedWithHistory.size }
        ?.let { target ->
            CollectionMilestoneTarget(
                label = milestoneLabel(target, totalCount),
                targetCount = target,
                remainingCount = target - collectedWithHistory.size
            )
        }

    return CollectionHistorySummary(
        collectedCount = collectedWithHistory.size,
        recentActivity = recentUpdates
            .filter { it.lastUpdatedAt != null }
            .sortedByDescending { it.lastUpdatedAt }
            .map { record ->
                RecentCollectionActivity(
                    stickerId = record.stickerId,
                    teamName = record.teamName,
                    teamFlagEmoji = record.teamFlagEmoji,
                    label = record.label,
                    quantityOwned = record.quantityOwned,
                    lastUpdatedAt = checkNotNull(record.lastUpdatedAt),
                    isNewSticker = record.quantityOwned > 0 &&
                        record.firstCollectedAt != null &&
                        record.firstCollectedAt == record.lastUpdatedAt,
                    isRemoved = record.quantityOwned == 0
                )
            },
        reachedMilestones = reachedMilestones,
        nextMilestone = nextMilestone
    )
}

private fun buildMilestoneTargets(totalCount: Int): List<Int> {
    if (totalCount <= 0) return emptyList()
    return linkedSetOf(
        1,
        ceil(totalCount * 0.25).toInt(),
        ceil(totalCount * 0.50).toInt(),
        ceil(totalCount * 0.75).toInt(),
        totalCount
    ).toList()
}

private fun milestoneLabel(targetCount: Int, totalCount: Int): String = when {
    targetCount <= 1 -> "First sticker"
    targetCount >= totalCount -> "Collection complete"
    targetCount == ceil(totalCount * 0.25).toInt() -> "25% collected"
    targetCount == ceil(totalCount * 0.50).toInt() -> "50% collected"
    targetCount == ceil(totalCount * 0.75).toInt() -> "75% collected"
    else -> "$targetCount stickers collected"
}

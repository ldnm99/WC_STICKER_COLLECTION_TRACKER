package com.wc2026stickers.app.ui.home

import com.wc2026stickers.app.data.db.dao.TeamKpiStats
import com.wc2026stickers.app.ui.kpi.HomeKpiUiModel
import com.wc2026stickers.app.ui.kpi.KpiCallToActionUiModel
import com.wc2026stickers.app.ui.kpi.KpiPriority
import com.wc2026stickers.app.ui.kpi.KpiType
import com.wc2026stickers.app.ui.kpi.confederationDisplayName

fun List<TeamKpiStats>.toHomeKpiUiModels(): List<HomeKpiUiModel> = listOf(
    toAlmostCompleteKpi(),
    toMostCompletedKpi(),
    toMostDuplicateKpi(),
    toConfederationKpi(),
    toMissingBadgesKpi(),
    toMissingTeamPhotosKpi()
)

private fun List<TeamKpiStats>.toAlmostCompleteKpi(): HomeKpiUiModel {
    if (isEmpty()) {
        return HomeKpiUiModel(
            type = KpiType.ALMOST_COMPLETE,
            headline = "No data yet",
            supportingText = "Teams that are 1–3 stickers away from completion will appear here.",
            context = "Closest sections to finishing",
            cta = KpiCallToActionUiModel("Review progress by team")
        )
    }

    val almostDone = filter { it.collectedCount > 0 }
        .map { it to (it.totalCount - it.collectedCount) }
        .filter { (_, missing) -> missing in 1..3 }
        .sortedBy { (_, missing) -> missing }

    if (almostDone.isEmpty()) {
        val anyProgress = any { it.collectedCount > 0 }
        return if (anyProgress) {
            HomeKpiUiModel(
                type = KpiType.ALMOST_COMPLETE,
                headline = "None yet",
                supportingText = "Keep collecting — teams within 3 stickers of completion will show up here.",
                context = "Closest sections to finishing",
                priority = KpiPriority.NEUTRAL,
                cta = KpiCallToActionUiModel("See completion leaders")
            )
        } else {
            HomeKpiUiModel(
                type = KpiType.ALMOST_COMPLETE,
                headline = "No progress yet",
                supportingText = "Add your first stickers to start tracking completion.",
                context = "Closest sections to finishing",
                priority = KpiPriority.MEDIUM,
                cta = KpiCallToActionUiModel("Start with any team section")
            )
        }
    }

    val (bestTeam, bestMissing) = almostDone.first()
    val missingLabel = if (bestMissing == 1) "1 sticker missing" else "$bestMissing stickers missing"
    val teamsAtSameLevel = almostDone.count { (_, missing) -> missing == bestMissing }
    val otherCount = almostDone.size - teamsAtSameLevel
    val otherSuffix = if (otherCount > 0) {
        " • $otherCount more section${if (otherCount > 1) "s" else ""} almost done"
    } else {
        ""
    }

    return HomeKpiUiModel(
        type = KpiType.ALMOST_COMPLETE,
        headline = "${bestTeam.flagEmoji} ${bestTeam.name}",
        supportingText = "$missingLabel$otherSuffix",
        context = if (bestMissing == 1) "Best chance to finish next" else "Sections within reach",
        priority = if (bestMissing == 1) KpiPriority.HIGH else KpiPriority.MEDIUM,
        cta = KpiCallToActionUiModel("Open near-complete ranking")
    )
}

private fun List<TeamKpiStats>.toMostCompletedKpi(): HomeKpiUiModel {
    if (isEmpty()) {
        return HomeKpiUiModel(
            type = KpiType.MOST_COMPLETED,
            headline = "No data yet",
            supportingText = "Completion leaders will appear here once the collection is loaded.",
            context = "Current collection leader"
        )
    }

    val bestCollected = maxOf { it.collectedCount }
    if (bestCollected == 0) {
        return HomeKpiUiModel(
            type = KpiType.MOST_COMPLETED,
            headline = "No progress yet",
            supportingText = "Add your first stickers to see which section is leading.",
            context = "Current collection leader",
            priority = KpiPriority.NEUTRAL
        )
    }

    val leaders = filter { it.collectedCount == bestCollected }
    val totalCount = leaders.firstOrNull()?.totalCount ?: 0
    val percent = if (totalCount > 0) (bestCollected * 100) / totalCount else 0

    return HomeKpiUiModel(
        type = KpiType.MOST_COMPLETED,
        headline = "$bestCollected / $totalCount ($percent%)",
        supportingText = leaderText(leaders),
        context = "Current collection leader",
        priority = KpiPriority.POSITIVE
    )
}

private fun List<TeamKpiStats>.toMostDuplicateKpi(): HomeKpiUiModel {
    if (isEmpty()) {
        return HomeKpiUiModel(
            type = KpiType.MOST_DUPLICATES,
            headline = "No data yet",
            supportingText = "Duplicate leaders will appear here once you add stickers.",
            context = "Best swap opportunity"
        )
    }

    val bestExtraCount = maxOf { it.duplicateExtraCount }
    if (bestExtraCount == 0) {
        return HomeKpiUiModel(
            type = KpiType.MOST_DUPLICATES,
            headline = "No duplicates yet",
            supportingText = "When you own 2+ copies, the section with the most extras shows up here.",
            context = "Best swap opportunity",
            priority = KpiPriority.NEUTRAL
        )
    }

    val leaders = filter { it.duplicateExtraCount == bestExtraCount }
    val minDuplicateStickerCount = leaders.minOf { it.duplicateStickerCount }
    val maxDuplicateStickerCount = leaders.maxOf { it.duplicateStickerCount }
    val duplicateStickerText = if (minDuplicateStickerCount == maxDuplicateStickerCount) {
        val stickerLabel = if (maxDuplicateStickerCount == 1) "sticker" else "stickers"
        "$maxDuplicateStickerCount duplicate $stickerLabel"
    } else {
        "$minDuplicateStickerCount-$maxDuplicateStickerCount duplicate stickers"
    }

    return HomeKpiUiModel(
        type = KpiType.MOST_DUPLICATES,
        headline = "$bestExtraCount extras",
        supportingText = "${leaderText(leaders)} • $duplicateStickerText",
        context = "Best swap opportunity",
        priority = KpiPriority.MEDIUM
    )
}

private fun List<TeamKpiStats>.toConfederationKpi(): HomeKpiUiModel {
    if (isEmpty()) {
        return HomeKpiUiModel(
            type = KpiType.CONFEDERATION,
            headline = "No data yet",
            supportingText = "Completion by region will appear here once the sticker set is loaded.",
            context = "Strongest region so far"
        )
    }

    data class ConfStats(val conf: String, val collected: Int, val total: Int)

    val confStats = groupBy { it.confederation }
        .map { (conf, teams) ->
            ConfStats(
                conf = conf,
                collected = teams.sumOf { it.collectedCount },
                total = teams.sumOf { it.totalCount }
            )
        }
        .filter { it.total > 0 }
        .sortedByDescending { if (it.total > 0) it.collected.toFloat() / it.total else 0f }

    val anyProgress = confStats.any { it.collected > 0 }
    if (!anyProgress) {
        return HomeKpiUiModel(
            type = KpiType.CONFEDERATION,
            headline = "No progress yet",
            supportingText = "Add stickers to see which confederation you're leading.",
            context = "Strongest region so far",
            priority = KpiPriority.NEUTRAL
        )
    }

    val best = confStats.first()
    val bestPct = if (best.total > 0) (best.collected * 100) / best.total else 0

    val summaryLines = confStats
        .filter { it.collected > 0 }
        .take(3)
        .joinToString(" · ") { stats ->
            val pct = if (stats.total > 0) (stats.collected * 100) / stats.total else 0
            "${confederationDisplayName(stats.conf)} $pct%"
        }

    return HomeKpiUiModel(
        type = KpiType.CONFEDERATION,
        headline = "${confederationDisplayName(best.conf)} — $bestPct%",
        supportingText = summaryLines,
        context = "Strongest region so far",
        priority = KpiPriority.POSITIVE
    )
}

private fun List<TeamKpiStats>.toMissingBadgesKpi(): HomeKpiUiModel {
    if (isEmpty()) {
        return HomeKpiUiModel(
            type = KpiType.MISSING_BADGES,
            headline = "No data yet",
            supportingText = "Badge collection status will appear here once the sticker set is loaded.",
            context = "Team header stickers still missing"
        )
    }

    val missingTeams = filter { it.badgeCollected == 0 }

    if (missingTeams.isEmpty()) {
        return HomeKpiUiModel(
            type = KpiType.MISSING_BADGES,
            headline = "All badges collected! 🎉",
            supportingText = "You have the badge sticker for every team section.",
            context = "Team header stickers still missing",
            priority = KpiPriority.POSITIVE,
            cta = KpiCallToActionUiModel("Review badge coverage")
        )
    }

    val teamList = missingTeams.take(3).joinToString(", ") { "${it.flagEmoji} ${it.name}" }
    val remaining = missingTeams.size - 3
    val suffix = if (remaining > 0) " +$remaining more" else ""

    return HomeKpiUiModel(
        type = KpiType.MISSING_BADGES,
        headline = "${missingTeams.size} badge${if (missingTeams.size == 1) "" else "s"} missing",
        supportingText = teamList + suffix,
        context = "Team header stickers still missing",
        priority = KpiPriority.HIGH
    )
}

private fun List<TeamKpiStats>.toMissingTeamPhotosKpi(): HomeKpiUiModel {
    if (isEmpty()) {
        return HomeKpiUiModel(
            type = KpiType.MISSING_TEAM_PHOTOS,
            headline = "No data yet",
            supportingText = "Team photo collection status will appear here once the sticker set is loaded.",
            context = "Section photo stickers still missing"
        )
    }

    val missingTeams = filter { it.teamPhotoCollected == 0 }

    if (missingTeams.isEmpty()) {
        return HomeKpiUiModel(
            type = KpiType.MISSING_TEAM_PHOTOS,
            headline = "All team photos collected! 🎉",
            supportingText = "You have the team photo sticker for every section.",
            context = "Section photo stickers still missing",
            priority = KpiPriority.POSITIVE,
            cta = KpiCallToActionUiModel("Review photo coverage")
        )
    }

    val teamList = missingTeams.take(3).joinToString(", ") { "${it.flagEmoji} ${it.name}" }
    val remaining = missingTeams.size - 3
    val suffix = if (remaining > 0) " +$remaining more" else ""

    return HomeKpiUiModel(
        type = KpiType.MISSING_TEAM_PHOTOS,
        headline = "${missingTeams.size} team photo${if (missingTeams.size == 1) "" else "s"} missing",
        supportingText = teamList + suffix,
        context = "Section photo stickers still missing",
        priority = KpiPriority.MEDIUM
    )
}

private fun leaderText(leaders: List<TeamKpiStats>): String {
    val names = leaders.take(3).joinToString { "${it.flagEmoji} ${it.name}" }
    val remainingCount = leaders.size - 3
    val suffix = if (remainingCount > 0) " +$remainingCount more" else ""
    val prefix = if (leaders.size == 1) "Leader: " else "Tied leaders: "
    return prefix + names + suffix
}

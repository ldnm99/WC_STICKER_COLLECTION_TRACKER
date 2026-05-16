package com.wc2026stickers.app.ui.kpi

import com.wc2026stickers.app.data.db.dao.TeamKpiStats

enum class KpiType(
    val routeKey: String,
    val cardTitle: String,
    val rankingTitle: String,
    val defaultCtaLabel: String
) {
    ALMOST_COMPLETE(
        routeKey = "almost_complete",
        cardTitle = "Almost Complete",
        rankingTitle = "🔜 Almost Complete",
        defaultCtaLabel = "Review near-complete teams"
    ),
    MOST_COMPLETED(
        routeKey = "most_completed",
        cardTitle = "Most Completed Team",
        rankingTitle = "🏆 Most Completed",
        defaultCtaLabel = "See completion leaders"
    ),
    MOST_DUPLICATES(
        routeKey = "most_duplicates",
        cardTitle = "Most Duplicates",
        rankingTitle = "📋 Most Duplicates",
        defaultCtaLabel = "Check duplicate hotspots"
    ),
    CONFEDERATION(
        routeKey = "confederation",
        cardTitle = "Confederation Progress",
        rankingTitle = "🌍 Confederation Progress",
        defaultCtaLabel = "Compare confederation progress"
    ),
    MISSING_BADGES(
        routeKey = "missing_badges",
        cardTitle = "Missing Badges",
        rankingTitle = "🛡️ Badge Status",
        defaultCtaLabel = "Find missing badges"
    ),
    MISSING_TEAM_PHOTOS(
        routeKey = "missing_team_photos",
        cardTitle = "Missing Team Photos",
        rankingTitle = "📸 Team Photo Status",
        defaultCtaLabel = "Find missing team photos"
    );

    companion object {
        fun fromRouteKey(routeKey: String?): KpiType =
            entries.firstOrNull { it.routeKey == routeKey } ?: MOST_COMPLETED
    }
}

enum class KpiPriority(val label: String) {
    HIGH("Focus"),
    MEDIUM("Watch"),
    POSITIVE("Strong"),
    NEUTRAL("Overview")
}

data class KpiCallToActionUiModel(
    val label: String
)

data class HomeKpiUiModel(
    val type: KpiType,
    val headline: String,
    val supportingText: String,
    val context: String? = null,
    val priority: KpiPriority = KpiPriority.NEUTRAL,
    val cta: KpiCallToActionUiModel = KpiCallToActionUiModel(type.defaultCtaLabel)
) {
    val title: String
        get() = type.cardTitle

    val routeKey: String
        get() = type.routeKey
}

data class KpiRankingSectionUiModel(
    val title: String,
    val summary: String? = null,
    val teams: List<TeamKpiStats>
)

data class KpiRankingEmptyStateUiModel(
    val title: String,
    val body: String
)

data class KpiRankingPresentationUiModel(
    val introTitle: String,
    val introBody: String,
    val orderLabel: String,
    val emptyState: KpiRankingEmptyStateUiModel? = null,
    val sections: List<KpiRankingSectionUiModel>
)

fun List<TeamKpiStats>.sortedByKpi(kpiType: KpiType): List<TeamKpiStats> = when (kpiType) {
    KpiType.MOST_COMPLETED -> sortedWith(
        compareByDescending<TeamKpiStats> { it.collectedCount }
            .thenByDescending { it.completionPercentage() }
            .thenBy { it.name }
    )
    KpiType.ALMOST_COMPLETE -> {
        val almost = filter { (it.totalCount - it.collectedCount) in 1..3 }
            .sortedBy { it.totalCount - it.collectedCount }
        val rest = filter { (it.totalCount - it.collectedCount) !in 1..3 }
            .sortedByDescending {
                if (it.totalCount > 0) it.collectedCount.toFloat() / it.totalCount else 0f
            }
        almost + rest
    }
    KpiType.MOST_DUPLICATES -> sortedWith(
        compareByDescending<TeamKpiStats> { it.duplicateExtraCount }
            .thenByDescending { it.duplicateStickerCount }
            .thenBy { it.name }
    )
    KpiType.CONFEDERATION -> sortedWith(
        compareBy<TeamKpiStats> { it.confederation }
            .thenByDescending { it.completionPercentage() }
            .thenBy { it.name }
    )
    KpiType.MISSING_BADGES -> sortedWith(compareBy({ it.badgeCollected }, { it.name }))
    KpiType.MISSING_TEAM_PHOTOS -> sortedWith(compareBy({ it.teamPhotoCollected }, { it.name }))
}

fun List<TeamKpiStats>.toKpiRankingPresentation(kpiType: KpiType): KpiRankingPresentationUiModel = when (kpiType) {
    KpiType.MOST_COMPLETED -> toMostCompletedRankingPresentation()
    KpiType.ALMOST_COMPLETE -> toAlmostCompleteRankingPresentation()
    KpiType.MOST_DUPLICATES -> toMostDuplicatesRankingPresentation()
    KpiType.CONFEDERATION -> toConfederationRankingPresentation()
    KpiType.MISSING_BADGES -> toMissingBadgesRankingPresentation()
    KpiType.MISSING_TEAM_PHOTOS -> toMissingTeamPhotosRankingPresentation()
}

fun TeamKpiStats.kpiRankingMetric(kpiType: KpiType): String = when (kpiType) {
    KpiType.MOST_COMPLETED -> "${collectedCount}/${totalCount} • ${completionPercentage()}%"
    KpiType.ALMOST_COMPLETE -> when (val missing = totalCount - collectedCount) {
        0 -> "Complete"
        1 -> "1 left"
        else -> "$missing left"
    }
    KpiType.MOST_DUPLICATES -> when (duplicateExtraCount) {
        0 -> "No extras"
        1 -> "1 extra"
        else -> "$duplicateExtraCount extras"
    }
    KpiType.CONFEDERATION -> "${completionPercentage()}%"
    KpiType.MISSING_BADGES -> if (badgeCollected == 1) "Collected" else "Missing"
    KpiType.MISSING_TEAM_PHOTOS -> if (teamPhotoCollected == 1) "Collected" else "Missing"
}

fun TeamKpiStats.kpiRankingSupportingText(kpiType: KpiType): String = when (kpiType) {
    KpiType.MOST_COMPLETED -> {
        val missing = (totalCount - collectedCount).coerceAtLeast(0)
        if (missing == 0) "Section completed" else "$missing sticker${if (missing == 1) "" else "s"} still missing"
    }
    KpiType.ALMOST_COMPLETE -> when (totalCount - collectedCount) {
        0 -> "Every sticker in this section is collected"
        else -> "$collectedCount of $totalCount collected so far"
    }
    KpiType.MOST_DUPLICATES -> when {
        duplicateExtraCount == 0 -> "No tradable extras in this section yet"
        duplicateStickerCount == 1 -> "1 duplicate sticker is generating $duplicateExtraCount tradable extras"
        else -> "$duplicateStickerCount duplicate stickers are generating $duplicateExtraCount tradable extras"
    }
    KpiType.CONFEDERATION -> "$collectedCount of $totalCount collected in this section"
    KpiType.MISSING_BADGES -> if (badgeCollected == 1) {
        "Header badge sticker already collected"
    } else {
        "Header badge sticker still missing"
    }
    KpiType.MISSING_TEAM_PHOTOS -> if (teamPhotoCollected == 1) {
        "Team photo sticker already collected"
    } else {
        "Team photo sticker still missing"
    }
}

fun TeamKpiStats.kpiRankingActionLabel(kpiType: KpiType): String = when (kpiType) {
    KpiType.ALMOST_COMPLETE -> if ((totalCount - collectedCount) in 1..3) "Finish this team" else "Open team section"
    KpiType.MOST_COMPLETED -> if (collectedCount == 0) "Start this team" else "Keep momentum here"
    KpiType.MOST_DUPLICATES -> if (duplicateExtraCount > 0) "Review extras" else "Open team section"
    KpiType.CONFEDERATION -> if (collectedCount == 0) "Start this team" else "Open team section"
    KpiType.MISSING_BADGES -> if (badgeCollected == 0) "Add badge sticker" else "Open team section"
    KpiType.MISSING_TEAM_PHOTOS -> if (teamPhotoCollected == 0) "Add photo sticker" else "Open team section"
}

fun KpiType.shouldShowRankingProgressBar(): Boolean = when (this) {
    KpiType.MOST_COMPLETED,
    KpiType.ALMOST_COMPLETE,
    KpiType.CONFEDERATION -> true
    KpiType.MOST_DUPLICATES,
    KpiType.MISSING_BADGES,
    KpiType.MISSING_TEAM_PHOTOS -> false
}

fun confederationDisplayName(code: String): String = when (code) {
    "SPECIAL" -> "⭐ Special"
    "CONCACAF" -> "🌎 CONCACAF"
    "UEFA" -> "🌍 UEFA"
    "CONMEBOL" -> "🌎 CONMEBOL"
    "CAF" -> "🌍 CAF"
    "AFC" -> "🌏 AFC"
    "OFC" -> "🌏 OFC"
    else -> code
}

private fun List<TeamKpiStats>.toMostCompletedRankingPresentation(): KpiRankingPresentationUiModel {
    if (isEmpty()) {
        return KpiRankingPresentationUiModel(
            introTitle = "Completion ranking",
            introBody = "See which team sections are furthest along, with progress context shown on every row.",
            orderLabel = "Ordered by collected stickers, then completion rate",
            emptyState = KpiRankingEmptyStateUiModel(
                title = "No team progress available yet",
                body = "Load your collection data to compare which sections are leading."
            ),
            sections = emptyList()
        )
    }

    val sorted = sortedByKpi(KpiType.MOST_COMPLETED)
    val highestCollected = sorted.maxOfOrNull { it.collectedCount } ?: 0
    val sections = buildList {
        addSection(
            title = "Leaders",
            summary = if (highestCollected > 0) "Sections with the highest collected total" else "Nobody has started yet",
            teams = sorted.filter { it.collectedCount == highestCollected && highestCollected > 0 }
        )
        addSection(
            title = "Building momentum",
            summary = "Sections already in progress but not leading yet",
            teams = sorted.filter { it.collectedCount in 1 until highestCollected }
        )
        addSection(
            title = "Still to start",
            summary = "Sections with zero collected stickers",
            teams = sorted.filter { it.collectedCount == 0 }
        )
    }

    return KpiRankingPresentationUiModel(
        introTitle = "Completion ranking",
        introBody = "See which team sections are furthest along, with progress context shown on every row.",
        orderLabel = "Ordered by collected stickers, then completion rate",
        emptyState = if (sorted.isNotEmpty() && highestCollected == 0) {
            KpiRankingEmptyStateUiModel(
                title = "No sections started yet",
                body = "Collect your first stickers to see the leaderboard take shape."
            )
        } else {
            null
        },
        sections = sections
    )
}

private fun List<TeamKpiStats>.toAlmostCompleteRankingPresentation(): KpiRankingPresentationUiModel {
    if (isEmpty()) {
        return KpiRankingPresentationUiModel(
            introTitle = "Near-complete tracker",
            introBody = "Teams that are 1 to 3 stickers away are broken into finish-line groups so your next wins stand out.",
            orderLabel = "Grouped by stickers remaining",
            emptyState = KpiRankingEmptyStateUiModel(
                title = "No team progress available yet",
                body = "Load your collection data to see which sections are closest to completion."
            ),
            sections = emptyList()
        )
    }

    val sorted = sortedByKpi(KpiType.ALMOST_COMPLETE)
    val sections = buildList {
        addMissingSection(sorted, 1)
        addMissingSection(sorted, 2)
        addMissingSection(sorted, 3)
        addSection(
            title = "Already complete",
            summary = "Sections you have already finished",
            teams = sorted.filter { it.totalCount - it.collectedCount == 0 }
        )
        addSection(
            title = "More than 3 away",
            summary = "Sections that still need a bigger push",
            teams = sorted.filter { (it.totalCount - it.collectedCount) > 3 }
        )
    }

    val nearCompleteCount = sorted.count { (it.totalCount - it.collectedCount) in 1..3 }
    return KpiRankingPresentationUiModel(
        introTitle = "Near-complete tracker",
        introBody = "Teams that are 1 to 3 stickers away are broken into finish-line groups so your next wins stand out.",
        orderLabel = "Grouped by stickers remaining",
        emptyState = if (sorted.isNotEmpty() && nearCompleteCount == 0) {
            KpiRankingEmptyStateUiModel(
                title = "Nothing is within three stickers yet",
                body = "Keep collecting and the closest sections will move into the finish-line groups."
            )
        } else {
            null
        },
        sections = sections
    )
}

private fun List<TeamKpiStats>.toMostDuplicatesRankingPresentation(): KpiRankingPresentationUiModel {
    if (isEmpty()) {
        return KpiRankingPresentationUiModel(
            introTitle = "Duplicate overview",
            introBody = "Use this screen to spot the sections with the most swap potential, not just the raw number of duplicate stickers.",
            orderLabel = "Ordered by tradable extras, then duplicate sticker count",
            emptyState = KpiRankingEmptyStateUiModel(
                title = "No duplicate data available yet",
                body = "Load your collection to see where swap opportunities are building up."
            ),
            sections = emptyList()
        )
    }

    val sorted = sortedByKpi(KpiType.MOST_DUPLICATES)
    val sections = buildList {
        addSection(
            title = "Best trade stock",
            summary = "Sections holding tradable extras right now",
            teams = sorted.filter { it.duplicateExtraCount > 0 }
        )
        addSection(
            title = "No extras yet",
            summary = "Sections without tradable duplicates yet",
            teams = sorted.filter { it.duplicateExtraCount == 0 }
        )
    }

    return KpiRankingPresentationUiModel(
        introTitle = "Duplicate overview",
        introBody = "Use this screen to spot the sections with the most swap potential, not just the raw number of duplicate stickers.",
        orderLabel = "Ordered by tradable extras, then duplicate sticker count",
        emptyState = if (sorted.isNotEmpty() && sorted.none { it.duplicateExtraCount > 0 }) {
            KpiRankingEmptyStateUiModel(
                title = "No tradable extras yet",
                body = "Once you own more than one copy of a sticker, the best swap opportunities will show up here."
            )
        } else {
            null
        },
        sections = sections
    )
}

private fun List<TeamKpiStats>.toConfederationRankingPresentation(): KpiRankingPresentationUiModel {
    if (isEmpty()) {
        return KpiRankingPresentationUiModel(
            introTitle = "Regional progress view",
            introBody = "Sections are grouped by confederation so you can compare like-for-like progress inside each region.",
            orderLabel = "Grouped by confederation and ordered by completion rate",
            emptyState = KpiRankingEmptyStateUiModel(
                title = "No regional progress available yet",
                body = "Load your collection data to compare confederation progress."
            ),
            sections = emptyList()
        )
    }

    val grouped = sortedByKpi(KpiType.CONFEDERATION).groupBy { it.confederation }
    val sections = grouped.map { (confederation, teams) ->
        KpiRankingSectionUiModel(
            title = confederationDisplayName(confederation),
            summary = teams.confederationSummary(),
            teams = teams
        )
    }

    return KpiRankingPresentationUiModel(
        introTitle = "Regional progress view",
        introBody = "Sections are grouped by confederation so you can compare like-for-like progress inside each region.",
        orderLabel = "Grouped by confederation and ordered by completion rate",
        emptyState = if (sections.isNotEmpty() && none { it.collectedCount > 0 }) {
            KpiRankingEmptyStateUiModel(
                title = "No regional leader yet",
                body = "Add your first stickers and this view will highlight the strongest confederation progress."
            )
        } else {
            null
        },
        sections = sections
    )
}

private fun List<TeamKpiStats>.toMissingBadgesRankingPresentation(): KpiRankingPresentationUiModel {
    if (isEmpty()) {
        return KpiRankingPresentationUiModel(
            introTitle = "Badge checklist",
            introBody = "Separate missing badges from completed ones so the remaining header stickers are easy to review.",
            orderLabel = "Missing badges first",
            emptyState = KpiRankingEmptyStateUiModel(
                title = "No badge data available yet",
                body = "Load your collection data to review which team badges are still missing."
            ),
            sections = emptyList()
        )
    }

    val sorted = sortedByKpi(KpiType.MISSING_BADGES)
    val missingTeams = sorted.filter { it.badgeCollected == 0 }
    return KpiRankingPresentationUiModel(
        introTitle = "Badge checklist",
        introBody = "Separate missing badges from completed ones so the remaining header stickers are easy to review.",
        orderLabel = "Missing badges first",
        emptyState = if (sorted.isNotEmpty() && missingTeams.isEmpty()) {
            KpiRankingEmptyStateUiModel(
                title = "Every badge is collected",
                body = "Nice work — every team section already has its header badge sticker."
            )
        } else {
            null
        },
        sections = listOfNotNull(
            section(
                title = "Still missing",
                summary = "Teams that still need their header badge sticker",
                teams = missingTeams
            ),
            section(
                title = "Already collected",
                summary = "Teams with their badge sticker already owned",
                teams = sorted.filter { it.badgeCollected == 1 }
            )
        )
    )
}

private fun List<TeamKpiStats>.toMissingTeamPhotosRankingPresentation(): KpiRankingPresentationUiModel {
    if (isEmpty()) {
        return KpiRankingPresentationUiModel(
            introTitle = "Team photo checklist",
            introBody = "Review the remaining section photo stickers first, while keeping completed sections one tap away.",
            orderLabel = "Missing team photos first",
            emptyState = KpiRankingEmptyStateUiModel(
                title = "No team photo data available yet",
                body = "Load your collection data to review which section photos are still missing."
            ),
            sections = emptyList()
        )
    }

    val sorted = sortedByKpi(KpiType.MISSING_TEAM_PHOTOS)
    val missingTeams = sorted.filter { it.teamPhotoCollected == 0 }
    return KpiRankingPresentationUiModel(
        introTitle = "Team photo checklist",
        introBody = "Review the remaining section photo stickers first, while keeping completed sections one tap away.",
        orderLabel = "Missing team photos first",
        emptyState = if (sorted.isNotEmpty() && missingTeams.isEmpty()) {
            KpiRankingEmptyStateUiModel(
                title = "Every team photo is collected",
                body = "Nice work — every section already has its team photo sticker."
            )
        } else {
            null
        },
        sections = listOfNotNull(
            section(
                title = "Still missing",
                summary = "Teams that still need their team photo sticker",
                teams = missingTeams
            ),
            section(
                title = "Already collected",
                summary = "Teams with their team photo sticker already owned",
                teams = sorted.filter { it.teamPhotoCollected == 1 }
            )
        )
    )
}

private fun MutableList<KpiRankingSectionUiModel>.addMissingSection(
    teams: List<TeamKpiStats>,
    missingCount: Int
) {
    addSection(
        title = if (missingCount == 1) "1 sticker away" else "$missingCount stickers away",
        summary = "Best next sections to finish",
        teams = teams.filter { (it.totalCount - it.collectedCount) == missingCount }
    )
}

private fun MutableList<KpiRankingSectionUiModel>.addSection(
    title: String,
    summary: String? = null,
    teams: List<TeamKpiStats>
) {
    section(title, summary, teams)?.let(::add)
}

private fun section(
    title: String,
    summary: String? = null,
    teams: List<TeamKpiStats>
): KpiRankingSectionUiModel? = teams.takeIf { it.isNotEmpty() }?.let {
    KpiRankingSectionUiModel(title = title, summary = summary, teams = it)
}

private fun List<TeamKpiStats>.confederationSummary(): String {
    val collected = sumOf { it.collectedCount }
    val total = sumOf { it.totalCount }
    val pct = if (total > 0) (collected * 100) / total else 0
    return "$collected/$total collected • $pct%"
}

private fun TeamKpiStats.completionPercentage(): Int =
    if (totalCount > 0) (collectedCount * 100) / totalCount else 0

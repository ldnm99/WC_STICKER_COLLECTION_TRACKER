package com.wc2026stickers.app.ui.home

import com.wc2026stickers.app.data.db.dao.TeamKpiStats
import com.wc2026stickers.app.ui.kpi.KpiPriority
import com.wc2026stickers.app.ui.kpi.KpiType
import com.wc2026stickers.app.ui.kpi.sortedByKpi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeKpiUiModelMapperTest {

    @Test
    fun `toHomeKpiUiModels returns richer state for actionable KPI cards`() {
        val models = listOf(
            teamStats(
                code = "ARG",
                name = "Argentina",
                confederation = "CONMEBOL",
                totalCount = 20,
                collectedCount = 19,
                duplicateStickerCount = 3,
                duplicateExtraCount = 4,
                badgeCollected = 0,
                teamPhotoCollected = 1
            ),
            teamStats(
                code = "BRA",
                name = "Brazil",
                confederation = "CONMEBOL",
                totalCount = 20,
                collectedCount = 18,
                duplicateStickerCount = 0,
                duplicateExtraCount = 0,
                badgeCollected = 1,
                teamPhotoCollected = 0
            ),
            teamStats(
                code = "ESP",
                name = "Spain",
                confederation = "UEFA",
                totalCount = 20,
                collectedCount = 18,
                duplicateStickerCount = 1,
                duplicateExtraCount = 1,
                badgeCollected = 1,
                teamPhotoCollected = 1
            )
        ).toHomeKpiUiModels()
            .associateBy { it.type }

        val almostComplete = requireNotNull(models[KpiType.ALMOST_COMPLETE])
        assertEquals("🇦🇷 Argentina", almostComplete.headline)
        assertEquals("Best chance to finish next", almostComplete.context)
        assertEquals(KpiPriority.HIGH, almostComplete.priority)
        assertEquals("Open near-complete ranking", almostComplete.cta.label)

        val missingBadges = requireNotNull(models[KpiType.MISSING_BADGES])
        assertEquals("1 badge missing", missingBadges.headline)
        assertEquals("Find missing badges", missingBadges.cta.label)
        assertEquals(KpiPriority.HIGH, missingBadges.priority)

        val confederation = requireNotNull(models[KpiType.CONFEDERATION])
        assertTrue(confederation.headline.startsWith("🌎 CONMEBOL"))
        assertEquals(KpiPriority.POSITIVE, confederation.priority)
    }

    @Test
    fun `toHomeKpiUiModels keeps all KPI cards available for empty data`() {
        val models = emptyList<TeamKpiStats>().toHomeKpiUiModels()

        assertEquals(6, models.size)
        assertEquals(
            KpiType.entries.toList(),
            models.map { it.type }
        )
        assertTrue(models.all { it.cta.label.isNotBlank() })
        assertTrue(models.all { it.headline == "No data yet" })
    }

    @Test
    fun `toHomeKpiUiModels summarizes tied leaders for shared KPI highs`() {
        val models = listOf(
            teamStats(
                code = "ARG",
                name = "Argentina",
                totalCount = 10,
                collectedCount = 7,
                duplicateStickerCount = 2,
                duplicateExtraCount = 3
            ),
            teamStats(
                code = "BRA",
                name = "Brazil",
                totalCount = 10,
                collectedCount = 7,
                duplicateStickerCount = 4,
                duplicateExtraCount = 3
            ),
            teamStats(
                code = "ESP",
                name = "Spain",
                totalCount = 10,
                collectedCount = 7,
                duplicateStickerCount = 1,
                duplicateExtraCount = 1
            ),
            teamStats(
                code = "USA",
                name = "USA",
                totalCount = 10,
                collectedCount = 7,
                duplicateStickerCount = 1,
                duplicateExtraCount = 0
            )
        ).toHomeKpiUiModels()
            .associateBy { it.type }

        val mostCompleted = requireNotNull(models[KpiType.MOST_COMPLETED])
        assertEquals("7 / 10 (70%)", mostCompleted.headline)
        assertEquals(
            "Tied leaders: 🇦🇷 Argentina, 🇧🇷 Brazil, 🇪🇸 Spain +1 more",
            mostCompleted.supportingText
        )

        val mostDuplicates = requireNotNull(models[KpiType.MOST_DUPLICATES])
        assertEquals("3 extras", mostDuplicates.headline)
        assertEquals(
            "Tied leaders: 🇦🇷 Argentina, 🇧🇷 Brazil • 2-4 duplicate stickers",
            mostDuplicates.supportingText
        )
    }

    @Test
    fun `toHomeKpiUiModels distinguishes no progress from low progress almost-complete states`() {
        val lowProgressModels = listOf(
            teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 5),
            teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 1)
        ).toHomeKpiUiModels()
            .associateBy { it.type }

        val lowProgressAlmostComplete = requireNotNull(lowProgressModels[KpiType.ALMOST_COMPLETE])
        assertEquals("None yet", lowProgressAlmostComplete.headline)
        assertEquals(
            "Keep collecting — teams within 3 stickers of completion will show up here.",
            lowProgressAlmostComplete.supportingText
        )
        assertEquals("See completion leaders", lowProgressAlmostComplete.cta.label)

        val noProgressModels = listOf(
            teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 0),
            teamStats(code = "BRA", name = "Brazil", totalCount = 20, collectedCount = 0)
        ).toHomeKpiUiModels()
            .associateBy { it.type }

        val noProgressAlmostComplete = requireNotNull(noProgressModels[KpiType.ALMOST_COMPLETE])
        assertEquals("No progress yet", noProgressAlmostComplete.headline)
        assertEquals("Add your first stickers to start tracking completion.", noProgressAlmostComplete.supportingText)
        assertEquals(KpiPriority.MEDIUM, noProgressAlmostComplete.priority)
        assertEquals("Start with any team section", noProgressAlmostComplete.cta.label)
    }

    @Test
    fun `sortedByKpi keeps almost complete teams ahead of other progress states`() {
        val teams = listOf(
            teamStats(code = "USA", name = "USA", totalCount = 20, collectedCount = 12),
            teamStats(code = "ARG", name = "Argentina", totalCount = 20, collectedCount = 19),
            teamStats(code = "ESP", name = "Spain", totalCount = 20, collectedCount = 18)
        )

        val sorted = teams.sortedByKpi(KpiType.ALMOST_COMPLETE)

        assertEquals(listOf("ARG", "ESP", "USA"), sorted.map { it.code })
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

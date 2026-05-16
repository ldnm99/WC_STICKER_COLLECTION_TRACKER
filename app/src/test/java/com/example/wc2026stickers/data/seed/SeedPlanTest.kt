package com.wc2026stickers.app.data.seed

import org.junit.Assert.assertEquals
import org.junit.Test

class SeedPlanTest {
    @Test
    fun `create skips work when database is complete and current`() {
        val plan = SeedPlan.create(
            storedSeedVersion = 3,
            currentTeamCount = 49,
            currentStickerCount = 980,
            expectedTeamCount = 49,
            expectedStickerCount = 980,
            targetSeedVersion = 3
        )

        assertEquals(SeedPlan(requiresBaseSeed = false, requiresLabelRefresh = false), plan)
    }

    @Test
    fun `create refreshes labels when version is outdated`() {
        val plan = SeedPlan.create(
            storedSeedVersion = 2,
            currentTeamCount = 49,
            currentStickerCount = 980,
            expectedTeamCount = 49,
            expectedStickerCount = 980,
            targetSeedVersion = 3
        )

        assertEquals(SeedPlan(requiresBaseSeed = false, requiresLabelRefresh = true), plan)
    }

    @Test
    fun `create reseeds incomplete databases even when metadata looks current`() {
        val plan = SeedPlan.create(
            storedSeedVersion = 3,
            currentTeamCount = 48,
            currentStickerCount = 979,
            expectedTeamCount = 49,
            expectedStickerCount = 980,
            targetSeedVersion = 3
        )

        assertEquals(SeedPlan(requiresBaseSeed = true, requiresLabelRefresh = true), plan)
    }
}

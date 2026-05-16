package com.wc2026stickers.app.data.seed

data class SeedPlan(
    val requiresBaseSeed: Boolean,
    val requiresLabelRefresh: Boolean
) {
    val requiresWork: Boolean
        get() = requiresBaseSeed || requiresLabelRefresh

    companion object {
        fun create(
            storedSeedVersion: Int,
            currentTeamCount: Int,
            currentStickerCount: Int,
            expectedTeamCount: Int,
            expectedStickerCount: Int,
            targetSeedVersion: Int
        ): SeedPlan {
            val requiresBaseSeed =
                currentTeamCount < expectedTeamCount || currentStickerCount < expectedStickerCount
            val requiresLabelRefresh = requiresBaseSeed || storedSeedVersion < targetSeedVersion

            return SeedPlan(
                requiresBaseSeed = requiresBaseSeed,
                requiresLabelRefresh = requiresLabelRefresh
            )
        }
    }
}

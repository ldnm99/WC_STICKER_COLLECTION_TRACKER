package com.wc2026stickers.app.ui.collection

import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.data.db.entities.StickerType
import org.junit.Assert.assertEquals
import org.junit.Test

class StickerCollectionFilteringTest {
    @Test
    fun `buildStickerCollectionUiState filters and sorts stickers`() {
        val uiState = buildStickerCollectionUiState(
            source = listOf(
                sticker(
                    id = "ARG-1",
                    teamCode = "ARG",
                    teamName = "Argentina",
                    confederation = "CONMEBOL",
                    label = "Badge",
                    stickerType = StickerType.BADGE,
                    isShiny = true,
                    quantityOwned = 1
                ),
                sticker(
                    id = "BRA-7",
                    teamCode = "BRA",
                    teamName = "Brazil",
                    confederation = "CONMEBOL",
                    label = "Forward",
                    stickerType = StickerType.PLAYER,
                    quantityOwned = 3
                ),
                sticker(
                    id = "ESP-4",
                    teamCode = "ESP",
                    teamName = "Spain",
                    confederation = "UEFA",
                    label = "Captain",
                    stickerType = StickerType.PLAYER,
                    quantityOwned = 2
                )
            ),
            filterState = StickerCollectionFilterState(
                confederation = "CONMEBOL",
                stickerType = StickerType.PLAYER,
                sortOption = StickerCollectionSortOption.OWNED_DESC
            )
        )

        assertEquals(3, uiState.totalCount)
        assertEquals(listOf("BRA-7"), uiState.stickers.map { it.id })
        assertEquals(listOf("UEFA", "CONMEBOL"), uiState.availableConfederations)
    }

    @Test
    fun `normalizeStickerSearchInput trims collapses spaces and uppercases`() {
        assertEquals("LIONEL MESSI", normalizeStickerSearchInput("  lionel   messi "))
    }

    @Test
    fun `applyStickerCollectionFilters keeps shiny items only`() {
        val stickers = listOf(
            sticker(id = "FWC-1", teamCode = "FWC", teamName = "World Cup", confederation = "SPECIAL", isShiny = true),
            sticker(id = "FWC-2", teamCode = "FWC", teamName = "World Cup", confederation = "SPECIAL", isShiny = false)
        )

        val filtered = stickers.applyStickerCollectionFilters(
            StickerCollectionFilterState(shinyOnly = true)
        )

        assertEquals(listOf("FWC-1"), filtered.map { it.id })
    }

    private fun sticker(
        id: String,
        teamCode: String,
        teamName: String,
        confederation: String,
        label: String = "Sticker",
        stickerType: StickerType = StickerType.PLAYER,
        isShiny: Boolean = false,
        quantityOwned: Int = 0
    ) = StickerWithQuantity(
        id = id,
        teamCode = teamCode,
        teamName = teamName,
        teamFlagEmoji = teamCode,
        confederation = confederation,
        number = 1,
        label = label,
        stickerType = stickerType,
        isShiny = isShiny,
        quantityOwned = quantityOwned
    )
}

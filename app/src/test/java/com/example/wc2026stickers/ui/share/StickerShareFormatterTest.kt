package com.wc2026stickers.app.ui.share

import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.data.db.entities.StickerType
import com.wc2026stickers.app.ui.friendmatcher.FriendMatchSticker
import org.junit.Assert.assertEquals
import org.junit.Test

class StickerShareFormatterTest {
    @Test
    fun `formatMissing builds grouped export text`() {
        val grouped = mapOf(
            "ARG" to listOf(
                sticker(id = "ARG-1", teamCode = "ARG", label = "Emblem (Argentina)")
            ),
            "FWC" to listOf(
                sticker(id = "FWC-9", teamCode = "FWC", label = "Mascot")
            )
        )

        val result = StickerShareFormatter.formatMissing(grouped)

        assertEquals(
            "[ARG]\n  ARG-1 – Emblem (Argentina)\n[FWC]\n  FWC-9 – Mascot",
            result
        )
    }

    @Test
    fun `formatDuplicates includes extra counts`() {
        val grouped = mapOf(
            "BRA" to listOf(
                sticker(id = "BRA-10", teamCode = "BRA", label = "Player", quantityOwned = 3)
            )
        )

        val result = StickerShareFormatter.formatDuplicates(grouped)

        assertEquals(
            "[BRA]\n  BRA-10 – Player ×2 extra",
            result
        )
    }

    @Test
    fun `formatFriendMatches keeps grouped actionable export text`() {
        val grouped = mapOf(
            "ARG" to listOf(
                FriendMatchSticker(
                    sticker = sticker(id = "ARG-1", teamCode = "ARG", label = "Badge"),
                    count = 1
                ),
                FriendMatchSticker(
                    sticker = sticker(id = "ARG-4", teamCode = "ARG", label = "Captain"),
                    count = 2
                )
            )
        )

        val result = StickerShareFormatter.formatFriendMatches(grouped)

        assertEquals(
            "[ARG]\n  ARG-1 – Badge\n  ARG-4 – Captain ×2 on their list",
            result
        )
    }

    private fun sticker(
        id: String,
        teamCode: String,
        label: String,
        quantityOwned: Int = 0
    ) = StickerWithQuantity(
        id = id,
        teamCode = teamCode,
        teamName = teamCode,
        teamFlagEmoji = teamCode,
        confederation = "TEST",
        number = 1,
        label = label,
        stickerType = StickerType.PLAYER,
        isShiny = false,
        quantityOwned = quantityOwned
    )
}

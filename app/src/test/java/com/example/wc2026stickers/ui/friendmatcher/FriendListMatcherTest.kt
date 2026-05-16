package com.wc2026stickers.app.ui.friendmatcher

import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.data.db.entities.StickerType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FriendListMatcherTest {
    @Test
    fun `extractFriendStickerCandidates keeps freeform input focused on sticker ids`() {
        val result = extractFriendStickerCandidates(
            """
            [ARG]
            ARG-1 – Badge
            BRA7, zzz99 and mascot text
            """.trimIndent()
        )

        assertEquals(listOf("ARG1", "BRA7", "ZZZ99"), result)
    }

    @Test
    fun `buildFriendListMatcherResult separates matches owned and invalid codes`() = runTest {
        val missing = listOf(
            sticker(id = "ARG-1", teamCode = "ARG", teamName = "Argentina", number = 1, label = "Badge"),
            sticker(id = "FWC-9", teamCode = "FWC", teamName = "World Cup", number = 9, label = "Mascot")
        )
        val knownIds = mapOf(
            "ARG1" to "ARG-1",
            "BRA7" to "BRA-7",
            "FWC9" to "FWC-9"
        )

        val result = buildFriendListMatcherResult(
            input = "ARG-1, BRA7, FWC9 FWC9 ZZZ99",
            missingStickers = missing,
            resolveStickerId = { token -> knownIds[token] }
        )

        assertEquals(5, result.totalEntries)
        assertEquals(listOf("ARG-1", "FWC-9"), result.matches.map { it.sticker.id })
        assertEquals(listOf(1, 2), result.matches.map { it.count })
        assertEquals(listOf(CountedStickerCode("BRA7", 1)), result.alreadyOwned)
        assertEquals(listOf(CountedStickerCode("ZZZ99", 1)), result.invalid)
    }

    private fun sticker(
        id: String,
        teamCode: String,
        teamName: String,
        number: Int,
        label: String
    ) = StickerWithQuantity(
        id = id,
        teamCode = teamCode,
        teamName = teamName,
        teamFlagEmoji = teamCode,
        confederation = "TEST",
        number = number,
        label = label,
        stickerType = StickerType.PLAYER,
        isShiny = false,
        quantityOwned = 0
    )
}

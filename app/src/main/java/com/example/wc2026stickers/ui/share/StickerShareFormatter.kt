package com.wc2026stickers.app.ui.share

import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.ui.friendmatcher.FriendMatchSticker

internal object StickerShareFormatter {
    fun formatMissing(grouped: Map<String, List<StickerWithQuantity>>): String =
        buildSectionedText(grouped) { sticker ->
            "  ${sticker.id} – ${sticker.label}"
        }

    fun formatDuplicates(grouped: Map<String, List<StickerWithQuantity>>): String =
        buildSectionedText(grouped) { sticker ->
            "  ${sticker.id} – ${sticker.label} ×${sticker.quantityOwned - 1} extra"
        }

    fun formatFriendMatches(grouped: Map<String, List<FriendMatchSticker>>): String =
        buildSectionedText(grouped) { match ->
            buildString {
                append("  ${match.sticker.id} – ${match.sticker.label}")
                if (match.count > 1) {
                    append(" ×${match.count} on their list")
                }
            }
        }

    private fun <T> buildSectionedText(
        grouped: Map<String, List<T>>,
        lineBuilder: (T) -> String
    ): String = buildString {
        grouped.forEach { (teamCode, stickers) ->
            appendLine("[$teamCode]")
            stickers.forEach { sticker ->
                appendLine(lineBuilder(sticker))
            }
        }
    }.trimEnd()
}

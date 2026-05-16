package com.wc2026stickers.app.data

internal data class StickerCode(
    val teamCode: String,
    val number: Int
)

internal object StickerCodeParser {
    private val pattern = Regex("^([A-Z]{2,3})(\\d{1,2})$")

    fun parse(raw: String): StickerCode? {
        val normalized = raw.trim().uppercase().replace("-", "")
        val match = pattern.matchEntire(normalized) ?: return null
        val number = match.groupValues[2].toIntOrNull() ?: return null
        return StickerCode(
            teamCode = match.groupValues[1],
            number = number
        )
    }
}

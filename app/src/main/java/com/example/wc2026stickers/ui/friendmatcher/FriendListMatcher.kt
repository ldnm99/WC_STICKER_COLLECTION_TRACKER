package com.wc2026stickers.app.ui.friendmatcher

import com.wc2026stickers.app.data.db.dao.StickerWithQuantity

// Matches sticker codes optionally followed by xN quantity suffix (from QR codes)
private val stickerIdCandidatePattern = Regex("\\b([A-Za-z]{2,3}-?\\d+)(?:[xX](\\d+))?\\b")

data class CountedStickerCode(
    val code: String,
    val count: Int
) {
    val displayLabel: String
        get() = if (count > 1) "$code ×$count" else code
}

data class FriendMatchSticker(
    val sticker: StickerWithQuantity,
    val count: Int
)

data class FriendListMatcherResult(
    val totalEntries: Int = 0,
    val matches: List<FriendMatchSticker> = emptyList(),
    val alreadyOwned: List<CountedStickerCode> = emptyList(),
    val invalid: List<CountedStickerCode> = emptyList()
) {
    val matchedEntryCount: Int
        get() = matches.sumOf { it.count }

    val alreadyOwnedEntryCount: Int
        get() = alreadyOwned.sumOf { it.count }

    val invalidEntryCount: Int
        get() = invalid.sumOf { it.count }
}

/**
 * Extracts sticker codes from free text or QR-format strings.
 * Handles plain codes (ARG1, ARG-1) and QR quantity format (ARG1x2 → two copies of ARG1).
 * Returns a flat list of codes, repeating entries to represent quantity.
 */
fun extractFriendStickerCandidates(input: String): List<String> {
    // Strip the WC2026: prefix if scanning from a generated QR code
    val normalizedInput = if (input.startsWith("WC2026:", ignoreCase = true)) {
        input.substring(7)
    } else {
        input
    }
    return stickerIdCandidatePattern.findAll(normalizedInput).flatMap { match ->
        val code = match.groupValues[1].normalizeStickerCode()
        val qty = match.groupValues[2].toIntOrNull()?.coerceAtLeast(1) ?: 1
        List(qty) { code }
    }.toList()
}

suspend fun buildFriendListMatcherResult(
    input: String,
    missingStickers: List<StickerWithQuantity>,
    resolveStickerId: suspend (String) -> String?
): FriendListMatcherResult {
    val candidates = extractFriendStickerCandidates(input)
    if (candidates.isEmpty()) {
        return FriendListMatcherResult()
    }

    val missingById = missingStickers.associateBy { it.id }
    val matchedCounts = linkedMapOf<String, Int>()
    val alreadyOwnedCounts = linkedMapOf<String, Int>()
    val invalidCounts = linkedMapOf<String, Int>()

    candidates.forEach { candidate ->
        val stickerId = resolveStickerId(candidate)
        when {
            stickerId == null -> invalidCounts.increment(candidate)
            stickerId in missingById -> matchedCounts.increment(stickerId)
            else -> alreadyOwnedCounts.increment(stickerId.toDisplayStickerCode())
        }
    }

    val matches = matchedCounts
        .mapNotNull { (stickerId, count) ->
            missingById[stickerId]?.let { sticker ->
                FriendMatchSticker(sticker = sticker, count = count)
            }
        }
        .sortedWith(
            compareBy<FriendMatchSticker>(
                { it.sticker.teamCode },
                { it.sticker.number },
                { it.sticker.id }
            )
        )

    return FriendListMatcherResult(
        totalEntries = candidates.size,
        matches = matches,
        alreadyOwned = alreadyOwnedCounts.toCountedStickerCodes(),
        invalid = invalidCounts.toCountedStickerCodes()
    )
}

private fun MutableMap<String, Int>.increment(key: String) {
    this[key] = (this[key] ?: 0) + 1
}

private fun Map<String, Int>.toCountedStickerCodes(): List<CountedStickerCode> =
    entries.map { (code, count) -> CountedStickerCode(code = code, count = count) }

private fun String.normalizeStickerCode(): String = trim().uppercase().replace("-", "")

private fun String.toDisplayStickerCode(): String = replace("-", "")

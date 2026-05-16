package com.wc2026stickers.app.ui.collection

import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.data.db.entities.StickerType

enum class StickerCollectionSortOption(val label: String) {
    TEAM_NUMBER("Team"),
    LABEL("Name"),
    STICKER_TYPE("Type"),
    OWNED_DESC("Copies")
}

data class StickerCollectionFilterState(
    val confederation: String? = null,
    val stickerType: StickerType? = null,
    val shinyOnly: Boolean = false,
    val sortOption: StickerCollectionSortOption = StickerCollectionSortOption.TEAM_NUMBER
) {
    fun hasActiveFilters(defaultState: StickerCollectionFilterState = StickerCollectionFilterState()): Boolean =
        this != defaultState
}

data class StickerCollectionUiState(
    val stickers: List<StickerWithQuantity> = emptyList(),
    val totalCount: Int = 0,
    val filterState: StickerCollectionFilterState = StickerCollectionFilterState(),
    val availableConfederations: List<String> = emptyList()
)

fun buildStickerCollectionUiState(
    source: List<StickerWithQuantity>,
    filterState: StickerCollectionFilterState
): StickerCollectionUiState = StickerCollectionUiState(
    stickers = source.applyStickerCollectionFilters(filterState),
    totalCount = source.size,
    filterState = filterState,
    availableConfederations = source
        .map { it.confederation }
        .distinct()
        .sortedWith(compareBy({ confederationSortIndex(it) }, { it }))
)

fun normalizeStickerSearchInput(input: String): String =
    input.trim().replace(Regex("\\s+"), " ").uppercase()

fun List<StickerWithQuantity>.applyStickerCollectionFilters(
    filterState: StickerCollectionFilterState
): List<StickerWithQuantity> = asSequence()
    .filter { sticker ->
        filterState.confederation == null || sticker.confederation == filterState.confederation
    }
    .filter { sticker ->
        filterState.stickerType == null || sticker.stickerType == filterState.stickerType
    }
    .filter { sticker ->
        !filterState.shinyOnly || sticker.isShiny
    }
    .sortedWith(filterState.sortOption.toComparator())
    .toList()

fun stickerTypeDisplayLabel(type: StickerType): String = when (type) {
    StickerType.BADGE -> "Badge"
    StickerType.TEAM_PHOTO -> "Team photo"
    StickerType.PLAYER -> "Player"
    StickerType.SPECIAL -> "Special"
}

fun confederationDisplayLabel(confederation: String): String = when (confederation) {
    "SPECIAL" -> "Special"
    "CONCACAF" -> "CONCACAF"
    "UEFA" -> "UEFA"
    "CONMEBOL" -> "CONMEBOL"
    "CAF" -> "CAF"
    "AFC" -> "AFC"
    "OFC" -> "OFC"
    else -> confederation
}

private fun StickerCollectionSortOption.toComparator(): Comparator<StickerWithQuantity> {
    val baseComparator = compareBy<StickerWithQuantity>(
        { confederationSortIndex(it.confederation) },
        { it.teamCode },
        { it.number },
        { it.id }
    )
    return when (this) {
        StickerCollectionSortOption.TEAM_NUMBER -> baseComparator
        StickerCollectionSortOption.LABEL -> compareBy<StickerWithQuantity>({ it.label.uppercase() }, { it.teamCode }, { it.number })
        StickerCollectionSortOption.STICKER_TYPE -> compareBy<StickerWithQuantity>(
            { stickerTypeSortIndex(it.stickerType) },
            { it.label.uppercase() },
            { it.teamCode },
            { it.number }
        )
        StickerCollectionSortOption.OWNED_DESC -> compareByDescending<StickerWithQuantity> { it.quantityOwned }
            .then(baseComparator)
    }
}

private fun stickerTypeSortIndex(type: StickerType): Int = when (type) {
    StickerType.BADGE -> 0
    StickerType.TEAM_PHOTO -> 1
    StickerType.PLAYER -> 2
    StickerType.SPECIAL -> 3
}

private fun confederationSortIndex(confederation: String): Int = when (confederation) {
    "SPECIAL" -> 0
    "CONCACAF" -> 1
    "UEFA" -> 2
    "CONMEBOL" -> 3
    "CAF" -> 4
    "AFC" -> 5
    "OFC" -> 6
    else -> 7
}

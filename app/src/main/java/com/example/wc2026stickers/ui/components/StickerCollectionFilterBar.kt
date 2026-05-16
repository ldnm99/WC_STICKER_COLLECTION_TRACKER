package com.wc2026stickers.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wc2026stickers.app.data.db.entities.StickerType
import com.wc2026stickers.app.ui.collection.StickerCollectionFilterState
import com.wc2026stickers.app.ui.collection.StickerCollectionSortOption
import com.wc2026stickers.app.ui.collection.StickerCollectionUiState
import com.wc2026stickers.app.ui.collection.confederationDisplayLabel
import com.wc2026stickers.app.ui.collection.stickerTypeDisplayLabel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StickerCollectionFilterBar(
    uiState: StickerCollectionUiState,
    defaultFilterState: StickerCollectionFilterState,
    sortOptions: List<StickerCollectionSortOption>,
    onFilterChange: (StickerCollectionFilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    val activeLabels = remember(uiState.filterState, defaultFilterState) {
        buildList {
            uiState.filterState.confederation?.let { add(confederationDisplayLabel(it)) }
            uiState.filterState.stickerType?.let { add(stickerTypeDisplayLabel(it)) }
            if (uiState.filterState.shinyOnly) add("Shiny only")
            if (uiState.filterState.sortOption != defaultFilterState.sortOption) {
                add("Sort: ${uiState.filterState.sortOption.label}")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Showing ${uiState.stickers.size} of ${uiState.totalCount}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (activeLabels.isEmpty()) "No extra filters applied" else activeLabels.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = { showSheet = true }) {
                androidx.compose.material3.Icon(Icons.Default.Tune, contentDescription = null)
                Text("Filter & sort", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (activeLabels.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeLabels.forEach { label ->
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(label) }
                    )
                }
                TextButton(onClick = { onFilterChange(defaultFilterState) }) {
                    Text("Reset")
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            StickerCollectionFilterSheet(
                currentState = uiState.filterState,
                defaultFilterState = defaultFilterState,
                confederations = uiState.availableConfederations,
                sortOptions = sortOptions,
                onFilterChange = onFilterChange,
                onDone = { showSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StickerCollectionFilterSheet(
    currentState: StickerCollectionFilterState,
    defaultFilterState: StickerCollectionFilterState,
    confederations: List<String>,
    sortOptions: List<StickerCollectionSortOption>,
    onFilterChange: (StickerCollectionFilterState) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Filter stickers",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        FilterSection(title = "Confederation") {
            ChoiceChip(
                label = "All",
                selected = currentState.confederation == null,
                onClick = { onFilterChange(currentState.copy(confederation = null)) }
            )
            confederations.forEach { confederation ->
                ChoiceChip(
                    label = confederationDisplayLabel(confederation),
                    selected = currentState.confederation == confederation,
                    onClick = { onFilterChange(currentState.copy(confederation = confederation)) }
                )
            }
        }

        FilterSection(title = "Sticker type") {
            ChoiceChip(
                label = "All",
                selected = currentState.stickerType == null,
                onClick = { onFilterChange(currentState.copy(stickerType = null)) }
            )
            StickerType.entries.forEach { type ->
                ChoiceChip(
                    label = stickerTypeDisplayLabel(type),
                    selected = currentState.stickerType == type,
                    onClick = { onFilterChange(currentState.copy(stickerType = type)) }
                )
            }
        }

        FilterSection(title = "Sort by") {
            sortOptions.forEach { sortOption ->
                ChoiceChip(
                    label = sortOption.label,
                    selected = currentState.sortOption == sortOption,
                    onClick = { onFilterChange(currentState.copy(sortOption = sortOption)) }
                )
            }
        }

        FilterSection(title = "Highlights") {
            FilterChip(
                selected = currentState.shinyOnly,
                onClick = { onFilterChange(currentState.copy(shinyOnly = !currentState.shinyOnly)) },
                label = { Text("Shiny only") }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onFilterChange(defaultFilterState) }) {
                Text("Reset")
            }
            Button(onClick = onDone) {
                Text("Done")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

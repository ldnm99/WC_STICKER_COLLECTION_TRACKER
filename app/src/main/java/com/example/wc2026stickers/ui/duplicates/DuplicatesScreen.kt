package com.wc2026stickers.app.ui.duplicates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.ui.collection.StickerCollectionFilterState
import com.wc2026stickers.app.ui.collection.StickerCollectionSortOption
import com.wc2026stickers.app.ui.collection.confederationDisplayLabel
import com.wc2026stickers.app.ui.collection.stickerTypeDisplayLabel
import com.wc2026stickers.app.ui.collection.StickerCollectionUiState
import com.wc2026stickers.app.ui.components.StickerCollectionFilterBar
import com.wc2026stickers.app.ui.share.StickerShareFormatter
import kotlinx.coroutines.launch

private val DuplicatesDefaultFilters = StickerCollectionFilterState()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicatesScreen(
    onBack: () -> Unit,
    viewModel: DuplicatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val duplicates = uiState.stickers
    val grouped = duplicates.groupBy { it.teamCode }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Duplicates (${duplicates.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (duplicates.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val text = StickerShareFormatter.formatDuplicates(grouped)
                        clipboardManager.setText(AnnotatedString(text))
                        scope.launch { snackbarHostState.showSnackbar("Duplicates copied to clipboard") }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy duplicates list")
                }
            }
        }
    ) { padding ->
        when {
            uiState.totalCount == 0 -> {
                EmptyDuplicatesState(modifier = Modifier.padding(padding))
            }

            duplicates.isEmpty() -> {
                EmptyFilteredDuplicatesState(
                    modifier = Modifier.padding(padding),
                    uiState = uiState,
                    sortOptions = viewModel.sortOptions,
                    onFilterChange = viewModel::setFilters
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    item {
                        StickerCollectionFilterBar(
                            uiState = uiState,
                            defaultFilterState = DuplicatesDefaultFilters,
                            sortOptions = viewModel.sortOptions,
                            onFilterChange = viewModel::setFilters
                        )
                    }

                    if (uiState.filterState.sortOption == StickerCollectionSortOption.TEAM_NUMBER) {
                        grouped.forEach { (_, stickers) ->
                            val first = stickers.first()
                            item(key = "header-${first.teamCode}") {
                                TeamHeader(first = first)
                            }
                            items(stickers, key = { it.id }) { sticker ->
                                DuplicateRow(sticker = sticker, showTeam = false)
                            }
                        }
                    } else {
                        items(duplicates, key = { it.id }) { sticker ->
                            DuplicateRow(sticker = sticker, showTeam = true)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyDuplicatesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📋", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No duplicates yet", style = MaterialTheme.typography.titleLarge)
            Text(
                "Any sticker with 2 or more copies will show up here for trading.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun EmptyFilteredDuplicatesState(
    uiState: StickerCollectionUiState,
    sortOptions: List<StickerCollectionSortOption>,
    onFilterChange: (StickerCollectionFilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        StickerCollectionFilterBar(
            uiState = uiState,
            defaultFilterState = DuplicatesDefaultFilters,
            sortOptions = sortOptions,
            onFilterChange = onFilterChange
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧭", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No duplicates match these filters", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Try a different confederation, sticker type, or include non-shiny stickers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TeamHeader(first: StickerWithQuantity) {
    Text(
        text = "${first.teamFlagEmoji} ${first.teamName} (${first.teamCode})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
    )
    HorizontalDivider()
}

@Composable
private fun DuplicateRow(
    sticker: StickerWithQuantity,
    showTeam: Boolean
) {
    ListItem(
        modifier = Modifier.semantics {
            stateDescription = "${sticker.quantityOwned - 1} extra copies available"
        },
        leadingContent = {
            Text(
                text = "${sticker.teamCode}-${sticker.number}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        headlineContent = { Text(sticker.label) },
        supportingContent = {
            Text(
                duplicateSupportingText(sticker, showTeam),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        trailingContent = {
            Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                Text(
                    "×${sticker.quantityOwned - 1} extra",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

private fun duplicateSupportingText(sticker: StickerWithQuantity, showTeam: Boolean): String = buildString {
    if (showTeam) {
        append("${sticker.teamFlagEmoji} ${sticker.teamName} • ")
    }
    append(stickerTypeDisplayLabel(sticker.stickerType))
    append(" • ")
    append(confederationDisplayLabel(sticker.confederation))
    if (sticker.isShiny) {
        append(" • Shiny")
    }
}

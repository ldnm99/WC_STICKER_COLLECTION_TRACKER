package com.wc2026stickers.app.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.ui.collection.StickerCollectionFilterState
import com.wc2026stickers.app.ui.collection.StickerCollectionSortOption
import com.wc2026stickers.app.ui.components.StickerCollectionFilterBar

private val SearchDefaultFilters = StickerCollectionFilterState()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val results = uiState.stickers
    val grouped = results.groupBy { it.teamCode }
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::onQueryChanged,
                        label = { Text("Search stickers") },
                        placeholder = { Text("Search stickers…") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when {
            query.length < 2 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Enter at least 2 characters", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Search by sticker ID, label, or team code, for example ARG1 or BRA.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            results.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                ) {
                    StickerCollectionFilterBar(
                        uiState = uiState,
                        defaultFilterState = SearchDefaultFilters,
                        sortOptions = viewModel.sortOptions,
                        onFilterChange = viewModel::setFilters
                    )
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😕", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No stickers found for “$query”", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Try a shorter term, a team code, or check the sticker number.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
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
                            defaultFilterState = SearchDefaultFilters,
                            sortOptions = viewModel.sortOptions,
                            onFilterChange = viewModel::setFilters
                        )
                    }
                    if (uiState.filterState.sortOption == StickerCollectionSortOption.TEAM_NUMBER) {
                        grouped.forEach { (teamCode, stickers) ->
                            item {
                                Text(
                                    text = teamCode,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                                )
                                HorizontalDivider()
                            }
                            items(stickers, key = { it.id }) { sticker ->
                                SearchResultRow(sticker)
                            }
                        }
                    } else {
                        items(results, key = { it.id }) { sticker ->
                            SearchResultRow(sticker)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
private fun SearchResultRow(sticker: StickerWithQuantity) {
    ListItem(
        modifier = Modifier.semantics {
            stateDescription = when {
                sticker.quantityOwned == 0 -> "Missing"
                sticker.quantityOwned == 1 -> "Collected once"
                else -> "${sticker.quantityOwned} copies owned"
            }
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
                sticker.stickerType.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        trailingContent = {
            when {
                sticker.quantityOwned == 0 -> Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text("Missing", color = Color.White, fontWeight = FontWeight.Bold)
                }
                sticker.quantityOwned == 1 -> Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("×1", color = Color.White, fontWeight = FontWeight.Bold)
                }
                else -> Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                    Text("×${sticker.quantityOwned}", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

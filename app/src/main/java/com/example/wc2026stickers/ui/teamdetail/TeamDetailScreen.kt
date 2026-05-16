package com.wc2026stickers.app.ui.teamdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.data.db.entities.StickerType
import com.wc2026stickers.app.ui.components.StickerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    onBack: () -> Unit,
    onNavigateToQuickAdd: () -> Unit,
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    val stickers by viewModel.stickers.collectAsStateWithLifecycle()
    val teamName by viewModel.teamName.collectAsStateWithLifecycle()
    val team by viewModel.team.collectAsStateWithLifecycle()
    var selectedSticker by remember { mutableStateOf<StickerWithQuantity?>(null) }

    val collectedCount = stickers.count { it.quantityOwned > 0 }
    val isFavorite = team?.isFavorite == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(teamName, fontWeight = FontWeight.Bold)
                        Text(
                            "$collectedCount / ${stickers.size} collected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::toggleFavorite,
                        enabled = team != null
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (isFavorite) {
                                "Remove team from favorites"
                            } else {
                                "Add team to favorites"
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToQuickAdd,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick add stickers")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TeamActionSummaryCard(
                    isFavorite = isFavorite,
                    stickers = stickers,
                    collectedCount = collectedCount,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onNavigateToQuickAdd = onNavigateToQuickAdd
                )
            }
            items(stickers, key = { it.id }) { sticker ->
                StickerCard(
                    sticker = sticker,
                    onClick = { selectedSticker = sticker }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Quantity bottom sheet
    selectedSticker?.let { sticker ->
        QuantityBottomSheet(
            sticker = sticker,
            onDismiss = { selectedSticker = null },
            onSetQuantity = { qty ->
                viewModel.setQuantity(sticker.id, qty)
                selectedSticker = null
            }
        )
    }
}

@Composable
private fun TeamActionSummaryCard(
    isFavorite: Boolean,
    stickers: List<StickerWithQuantity>,
    collectedCount: Int,
    onToggleFavorite: () -> Unit,
    onNavigateToQuickAdd: () -> Unit
) {
    val totalCount = stickers.size
    val missingCount = (totalCount - collectedCount).coerceAtLeast(0)
    val duplicateExtras = stickers.sumOf { (it.quantityOwned - 1).coerceAtLeast(0) }
    val badgeCollected = stickers
        .firstOrNull { it.stickerType == StickerType.BADGE }
        ?.quantityOwned
        ?.let { it > 0 }
        ?: true
    val photoCollected = stickers
        .firstOrNull { it.stickerType == StickerType.TEAM_PHOTO }
        ?.quantityOwned
        ?.let { it > 0 }
        ?: true

    val headline = when {
        totalCount == 0 -> "Loading section progress…"
        missingCount == 0 && duplicateExtras > 0 -> "Section complete — $duplicateExtras extra${if (duplicateExtras == 1) "" else "s"} ready to trade"
        missingCount == 0 -> "Section complete — nothing left to add here"
        !badgeCollected -> "Start with the badge sticker"
        !photoCollected -> "Grab the team photo next"
        missingCount in 1..3 -> "Finish line: $missingCount sticker${if (missingCount == 1) "" else "s"} left"
        else -> "$missingCount stickers still missing"
    }
    val supportingText = when {
        totalCount == 0 -> "Your sticker list will appear here in a moment."
        missingCount == 0 && duplicateExtras > 0 -> "Everything is collected in this team. Review duplicates below or keep using Quick Add for trades."
        missingCount == 0 -> "Tap any sticker below to review quantities, or jump to another team from the progress screens."
        !badgeCollected -> "Collecting the badge makes this section header feel complete. You can also tap any sticker below to update its quantity."
        !photoCollected -> "The team photo anchors the section header. Add it next, then fill the remaining stickers from the grid."
        missingCount in 1..3 -> "You're close. Tap the missing stickers below or use Quick Add if you already know the sticker IDs."
        else -> "Use this grid to work through the missing stickers, or switch to Quick Add for faster batch entry."
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Next move",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamStatusPill(
                    text = if (badgeCollected) "Badge collected" else "Badge missing",
                    modifier = Modifier.weight(1f)
                )
                TeamStatusPill(
                    text = if (photoCollected) "Photo collected" else "Photo missing",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamStatusPill(
                    text = if (missingCount == 0) "Team complete" else "$missingCount left",
                    modifier = Modifier.weight(1f)
                )
                TeamStatusPill(
                    text = if (duplicateExtras == 0) "No extras" else "$duplicateExtras extras",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isFavorite) "Unfavorite team" else "Favorite team")
                }
                FilledTonalButton(
                    onClick = onNavigateToQuickAdd,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Quick Add")
                }
            }
        }
    }
}

@Composable
private fun TeamStatusPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuantityBottomSheet(
    sticker: StickerWithQuantity,
    onDismiss: () -> Unit,
    onSetQuantity: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "${sticker.teamCode}-${sticker.number}: ${sticker.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "How many do you have?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick set buttons 0–5
                (0..5).forEach { qty ->
                    val isSelected = sticker.quantityOwned == qty
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetQuantity(qty) },
                        label = { Text("$qty") },
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = "Set quantity to $qty"
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // + / - controls for higher quantities
            if (sticker.quantityOwned > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedIconButton(
                        onClick = { onSetQuantity(sticker.quantityOwned - 1) },
                        modifier = Modifier.semantics {
                            contentDescription = "Decrease quantity to ${sticker.quantityOwned - 1}"
                        }
                    ) {
                        Text("−", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "${sticker.quantityOwned}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedIconButton(
                        onClick = { onSetQuantity(sticker.quantityOwned + 1) },
                        modifier = Modifier.semantics {
                            contentDescription = "Increase quantity to ${sticker.quantityOwned + 1}"
                        }
                    ) {
                        Text("+", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

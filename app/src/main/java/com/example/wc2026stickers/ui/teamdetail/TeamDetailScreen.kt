package com.example.wc2026stickers.ui.teamdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wc2026stickers.data.db.dao.StickerWithQuantity
import com.example.wc2026stickers.ui.components.StickerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    onBack: () -> Unit,
    onNavigateToQuickAdd: () -> Unit,
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    val stickers by viewModel.stickers.collectAsState()
    val teamName by viewModel.teamName.collectAsState()
    var selectedSticker by remember { mutableStateOf<StickerWithQuantity?>(null) }

    val collectedCount = stickers.count { it.quantityOwned > 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(teamName, fontWeight = FontWeight.Bold)
                        Text(
                            "$collectedCount / ${stickers.size} collected",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToQuickAdd,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick Add")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
                        modifier = Modifier.weight(1f)
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
                    OutlinedIconButton(onClick = { onSetQuantity(sticker.quantityOwned - 1) }) {
                        Text("−", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "${sticker.quantityOwned}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedIconButton(onClick = { onSetQuantity(sticker.quantityOwned + 1) }) {
                        Text("+", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

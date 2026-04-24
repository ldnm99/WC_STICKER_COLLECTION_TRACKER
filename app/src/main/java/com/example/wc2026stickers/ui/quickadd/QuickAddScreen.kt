package com.example.wc2026stickers.ui.quickadd

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddScreen(
    onBack: () -> Unit,
    viewModel: QuickAddViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Add", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How to use", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Type sticker IDs separated by spaces or commas.\n" +
                        "Format: team code + number (e.g. ARG1, MEX20, FWC9, ENG15)\n" +
                        "Each sticker added will increase its count by 1.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Text input
            OutlinedTextField(
                value = state.inputText,
                onValueChange = viewModel::onInputChanged,
                label = { Text("Sticker IDs") },
                placeholder = { Text("e.g. ARG1 MEX20 ENG5 FWC9") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            // Live validation chips
            if (state.entries.isNotEmpty()) {
                Text("Validation:", fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.entries) { entry ->
                        val isValid = entry.stickerId != null
                        AssistChip(
                            onClick = {},
                            label = { Text(entry.raw) },
                            leadingIcon = {
                                Text(if (isValid) "✅" else "❌")
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isValid)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            )
                        )
                    }
                }

                val validCount = state.entries.count { it.stickerId != null }
                val invalidCount = state.entries.count { it.stickerId == null }
                Text(
                    buildString {
                        append("$validCount valid")
                        if (invalidCount > 0) append(" · $invalidCount unrecognized")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Success banner
            AnimatedVisibility(visible = state.showSuccess) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✅", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${state.addedCount} sticker${if (state.addedCount != 1) "s" else ""} added to your collection!",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Add button
            val validEntries = state.entries.count { it.stickerId != null }
            Button(
                onClick = viewModel::addAll,
                modifier = Modifier.fillMaxWidth(),
                enabled = validEntries > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (validEntries > 0) "Add $validEntries sticker${if (validEntries != 1) "s" else ""} to Collection"
                    else "Enter sticker IDs above"
                )
            }
        }
    }
}

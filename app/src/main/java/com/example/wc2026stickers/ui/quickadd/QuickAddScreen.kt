package com.wc2026stickers.app.ui.quickadd

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddScreen(
    onBack: (() -> Unit)? = null,
    viewModel: QuickAddViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Add", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
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

            AnimatedVisibility(visible = state.sessionSummary != null) {
                val summary = state.sessionSummary ?: return@AnimatedVisibility
                QuickAddSessionSummaryCard(
                    addedCount = state.addedCount,
                    summary = summary
                )
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

@Composable
private fun QuickAddSessionSummaryCard(
    addedCount: Int,
    summary: QuickAddSessionSummary
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✅", style = MaterialTheme.typography.titleLarge)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Session summary", fontWeight = FontWeight.Bold)
                    Text(
                        "${addedCount} sticker${if (addedCount != 1) "s" else ""} logged.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            SummarySection(
                title = "New to collection",
                summary = if (summary.newStickers.isNotEmpty()) {
                    "${summary.newStickerCount} first-time sticker${if (summary.newStickerCount != 1) "s" else ""}"
                } else {
                    "No first-time stickers this round"
                },
                details = summary.newStickers.toStickerSummaryText()
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
            SummarySection(
                title = "Became duplicates",
                summary = if (summary.duplicateStickers.isNotEmpty()) {
                    "${summary.duplicateStickerCount} duplicate extra${if (summary.duplicateStickerCount != 1) "s" else ""} created"
                } else {
                    "No new duplicates this round"
                },
                details = summary.duplicateStickers.toStickerSummaryText()
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
            SummarySection(
                title = "Closer to completion",
                summary = if (summary.progressedTeams.isNotEmpty()) {
                    "${summary.progressedTeams.size} team${if (summary.progressedTeams.size != 1) "s" else ""} moved forward"
                } else {
                    "No teams moved closer this round"
                },
                details = summary.progressedTeams.toTeamSummaryText()
            )
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    summary: String,
    details: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(summary, style = MaterialTheme.typography.bodyMedium)
        if (!details.isNullOrBlank()) {
            Text(
                details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

private fun List<QuickAddStickerSessionDelta>.toStickerSummaryText(limit: Int = 6): String? {
    if (isEmpty()) return null
    val visibleItems = take(limit).joinToString(", ") { delta ->
        buildString {
            append(delta.stickerId.toQuickAddDisplayCode())
            if (delta.count > 1) append(" ×${delta.count}")
        }
    }
    val overflowCount = size - limit
    return if (overflowCount > 0) "$visibleItems +$overflowCount more" else visibleItems
}

private fun List<QuickAddTeamSessionDelta>.toTeamSummaryText(limit: Int = 4): String? {
    if (isEmpty()) return null
    val visibleItems = take(limit).joinToString("\n") { delta ->
        buildString {
            append("${delta.flagEmoji} ${delta.name} · ")
            if (delta.isCompleted) {
                append("completed this round")
            } else {
                append("${delta.remainingAfter} left (was ${delta.remainingBefore})")
            }
            append(" · +${delta.collectedDelta}")
        }
    }
    val overflowCount = size - limit
    return if (overflowCount > 0) "$visibleItems\n+$overflowCount more teams" else visibleItems
}

private fun String.toQuickAddDisplayCode(): String = replace("-", "")

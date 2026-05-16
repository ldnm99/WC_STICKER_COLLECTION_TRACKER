package com.wc2026stickers.app.ui.friendmatcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.ui.collection.confederationDisplayLabel
import com.wc2026stickers.app.ui.collection.stickerTypeDisplayLabel
import com.wc2026stickers.app.ui.share.StickerShareFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendMatcherScreen(
    onBack: () -> Unit,
    viewModel: FriendMatcherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val matches = uiState.result.matches
    val groupedMatches = matches.groupBy { it.sticker.teamCode }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Matcher", fontWeight = FontWeight.Bold) },
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
            if (matches.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        clipboardManager.setText(
                            AnnotatedString(StickerShareFormatter.formatFriendMatches(groupedMatches))
                        )
                        scope.launch { snackbarHostState.showSnackbar("Matches copied to clipboard") }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy matched stickers")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MatcherIntroCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onInputChanged,
                    label = { Text("Paste duplicate list") },
                    placeholder = { Text("ARG1, BRA7, FWC9 or ARG-1 on separate lines") },
                    trailingIcon = {
                        if (uiState.inputText.isNotBlank()) {
                            IconButton(onClick = { viewModel.onInputChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear pasted text")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    minLines = 5,
                    maxLines = 10
                )
            }

            if (uiState.inputText.isNotBlank()) {
                item {
                    MatchSummaryCard(
                        uiState = uiState,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            if (uiState.inputText.isNotBlank() && uiState.result.totalEntries == 0) {
                item {
                    InformationalCard(
                        title = "No sticker IDs found yet",
                        body = "Paste codes like ARG1 or ARG-1. Extra text is ignored automatically.",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            if (uiState.result.invalid.isNotEmpty()) {
                item {
                    CodeBucketCard(
                        title = "Unrecognized IDs",
                        body = "These look like sticker codes, but they don't exist in this album.",
                        codes = uiState.result.invalid,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            if (uiState.result.alreadyOwned.isNotEmpty()) {
                item {
                    CodeBucketCard(
                        title = "Already in your collection",
                        body = "Recognized, but you already have these stickers.",
                        codes = uiState.result.alreadyOwned,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            if (uiState.result.totalEntries > 0 && matches.isEmpty()) {
                item {
                    InformationalCard(
                        title = "No missing matches from this list",
                        body = "Good news: nothing in the pasted list is still missing from your album.",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            if (matches.isNotEmpty()) {
                item {
                    Text(
                        text = "You still need",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                groupedMatches.forEach { (_, teamMatches) ->
                    val first = teamMatches.first().sticker
                    item(key = "header-${first.teamCode}") {
                        TeamHeader(
                            title = "${first.teamFlagEmoji} ${first.teamName} (${first.teamCode})",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    items(teamMatches, key = { it.sticker.id }) { match ->
                        FriendMatchRow(
                            match = match,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MatcherIntroCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("How it works", fontWeight = FontWeight.Bold)
            Text(
                "Paste a friend's duplicate list and this screen will match it offline against the stickers you still need.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Accepted formats: ARG1, ARG-1, comma-separated, space-separated, or one code per line.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MatchSummaryCard(
    uiState: FriendMatcherUiState,
    modifier: Modifier = Modifier
) {
    val result = uiState.result
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Summary", fontWeight = FontWeight.Bold)
            Text(
                buildString {
                    append("${result.totalEntries} code")
                    if (result.totalEntries != 1) append("s")
                    append(" found")
                    append(" · ${result.matchedEntryCount} needed")
                    append(" · ${result.alreadyOwnedEntryCount} already owned")
                    append(" · ${result.invalidEntryCount} unrecognized")
                },
                style = MaterialTheme.typography.bodyMedium
            )
            if (result.matches.isNotEmpty()) {
                Text(
                    "${result.matches.size} distinct sticker${if (result.matches.size != 1) "s" else ""} can help you right now.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CodeBucketCard(
    title: String,
    body: String,
    codes: List<CountedStickerCode>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                codes.joinToString(", ") { it.displayLabel },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InformationalCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TeamHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun FriendMatchRow(
    match: FriendMatchSticker,
    modifier: Modifier = Modifier
) {
    val sticker = match.sticker
    ListItem(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        leadingContent = {
            Text(
                text = sticker.id,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        headlineContent = { Text(sticker.label) },
        supportingContent = {
            Text(
                buildFriendMatchSupportingText(sticker),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (match.count > 1) {
                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                    Text("×${match.count}", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

private fun buildFriendMatchSupportingText(sticker: StickerWithQuantity): String =
    buildString {
        append(stickerTypeDisplayLabel(sticker.stickerType))
        append(" • ")
        append(confederationDisplayLabel(sticker.confederation))
        if (sticker.isShiny) {
            append(" • Shiny")
        }
    }

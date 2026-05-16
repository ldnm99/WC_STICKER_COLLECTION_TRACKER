package com.wc2026stickers.app.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wc2026stickers.app.data.backup.CollectionBackupJson
import com.wc2026stickers.app.data.backup.CollectionRestoreMode
import com.wc2026stickers.app.ui.components.StatCard
import com.wc2026stickers.app.ui.history.CollectionHistorySummary
import com.wc2026stickers.app.ui.history.RecentCollectionActivity
import com.wc2026stickers.app.ui.kpi.HomeKpiUiModel
import com.wc2026stickers.app.ui.kpi.KpiPriority
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTeams: () -> Unit,
    onNavigateToTeam: (String) -> Unit,
    onNavigateToMissing: () -> Unit,
    onNavigateToFriendMatcher: () -> Unit,
    onNavigateToDuplicates: () -> Unit,
    onNavigateToQuickAdd: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToKpiRanking: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = if (state.totalCount > 0) state.collectedCount.toFloat() / state.totalCount else 0f
    val snackbarHostState = remember { SnackbarHostState() }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri)
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onSnackbarShown()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("⚽ WC 2026 Stickers", fontWeight = FontWeight.Bold) },
                    actions = {
                        CollectionBackupMenu(
                            enabled = !state.isBackupInProgress,
                            onExportClick = {
                                exportLauncher.launch(CollectionBackupJson.suggestedFileName())
                            },
                            onImportClick = {
                                importLauncher.launch(arrayOf("application/json", "text/plain"))
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
                if (state.isBackupInProgress) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToQuickAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Quick Add") },
                containerColor = MaterialTheme.colorScheme.secondary
            )
        }
    ) { padding ->
        state.pendingRestorePreview?.let { preview ->
            CollectionRestoreDialog(
                preview = preview,
                enabled = !state.isBackupInProgress,
                onDismiss = viewModel::dismissRestorePreview,
                onMerge = { viewModel.restoreBackup(CollectionRestoreMode.MERGE) },
                onReplace = { viewModel.restoreBackup(CollectionRestoreMode.REPLACE) }
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Loading collection summary"
                        }
                    )
                    Text(
                        text = "Loading your collection summary…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "This usually only takes a moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Collection progress
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Collection Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .semantics {
                                progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                            },
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${state.collectedCount} / ${state.totalCount} stickers",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Collected",
                    value = "${state.collectedCount}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Missing",
                    value = "${state.missingCount}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Duplicates",
                    value = "${state.duplicatesCount}",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            FavoriteTeamsSection(
                favoriteTeams = state.favoriteTeams,
                onOpenTeam = onNavigateToTeam,
                onBrowseTeams = onNavigateToTeams
            )

            CollectionHistorySection(history = state.collectionHistory)

            // Navigation grid
            Text("Browse", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeNavCard(
                    emoji = "🏆",
                    label = "Teams",
                    onClick = onNavigateToTeams,
                    modifier = Modifier.weight(1f)
                )
                HomeNavCard(
                    emoji = "❌",
                    label = "Missing",
                    badge = if (state.missingCount > 0) "${state.missingCount}" else null,
                    onClick = onNavigateToMissing,
                    modifier = Modifier.weight(1f)
                )
                HomeNavCard(
                    emoji = "📋",
                    label = "Duplicates",
                    badge = if (state.duplicatesCount > 0) "${state.duplicatesCount}" else null,
                    onClick = onNavigateToDuplicates,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeNavCard(
                    emoji = "🤝",
                    label = "Friend\nMatcher",
                    onClick = onNavigateToFriendMatcher,
                    modifier = Modifier.weight(1f)
                )
                HomeNavCard(
                    emoji = "🔍",
                    label = "Search",
                    onClick = onNavigateToSearch,
                    modifier = Modifier.weight(1f)
                )
                HomeNavCard(
                    emoji = "➕",
                    label = "Quick Add",
                    onClick = onNavigateToQuickAdd,
                    modifier = Modifier.weight(1f)
                )
            }

            KpiInsightsSection(
                insights = state.kpiInsights,
                onInsightClick = { onNavigateToKpiRanking(it.routeKey) }
            )
        }
    }
}

@Composable
private fun CollectionHistorySection(
    history: CollectionHistorySummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Collection history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Recent changes and milestone moments from your sticker collection.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        if (history.recentActivity.isEmpty() && history.reachedMilestones.isEmpty()) {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No history yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add stickers from a team page or Quick Add and your latest activity will show up here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            history.nextMilestone?.let { nextMilestone ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Next milestone",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = nextMilestone.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${nextMilestone.remainingCount} sticker${if (nextMilestone.remainingCount == 1) "" else "s"} to go (${history.collectedCount}/${nextMilestone.targetCount}).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (history.reachedMilestones.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Milestones",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        history.reachedMilestones.takeLast(3).asReversed().forEach { milestone ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = milestone.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${milestone.targetCount} sticker${if (milestone.targetCount == 1) "" else "s"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatHistoryTimestamp(milestone.reachedAt),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (history.recentActivity.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Recent activity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        history.recentActivity.take(4).forEach { activity ->
                            RecentActivityRow(activity = activity)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivityRow(
    activity: RecentCollectionActivity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${activity.teamFlagEmoji} ${activity.stickerId.toDisplayStickerCode()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatHistoryTimestamp(activity.lastUpdatedAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${activity.teamName} · ${activity.label}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = activity.toActivitySummary(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
        )
    }
}

private fun RecentCollectionActivity.toActivitySummary(): String = when {
    isRemoved -> "Removed from the collection."
    isNewSticker -> "First copy collected."
    quantityOwned <= 1 -> "Quantity updated to 1."
    else -> "Quantity updated to $quantityOwned copies."
}

private fun String.toDisplayStickerCode(): String = replace("-", "")

private fun formatHistoryTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val elapsedMinutes = ((now - timestamp).coerceAtLeast(0L)) / 60_000L
    return when {
        elapsedMinutes < 1L -> "Just now"
        elapsedMinutes < 60L -> "${elapsedMinutes}m ago"
        elapsedMinutes < 1_440L -> "${elapsedMinutes / 60L}h ago"
        elapsedMinutes < 10_080L -> "${elapsedMinutes / 1_440L}d ago"
        else -> HISTORY_DATE_FORMATTER.format(
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
        )
    }
}

private val HISTORY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

@Composable
private fun FavoriteTeamsSection(
    favoriteTeams: List<FavoriteTeamHighlight>,
    onOpenTeam: (String) -> Unit,
    onBrowseTeams: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Favorite teams", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = if (favoriteTeams.isEmpty()) {
                    "Star teams from the Teams screen to pin quick shortcuts here."
                } else {
                    "Jump back into the teams you care about most."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        if (favoriteTeams.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBrowseTeams
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No favorites yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Browse teams and tap the star on any team to keep it handy on the home screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Open Teams",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            favoriteTeams.forEach { team ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenTeam(team.code) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = team.flagEmoji, style = MaterialTheme.typography.headlineMedium)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = team.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${team.collectedCount} / ${team.totalCount} collected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = team.summary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiInsightsSection(
    insights: List<HomeKpiUiModel>,
    onInsightClick: (HomeKpiUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val promotedInsights = insights
        .filter { it.priority == KpiPriority.HIGH || it.priority == KpiPriority.MEDIUM }
        .take(2)
        .ifEmpty { insights.take(1) }
    val secondaryInsights = insights.filterNot { promotedInsights.contains(it) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("KPI Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Focus on the next best moves first, then scan the rest at a glance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        promotedInsights.firstOrNull()?.let { insight ->
            FeaturedInsightCard(
                insight = insight,
                onClick = { onInsightClick(insight) }
            )
        }

        promotedInsights.drop(1).forEach { insight ->
            PrimaryInsightCard(
                insight = insight,
                onClick = { onInsightClick(insight) }
            )
        }

        if (secondaryInsights.isNotEmpty()) {
            Text(
                text = "More insights",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            secondaryInsights.chunked(2).forEach { rowInsights ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowInsights.forEach { insight ->
                        CompactInsightCard(
                            insight = insight,
                            onClick = { onInsightClick(insight) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowInsights.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedInsightCard(
    insight: HomeKpiUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = insightPriorityColor(insight.priority)
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    insight.context?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                InsightPriorityBadge(
                    label = insight.priority.label,
                    accentColor = accentColor
                )
            }
            Text(
                text = insight.headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = insight.supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = accentColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = insight.cta.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PrimaryInsightCard(
    insight: HomeKpiUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = insightPriorityColor(insight.priority)
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = insight.headline,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = insight.supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InsightPriorityBadge(
                    label = insight.priority.label,
                    accentColor = accentColor
                )
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleLarge,
                    color = accentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CompactInsightCard(
    insight: HomeKpiUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = insightPriorityColor(insight.priority)
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 152.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = insight.headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = insight.supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = insight.cta.label,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InsightPriorityBadge(
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = accentColor.copy(alpha = 0.14f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun insightPriorityColor(priority: KpiPriority): Color = when (priority) {
    KpiPriority.HIGH -> MaterialTheme.colorScheme.error
    KpiPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
    KpiPriority.POSITIVE -> MaterialTheme.colorScheme.primary
    KpiPriority.NEUTRAL -> MaterialTheme.colorScheme.outline
}

@Composable
private fun HomeNavCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 88.dp)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (badge != null) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

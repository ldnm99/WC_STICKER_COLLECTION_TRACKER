package com.wc2026stickers.app.ui.kpiranking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wc2026stickers.app.data.db.dao.TeamKpiStats
import com.wc2026stickers.app.ui.kpi.KpiRankingEmptyStateUiModel
import com.wc2026stickers.app.ui.kpi.KpiRankingSectionUiModel
import com.wc2026stickers.app.ui.kpi.KpiType
import com.wc2026stickers.app.ui.kpi.confederationDisplayName
import com.wc2026stickers.app.ui.kpi.kpiRankingActionLabel
import com.wc2026stickers.app.ui.kpi.kpiRankingMetric
import com.wc2026stickers.app.ui.kpi.kpiRankingSupportingText
import com.wc2026stickers.app.ui.kpi.shouldShowRankingProgressBar
import com.wc2026stickers.app.ui.kpi.toKpiRankingPresentation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpiRankingScreen(
    onBack: () -> Unit,
    onTeamClick: (String) -> Unit,
    viewModel: KpiRankingViewModel = hiltViewModel()
) {
    val teams by viewModel.rankedTeams.collectAsStateWithLifecycle()
    val kpiType = viewModel.kpiType
    val title = kpiType.rankingTitle
    val presentation = teams.toKpiRankingPresentation(kpiType)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                RankingIntroCard(
                    title = presentation.introTitle,
                    body = presentation.introBody,
                    orderLabel = presentation.orderLabel
                )
            }
            presentation.emptyState?.let { emptyState ->
                item {
                    RankingEmptyStateCard(emptyState = emptyState)
                }
            }
            presentation.sections.forEach { section ->
                item(key = "section-${section.title}") {
                    RankingSectionHeader(section = section)
                }
                itemsIndexed(section.teams, key = { _, t -> "${section.title}-${t.code}" }) { index, team ->
                    KpiTeamRow(
                        rank = index + 1,
                        team = team,
                        kpiType = kpiType,
                        onClick = { onTeamClick(team.code) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun KpiTeamRow(
    rank: Int,
    team: TeamKpiStats,
    kpiType: KpiType,
    onClick: () -> Unit
) {
    val metricText = team.kpiRankingMetric(kpiType)
    val detailText = team.kpiRankingSupportingText(kpiType)
    val completionPct = if (team.totalCount > 0) team.collectedCount.toFloat() / team.totalCount else 0f
    val green = MaterialTheme.colorScheme.primary
    val red = MaterialTheme.colorScheme.error
    val orange = MaterialTheme.colorScheme.secondary
    val neutral = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val metricColor: Color = when (kpiType) {
        KpiType.MISSING_BADGES -> if (team.badgeCollected == 1) green else red
        KpiType.MISSING_TEAM_PHOTOS -> if (team.teamPhotoCollected == 1) green else red
        KpiType.MOST_DUPLICATES -> if (team.duplicateExtraCount > 0) orange else neutral
        KpiType.ALMOST_COMPLETE -> {
            val missing = team.totalCount - team.collectedCount
            when {
                missing == 0 -> green
                missing in 1..3 -> orange
                else -> neutral
            }
        }
        else -> MaterialTheme.colorScheme.primary
    }
    val actionLabel = team.kpiRankingActionLabel(kpiType)

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Open ${team.name} details", onClick = onClick)
            .semantics {
                stateDescription = metricText
            },
        leadingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.width(28.dp),
                    fontWeight = FontWeight.Bold
                )
                Text(text = team.flagEmoji, fontSize = 28.sp)
            }
        },
        headlineContent = {
            Text(team.name, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = confederationDisplayName(team.confederation),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = metricColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = actionLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = metricColor
                    )
                }
                if (kpiType.shouldShowRankingProgressBar()) {
                    LinearProgressIndicator(
                        progress = { completionPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .semantics {
                                progressBarRangeInfo = ProgressBarRangeInfo(completionPct, 0f..1f)
                            },
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        },
        trailingContent = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = metricColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = metricText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = metricColor
                )
            }
        }
    )
}

@Composable
private fun RankingIntroCard(
    title: String,
    body: String,
    orderLabel: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap a team to open its sticker grid and update quantities right away.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(orderLabel) }
            )
        }
    }
}

@Composable
private fun RankingEmptyStateCard(emptyState: KpiRankingEmptyStateUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = emptyState.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = emptyState.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RankingSectionHeader(section: KpiRankingSectionUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        section.summary?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

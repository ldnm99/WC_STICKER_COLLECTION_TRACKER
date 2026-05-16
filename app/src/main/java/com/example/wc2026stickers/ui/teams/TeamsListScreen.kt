package com.wc2026stickers.app.ui.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wc2026stickers.app.data.db.dao.TeamWithProgress
import com.wc2026stickers.app.ui.components.TeamProgressRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsListScreen(
    onBack: () -> Unit,
    onTeamClick: (String) -> Unit,
    onNavigateToQuickAdd: () -> Unit,
    viewModel: TeamsViewModel = hiltViewModel()
) {
    val teams by viewModel.teams.collectAsStateWithLifecycle()
    val focusTeam = teams.nextFocusTeam()
    val favoriteTeams = teams.filter { it.isFavorite }.sortedBy { it.sortOrder }

    // Group by confederation, special section always first
    val grouped = teams
        .sortedBy { it.sortOrder }
        .groupBy { it.confederation }

    val confOrder = listOf("SPECIAL", "CONCACAF", "UEFA", "CONMEBOL", "CAF", "AFC", "OFC")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teams", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Default.Add, contentDescription = "Quick add stickers")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                TeamProgressFocusCard(
                    focusTeam = focusTeam,
                    onOpenTeam = { focusTeam?.let { onTeamClick(it.code) } },
                    onQuickAdd = onNavigateToQuickAdd
                )
            }
            if (favoriteTeams.isNotEmpty()) {
                item {
                    Text(
                        text = "⭐ Favorite teams",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                    HorizontalDivider()
                }
                items(favoriteTeams, key = { "favorite-${it.code}" }) { team ->
                    TeamProgressRow(
                        flag = team.flagEmoji,
                        name = team.name,
                        confederation = team.confederation,
                        collected = team.collectedCount,
                        total = team.totalCount,
                        isFavorite = team.isFavorite,
                        onClick = { onTeamClick(team.code) },
                        onToggleFavorite = { viewModel.setFavorite(team.code, !team.isFavorite) }
                    )
                }
            }
            confOrder.forEach { conf ->
                val confTeams = (grouped[conf] ?: return@forEach)
                    .filterNot { it.isFavorite }
                if (confTeams.isEmpty()) return@forEach
                item {
                    Text(
                        text = confederationLabel(conf),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                    )
                    HorizontalDivider()
                }
                items(confTeams, key = { it.code }) { team ->
                    TeamProgressRow(
                        flag = team.flagEmoji,
                        name = team.name,
                        confederation = team.confederation,
                        collected = team.collectedCount,
                        total = team.totalCount,
                        isFavorite = team.isFavorite,
                        onClick = { onTeamClick(team.code) },
                        onToggleFavorite = { viewModel.setFavorite(team.code, !team.isFavorite) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun TeamProgressFocusCard(
    focusTeam: TeamWithProgress?,
    onOpenTeam: () -> Unit,
    onQuickAdd: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Best next team move",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = focusTeam?.teamProgressSummary()
                    ?: "Open any team to start logging stickers. Near-complete sections will bubble up here once progress starts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onOpenTeam,
                    enabled = focusTeam != null
                ) {
                    Text(
                        focusTeam?.let { "Open ${it.flagEmoji} ${it.name}" } ?: "Open a team"
                    )
                }
                OutlinedButton(onClick = onQuickAdd) {
                    Text("Quick Add")
                }
            }
        }
    }
}

private fun List<TeamWithProgress>.nextFocusTeam(): TeamWithProgress? = this
    .filter { it.totalCount > 0 }
    .sortedWith(
        compareBy<TeamWithProgress>(
            {
                val remaining = it.remainingCount()
                when {
                    it.collectedCount in 1 until it.totalCount && remaining in 1..3 -> 0
                    it.collectedCount in 1 until it.totalCount -> 1
                    it.collectedCount == 0 -> 2
                    else -> 3
                }
            },
            { it.remainingCount() },
            { it.sortOrder }
        )
    )
    .firstOrNull()

private fun TeamWithProgress.remainingCount(): Int = (totalCount - collectedCount).coerceAtLeast(0)

private fun TeamWithProgress.teamProgressSummary(): String {
    val remaining = remainingCount()
    return when {
        totalCount == 0 -> "This section is ready to review."
        collectedCount == totalCount -> "$flagEmoji $name is complete. Open the section to review collected stickers or extras."
        collectedCount == 0 -> "$flagEmoji $name has not been started yet. Open the section or use Quick Add to log the first sticker."
        remaining in 1..3 -> "$flagEmoji $name is only $remaining sticker${if (remaining == 1) "" else "s"} away. Open it now to finish the section."
        else -> "$flagEmoji $name has $remaining stickers left. Open it to review missing spots and keep momentum going."
    }
}

private fun confederationLabel(conf: String): String = when (conf) {
    "SPECIAL" -> "⭐ Special Stickers"
    "CONCACAF" -> "🌎 CONCACAF"
    "UEFA" -> "🌍 UEFA"
    "CONMEBOL" -> "🌎 CONMEBOL"
    "CAF" -> "🌍 CAF (Africa)"
    "AFC" -> "🌏 AFC (Asia)"
    "OFC" -> "🌏 OFC (Oceania)"
    else -> conf
}

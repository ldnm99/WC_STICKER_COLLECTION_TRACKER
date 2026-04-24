package com.example.wc2026stickers.ui.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wc2026stickers.data.db.dao.TeamWithProgress
import com.example.wc2026stickers.ui.components.TeamProgressRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsListScreen(
    onBack: () -> Unit,
    onTeamClick: (String) -> Unit,
    onNavigateToQuickAdd: () -> Unit,
    viewModel: TeamsViewModel = hiltViewModel()
) {
    val teams by viewModel.teams.collectAsState()

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
                Icon(Icons.Default.Add, contentDescription = "Quick Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            confOrder.forEach { conf ->
                val confTeams = grouped[conf] ?: return@forEach
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
                        onClick = { onTeamClick(team.code) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
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

package com.example.wc2026stickers.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wc2026stickers.ui.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTeams: () -> Unit,
    onNavigateToMissing: () -> Unit,
    onNavigateToDuplicates: () -> Unit,
    onNavigateToQuickAdd: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val progress = if (state.totalCount > 0) state.collectedCount.toFloat() / state.totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚽ WC 2026 Stickers", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToQuickAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = "Quick Add") },
                text = { Text("Quick Add") },
                containerColor = MaterialTheme.colorScheme.secondary
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
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
                        modifier = Modifier.fillMaxWidth().height(12.dp),
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
                    color = Color(0xFFE53935),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Duplicates",
                    value = "${state.duplicatesCount}",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Navigation buttons
            Text("Browse", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Button(
                onClick = onNavigateToTeams,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("🏆  Browse by Team")
            }
            OutlinedButton(
                onClick = onNavigateToMissing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("❌  Missing Stickers (${state.missingCount})")
            }
            OutlinedButton(
                onClick = onNavigateToDuplicates,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋  Duplicates (${state.duplicatesCount})")
            }
            OutlinedButton(
                onClick = onNavigateToQuickAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("➕  Quick Add by ID")
            }
        }
    }
}

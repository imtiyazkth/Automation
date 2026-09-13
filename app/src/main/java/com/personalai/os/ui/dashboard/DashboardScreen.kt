package com.personalai.os.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Tile(val label: String, val value: String)

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()

    val tiles = listOf(
        Tile("Messages today", state.messagesToday.toString()),
        Tile("Active leads", state.activeLeads.toString()),
        Tile("Orders today", state.ordersToday.toString()),
        Tile("Needs approval", state.needsApproval.toString()),
        Tile("Alerts (24h)", state.alertsToday.toString())
    )

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("AI Status: ONLINE", style = MaterialTheme.typography.titleMedium)
        Text("Automation mode: ${state.automationMode}", style = MaterialTheme.typography.bodyMedium)

        Button(onClick = { viewModel.refresh() }, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("Refresh")
        }

        if (state.loading) {
            Text("Loading...")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tiles) { tile ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(tile.value, style = MaterialTheme.typography.headlineMedium)
                            Text(tile.label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

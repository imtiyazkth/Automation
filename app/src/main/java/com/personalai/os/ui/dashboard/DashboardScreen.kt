package com.personalai.os.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class DashboardTile(val label: String, val value: String)

/** Static layout mirroring the wireframe in blueprint Part 26 - wire tiles to real DAOs. */
@Composable
fun DashboardScreen(tiles: List<DashboardTile>) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Good evening.")
        Text("AI Status: ONLINE   Automation: ACTIVE")
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            tiles.forEach { tile ->
                Card(Modifier.padding(4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(tile.value)
                        Text(tile.label)
                    }
                }
            }
        }
    }
}

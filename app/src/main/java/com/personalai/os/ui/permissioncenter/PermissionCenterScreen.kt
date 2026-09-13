package com.personalai.os.ui.permissioncenter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionCenterScreen(viewModel: PermissionCenterViewModel) {
    val permissions by viewModel.permissions.collectAsState()

    LazyColumn(Modifier.fillMaxWidth().padding(12.dp)) {
        items(permissions) { perm ->
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(perm.permission, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Needed by: ${perm.requiredByAgents.joinToString()}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Switch(
                        checked = perm.granted,
                        onCheckedChange = { viewModel.toggle(perm.permission, perm.granted) }
                    )
                }
            }
        }
    }
}

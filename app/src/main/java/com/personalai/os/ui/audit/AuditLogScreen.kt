package com.personalai.os.ui.audit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogScreen(viewModel: AuditLogViewModel) {
    val entries by viewModel.entries.collectAsState()
    val formatter = dateFormatter()

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Button(onClick = { viewModel.refresh() }, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Refresh")
        }

        LazyColumn {
            items(entries) { entry ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Text(
                            "${formatter.format(Date(entry.timestamp))}  ${entry.result}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text("${entry.actor} -> ${entry.action}", style = MaterialTheme.typography.bodyMedium)
                        entry.detail?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

private fun dateFormatter(): SimpleDateFormat = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())

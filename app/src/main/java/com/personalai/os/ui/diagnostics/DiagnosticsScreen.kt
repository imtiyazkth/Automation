package com.personalai.os.ui.diagnostics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun DiagnosticsScreen(viewModel: DiagnosticsViewModel) {
    val state by viewModel.state.collectAsState()

    if (state.selectedContent != null) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Button(onClick = { viewModel.closeDetail() }) { Text("Back") }
            Text(
                state.selectedContent.orEmpty(),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).verticalScroll(rememberScrollState())
            )
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Crash & error logs, read directly from this device - no ADB needed.",
            style = MaterialTheme.typography.bodySmall)

        Button(onClick = { viewModel.refresh() }, modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Refresh")
        }

        if (state.logFileNames.isEmpty()) {
            Text("No crash logs yet - nothing has thrown an uncaught exception.")
        } else {
            LazyColumn {
                items(state.logFileNames) { name ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(10.dp)) {
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton(onClick = { viewModel.open(name) }, modifier = Modifier.padding(top = 4.dp)) {
                                Text("View")
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = { viewModel.clearAll() }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Clear all logs")
            }
        }
    }
}

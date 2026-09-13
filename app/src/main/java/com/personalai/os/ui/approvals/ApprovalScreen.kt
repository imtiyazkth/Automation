package com.personalai.os.ui.approvals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApprovalScreen(viewModel: ApprovalViewModel) {
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        state.lastResult?.let {
            Text("Last result: $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (state.pending.isEmpty()) {
            Text("Nothing waiting for approval right now.")
        }

        LazyColumn {
            items(state.pending, key = { it.id }) { approval ->
                var editing by remember { mutableStateOf(false) }
                var editedText by remember {
                    mutableStateOf(approval.step.params["message"] as? String ?: "")
                }

                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(approval.draftSummary, style = MaterialTheme.typography.titleSmall)
                        Text(approval.reason, style = MaterialTheme.typography.bodySmall)

                        if (editing) {
                            OutlinedTextField(
                                value = editedText,
                                onValueChange = { editedText = it },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                label = { Text("Message") }
                            )
                        }

                        Row(
                            Modifier.padding(top = 8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (editing) {
                                Button(onClick = {
                                    viewModel.sendEdited(approval.id, editedText)
                                    editing = false
                                }) { Text("Send edited") }
                            } else {
                                Button(onClick = { viewModel.send(approval.id) }) { Text("Send") }
                                OutlinedButton(onClick = { editing = true }) { Text("Edit") }
                            }
                            OutlinedButton(onClick = { viewModel.ignore(approval.id) }) { Text("Ignore") }
                        }
                    }
                }
            }
        }
    }
}

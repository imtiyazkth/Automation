package com.personalai.os.ui.permissioncenter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalai.os.ui.components.EmptyState
import com.personalai.os.ui.components.GroupedCard
import com.personalai.os.ui.components.ListRow
import com.personalai.os.ui.components.RowDivider
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.appSwitchColors
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.components.permissionLabel

@Composable
fun PermissionCenterScreen(
    viewModel: PermissionCenterViewModel,
    onBack: (() -> Unit)? = null,
    onOpenAgents: () -> Unit = {}
) {
    val permissions by viewModel.permissions.collectAsState()
    val granted = permissions.count { it.granted }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Permissions",
            subtitle = if (permissions.isEmpty()) null else "$granted of ${permissions.size} allowed",
            onBack = onBack
        )

        if (permissions.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.Lock,
                    title = "No permissions needed",
                    body = "None of your agents ask for extra access right now.",
                    actionLabel = "View agents",
                    onAction = onOpenAgents
                )
            }
        } else {
            Column(Modifier.weight(1f).fadeEdges().verticalScroll(rememberScrollState())) {
                GroupedCard(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    permissions.forEachIndexed { i, perm ->
                        if (i > 0) RowDivider()
                        ListRow(
                            title = permissionLabel(perm.permission),
                            subtitle = "Used by ${perm.requiredByAgents.joinToString()}",
                            trailing = {
                                Switch(
                                    checked = perm.granted,
                                    onCheckedChange = { viewModel.toggle(perm.permission, perm.granted) },
                                    colors = appSwitchColors()
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

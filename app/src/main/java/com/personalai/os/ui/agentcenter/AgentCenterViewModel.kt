package com.personalai.os.ui.agentcenter

import androidx.lifecycle.ViewModel
import com.personalai.os.core.agents.AgentRegistry
import com.personalai.os.core.automation.AutomationMode
import com.personalai.os.core.automation.AutomationModeStore
import com.personalai.os.core.security.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AgentUiModel(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val risk: String,
    val mode: AutomationMode,
    val allPermissionsGranted: Boolean,
    val missingPermissions: List<String>
)

class AgentCenterViewModel(
    private val registry: AgentRegistry,
    private val modeStore: AutomationModeStore,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _agents = MutableStateFlow<List<AgentUiModel>>(emptyList())
    val agents: StateFlow<List<AgentUiModel>> = _agents

    init { refresh() }

    fun refresh() {
        _agents.value = registry.all().map { def ->
            val missing = def.permissions.filterNot { permissionManager.isGranted(it) }
            AgentUiModel(
                id = def.id,
                name = def.name,
                category = def.category,
                description = def.description,
                risk = def.risk,
                mode = modeStore.modeFor(def.id),
                allPermissionsGranted = missing.isEmpty(),
                missingPermissions = missing
            )
        }
    }

    fun setMode(agentId: String, mode: AutomationMode) {
        modeStore.setModeFor(agentId, mode)
        refresh()
    }
}

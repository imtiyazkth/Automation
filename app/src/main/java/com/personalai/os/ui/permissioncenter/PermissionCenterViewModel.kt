package com.personalai.os.ui.permissioncenter

import androidx.lifecycle.ViewModel
import com.personalai.os.core.agents.AgentRegistry
import com.personalai.os.core.security.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PermissionUiModel(
    val permission: String,
    val granted: Boolean,
    val requiredByAgents: List<String>
)

class PermissionCenterViewModel(
    private val registry: AgentRegistry,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _permissions = MutableStateFlow<List<PermissionUiModel>>(emptyList())
    val permissions: StateFlow<List<PermissionUiModel>> = _permissions

    init { refresh() }

    fun refresh() {
        val allDefs = registry.all()
        val byPermission = allDefs
            .flatMap { def -> def.permissions.map { it to def.name } }
            .groupBy({ it.first }, { it.second })

        _permissions.value = byPermission.map { (permission, agentNames) ->
            PermissionUiModel(
                permission = permission,
                granted = permissionManager.isGranted(permission),
                requiredByAgents = agentNames.distinct()
            )
        }.sortedBy { it.permission }
    }

    fun toggle(permission: String, currentlyGranted: Boolean) {
        if (currentlyGranted) permissionManager.revoke(permission) else permissionManager.grant(permission)
        refresh()
    }
}

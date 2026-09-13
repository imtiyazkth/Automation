package com.personalai.os.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalai.os.core.automation.ApprovalManager
import com.personalai.os.core.automation.AutomationModeStore
import com.personalai.os.core.security.AuditLogger
import com.personalai.os.data.dao.CrmDao
import com.personalai.os.data.dao.MessageDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val automationMode: String = "-",
    val messagesToday: Int = 0,
    val activeLeads: Int = 0,
    val ordersToday: Int = 0,
    val needsApproval: Int = 0,
    val alertsToday: Int = 0,
    val loading: Boolean = true
)

class DashboardViewModel(
    private val messageDao: MessageDao,
    private val crmDao: CrmDao,
    private val approvalManager: ApprovalManager,
    private val auditLogger: AuditLogger,
    private val modeStore: AutomationModeStore
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val startOfDay = startOfTodayEpochMillis()
            val messagesToday = runCatching { messageDao.countSince(startOfDay) }.getOrDefault(0)
            val activeLeads = runCatching { crmDao.activeLeads().size }.getOrDefault(0)
            val ordersToday = runCatching { crmDao.ordersSince(startOfDay).size }.getOrDefault(0)
            val alertsToday = runCatching {
                auditLogger.recent(200).count { it.timestamp >= startOfDay && (it.result == "BLOCKED" || it.result == "FAILED") }
            }.getOrDefault(0)

            _state.value = DashboardUiState(
                automationMode = modeStore.globalMode().name,
                messagesToday = messagesToday,
                activeLeads = activeLeads,
                ordersToday = ordersToday,
                needsApproval = approvalManager.all().size,
                alertsToday = alertsToday,
                loading = false
            )
        }
    }

    private fun startOfTodayEpochMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

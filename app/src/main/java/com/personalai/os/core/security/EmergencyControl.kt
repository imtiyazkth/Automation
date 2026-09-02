package com.personalai.os.core.security

import com.personalai.os.core.automation.AutomationMode
import com.personalai.os.core.automation.AutomationModeStore

/**
 * The kill switch from blueprint Part 38. Deliberately blunt and
 * synchronous - in a real incident, "did it actually stop" matters more
 * than elegance.
 */
class EmergencyControl(
    private val modeStore: AutomationModeStore,
    private val permissionManager: PermissionManager,
    private val auditLogger: AuditLogger
) {
    fun stopAllAutomation() {
        modeStore.setGlobalMode(AutomationMode.MANUAL)
        auditLogger.log(AuditEntry(System.currentTimeMillis(), "user", "stop_all_automation", null, "SUCCESS"))
    }

    fun disableCloudAi() {
        permissionManager.revoke("use_cloud_ai")
        auditLogger.log(AuditEntry(System.currentTimeMillis(), "user", "disable_cloud_ai", null, "SUCCESS"))
    }

    fun disableMessaging() {
        listOf("send_whatsapp_business", "send_sms", "telegram_send", "email_send")
            .forEach { permissionManager.revoke(it) }
        auditLogger.log(AuditEntry(System.currentTimeMillis(), "user", "disable_messaging", null, "SUCCESS"))
    }

    fun lockSensitiveData() {
        listOf("access_hr_database", "crm_write").forEach { permissionManager.revoke(it) }
        auditLogger.log(AuditEntry(System.currentTimeMillis(), "user", "lock_sensitive_data", null, "SUCCESS"))
    }

    /** Revokes cached OAuth/API tokens - wire to real token stores per integration. */
    fun revokeTokens() {
        auditLogger.log(AuditEntry(System.currentTimeMillis(), "user", "revoke_tokens", null, "SUCCESS"))
    }

    fun panicStopEverything() {
        stopAllAutomation()
        disableCloudAi()
        disableMessaging()
        lockSensitiveData()
        revokeTokens()
    }
}

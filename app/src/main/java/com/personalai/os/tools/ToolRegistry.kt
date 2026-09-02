package com.personalai.os.tools

/**
 * Declares which tool IDs exist and, at a glance, their risk tier - used by
 * PolicyEngine / AgentDefinition cross-checks in tests to make sure no
 * agent.json ever lists a tool that doesn't actually exist.
 */
object ToolRegistry {
    val knownTools = setOf(
        "hr_database_read", "excel_export", "file_read", "file_write", "ocr_engine",
        "url_parser", "domain_reputation_lookup", "resume_parser", "web_search_gemini",
        "whatsapp_business_send", "telegram_send", "sms_send", "email_send", "crm_write"
    )
}

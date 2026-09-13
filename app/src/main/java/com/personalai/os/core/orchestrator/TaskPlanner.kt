package com.personalai.os.core.orchestrator

class TaskPlanner {

    fun plan(intent: DetectedIntent): TaskPlan {
        val steps = when (intent.intentType) {
            "hr_query" -> listOf(
                TaskStep(id = "s1", agentId = "hr-agent", action = "query", params = intent.slots)
            )
            "job_search" -> listOf(
                TaskStep(id = "s1", agentId = "job-search-agent", action = "parse_resume"),
                TaskStep(id = "s2", agentId = "job-search-agent", action = "search", dependsOn = listOf("s1")),
                TaskStep(id = "s3", agentId = "job-search-agent", action = "rank_and_report", dependsOn = listOf("s2"))
            )
            "job_evaluate" -> listOf(
                TaskStep(
                    id = "s1", agentId = "career-ops-agent", action = "evaluate",
                    params = mapOf("job_description" to intent.rawText)
                )
            )
            "document_task" -> listOf(
                TaskStep(id = "s1", agentId = "document-agent", action = "extract"),
                TaskStep(id = "s2", agentId = "document-agent", action = "clean_and_export", dependsOn = listOf("s1"))
            )
            "link_check" -> listOf(
                TaskStep(id = "s1", agentId = "link-safety-agent", action = "check", params = intent.slots)
            )
            "send_message" -> listOf(
                TaskStep(id = "s1", agentId = "communication-agent", action = "send", params = intent.slots)
            )
            "automation_toggle" -> listOf(
                TaskStep(id = "s1", agentId = "marketing-agent", action = "toggle_mode", params = intent.slots)
            )
            "crm_query" -> listOf(
                TaskStep(id = "s1", agentId = "marketing-agent", action = "crm_query")
            )
            "emergency_control" -> listOf(
                TaskStep(id = "s1", agentId = "security-agent", action = "stop_all_automation")
            )
            else -> emptyList()
        }
        return TaskPlan(originalRequest = intent.rawText, intent = intent, steps = steps)
    }
}

package com.personalai.os.core.agents

import com.personalai.os.core.ai.AiRouter
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep
import com.personalai.os.tools.ExcelExportTool

class JobSearchAgent(
    private val aiRouter: AiRouter,
    private val excelExportTool: ExcelExportTool
) : Agent {
    override val definitionId = "job-search-agent"

    override suspend fun execute(step: TaskStep): ExecutionReport {
        return when (step.action) {
            "parse_resume" -> ExecutionReport.Success("Resume parsed (stub - wire to a real resume parser)")
            "search" -> {
                val query = step.params["query"] as? String ?: "matching jobs"
                val result = aiRouter.generate(
                    intentType = "job_search",
                    prompt = "Search for recent job postings: $query. Only report postings with a verifiable date.",
                    payload = mapOf("skills" to (step.params["skills"] as? String ?: ""))
                )
                ExecutionReport.Success("Search complete", mapOf("raw" to result))
            }
            "rank_and_report" -> {
                val path = runCatching {
                    excelExportTool.export(listOf(listOf("Job", "Company", "Match Score")), "job_search_report")
                }.getOrElse { return ExecutionReport.Failed("Could not write job report", it) }
                ExecutionReport.Success("Report saved to $path", mapOf("path" to path))
            }
            else -> ExecutionReport.Failed("JobSearchAgent has no handler for action '${step.action}'")
        }
    }
}

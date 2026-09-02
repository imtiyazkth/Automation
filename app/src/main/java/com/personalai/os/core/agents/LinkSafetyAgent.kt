package com.personalai.os.core.agents

import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep
import com.personalai.os.tools.LinkReputationTool
import java.net.URI

/** Never claims guaranteed safety - see blueprint Part 14/18. */
class LinkSafetyAgent(private val reputationTool: LinkReputationTool) : Agent {
    override val definitionId = "link-safety-agent"

    override suspend fun execute(step: TaskStep): ExecutionReport {
        val url = step.params["url"] as? String
            ?: return ExecutionReport.RequiresUserAction("Which link should I check?", "missing url")

        val indicators = mutableListOf<String>()
        val uri = runCatching { URI(url) }.getOrNull()
            ?: return ExecutionReport.Failed("'$url' isn't a parseable URL")

        if (uri.scheme != "https") indicators += "not served over HTTPS"
        if (Regex("\\d{1,3}(\\.\\d{1,3}){3}").containsMatchIn(uri.host.orEmpty())) indicators += "raw IP address host"
        if ((uri.host?.count { it == '-' } ?: 0) >= 3) indicators += "unusually hyphenated domain"

        val reputation = runCatching { reputationTool.lookup(uri.host.orEmpty()) }.getOrNull()
        if (reputation?.knownMalicious == true) indicators += "flagged by reputation lookup"

        val verdict = when {
            reputation?.knownMalicious == true -> "MALICIOUS INDICATORS DETECTED"
            indicators.size >= 2 -> "HIGH RISK"
            indicators.size == 1 -> "SUSPICIOUS"
            reputation == null -> "UNKNOWN"
            else -> "SAFE-LOOKING (not detected as harmful - never guaranteed safe)"
        }

        return ExecutionReport.Success(
            "$verdict${if (indicators.isNotEmpty()) " - " + indicators.joinToString() else ""}",
            mapOf("verdict" to verdict, "indicators" to indicators)
        )
    }
}

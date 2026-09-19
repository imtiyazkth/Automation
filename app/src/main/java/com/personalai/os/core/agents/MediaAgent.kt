package com.personalai.os.core.agents

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep

class MediaAgent(private val appContext: Context) : Agent {
    override val definitionId = "media-agent"

    override suspend fun execute(step: TaskStep): ExecutionReport {
        return when (step.action) {
            "search_and_open" -> {
                val query = (step.params["query"] as? String)?.trim()
                if (query.isNullOrBlank()) {
                    return ExecutionReport.RequiresUserAction("What should I search for on YouTube?", "missing query")
                }
                val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                runCatching { appContext.startActivity(intent) }
                    .fold(
                        onSuccess = { ExecutionReport.Success("Opened YouTube search for \"$query\"") },
                        onFailure = { ExecutionReport.Failed("Couldn't open YouTube", it) }
                    )
            }
            else -> ExecutionReport.Failed("MediaAgent has no handler for action '${step.action}'")
        }
    }
}

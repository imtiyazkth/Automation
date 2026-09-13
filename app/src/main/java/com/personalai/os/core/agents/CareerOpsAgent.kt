package com.personalai.os.core.agents

import com.personalai.os.core.ai.AiRouter
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep
import com.personalai.os.data.dao.JobEvaluationDao
import com.personalai.os.data.entities.JobEvaluationEntity
import java.util.UUID

class CareerOpsAgent(
    private val aiRouter: AiRouter,
    private val jobEvaluationDao: JobEvaluationDao
) : Agent {
    override val definitionId = "career-ops-agent"

    private val rubric = """
        Evaluate the following job description across these dimensions, each
        with a short verdict:
        A. Role & Responsibilities Fit
        B. Skills & Experience Match
        C. Compensation & Benefits Signal
        D. Company Stability & Reputation
        E. Growth & Learning Potential
        F. Team & Culture Signals
        G. Logistics (location, remote policy, hours)
        H. Red Flags

        Then give ONE holistic overall score from 1-5, written as "Score: X/5"
        (use judgement across all dimensions together, not an arithmetic
        average), and a one-line recommendation. Be direct about weaknesses -
        do not inflate the score to be encouraging.
    """.trimIndent()

    override suspend fun execute(step: TaskStep): ExecutionReport {
        return when (step.action) {
            "evaluate" -> evaluate(step)
            else -> ExecutionReport.Failed("CareerOpsAgent has no handler for action '${step.action}'")
        }
    }

    private suspend fun evaluate(step: TaskStep): ExecutionReport {
        val jobDescription = step.params["job_description"] as? String
        if (jobDescription.isNullOrBlank()) {
            return ExecutionReport.RequiresUserAction(
                "Paste the job description (or its key details) you want evaluated.",
                "missing job_description"
            )
        }
        val candidateSkills = step.params["candidate_skills"] as? String ?: ""

        val prompt = buildString {
            append(rubric)
            append("\n\nJob description:\n")
            append(jobDescription)
            if (candidateSkills.isNotBlank()) {
                append("\n\nCandidate's key skills for fit comparison: ")
                append(candidateSkills)
            }
        }

        val reportText = aiRouter.generate(
            intentType = "job_search",
            prompt = prompt,
            payload = mapOf("job_description" to jobDescription, "candidate_skills" to candidateSkills)
        )

        val score = extractScore(reportText)

        runCatching {
            jobEvaluationDao.insert(
                JobEvaluationEntity(
                    id = UUID.randomUUID().toString(),
                    jobTitle = step.params["job_title"] as? String,
                    company = step.params["company"] as? String,
                    score = score,
                    reportText = reportText,
                    sourceUrl = step.params["url"] as? String,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        val warning = if (score != null && score < 4.0) {
            "\n\n(Scored below 4.0 - career-ops' own guidance is that roles below this bar usually aren't worth your time.)"
        } else ""

        return ExecutionReport.Success(reportText + warning, mapOf("score" to (score ?: -1.0)))
    }

    private fun extractScore(text: String): Double? {
        val match = Regex("""(\d(?:\.\d)?)\s*/\s*5""").find(text)
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }
}

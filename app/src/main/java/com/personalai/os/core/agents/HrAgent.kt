package com.personalai.os.core.agents

import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep
import com.personalai.os.data.dao.AttendanceDao

/**
 * Answers attendance/leave/overtime questions from the LOCAL database only
 * (agent.json declares data_residency = local_only). Never routes HR data
 * to Gemini.
 */
class HrAgent(private val attendanceDao: AttendanceDao) : Agent {
    override val definitionId = "hr-agent"

    override suspend fun execute(step: TaskStep): ExecutionReport {
        return when (step.action) {
            "query" -> {
                val summary = attendanceDao.todaySummary()
                ExecutionReport.Success(
                    message = "${summary.present} Present, ${summary.absent} Absent, " +
                        "${summary.onLeave} On Leave, ${summary.late} Late",
                    data = mapOf(
                        "present" to summary.present, "absent" to summary.absent,
                        "onLeave" to summary.onLeave, "late" to summary.late
                    )
                )
            }
            else -> ExecutionReport.Failed("HrAgent has no handler for action '${step.action}'")
        }
    }
}

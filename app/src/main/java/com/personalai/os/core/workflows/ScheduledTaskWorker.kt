package com.personalai.os.core.workflows

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.personalai.os.core.orchestrator.HeadAgent
import com.personalai.os.data.dao.ScheduledTaskDao

class ScheduledTaskWorker(
    context: Context,
    params: WorkerParameters,
    private val headAgent: HeadAgent,
    private val taskDao: ScheduledTaskDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val task = taskDao.get(taskId) ?: return Result.failure()
        if (!task.enabled) return Result.success()

        val handled = runCatching { headAgent.handle(task.commandText) }
            .getOrElse { e ->
                taskDao.recordRun(taskId, System.currentTimeMillis(), "Error: ${e.message}")
                return Result.retry()
            }

        val summary = handled.reports.joinToString("; ") { it.message }.take(500)
        taskDao.recordRun(taskId, System.currentTimeMillis(), summary)
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
    }
}

class ScheduledTaskWorkerFactory(
    private val headAgent: HeadAgent,
    private val taskDao: ScheduledTaskDao
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == ScheduledTaskWorker::class.java.name) {
            ScheduledTaskWorker(appContext, workerParameters, headAgent, taskDao)
        } else {
            null
        }
    }
}

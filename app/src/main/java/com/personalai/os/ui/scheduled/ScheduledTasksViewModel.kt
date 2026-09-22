package com.personalai.os.ui.scheduled

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.personalai.os.core.workflows.ScheduledTaskWorker
import com.personalai.os.data.dao.ScheduledTaskDao
import com.personalai.os.data.entities.ScheduledTaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class ScheduledTasksViewModel(
    private val taskDao: ScheduledTaskDao,
    private val appContext: Context
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<ScheduledTaskEntity>>(emptyList())
    val tasks: StateFlow<List<ScheduledTaskEntity>> = _tasks

    init { refresh() }

    fun refresh() {
        viewModelScope.launch { _tasks.value = taskDao.all() }
    }

    fun create(name: String, commandText: String, intervalMinutes: Long) {
        viewModelScope.launch {
            val task = ScheduledTaskEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                commandText = commandText,
                intervalMinutes = intervalMinutes.coerceAtLeast(15),
                enabled = true,
                createdAt = System.currentTimeMillis()
            )
            taskDao.upsert(task)
            scheduleWork(task)
            refresh()
        }
    }

    fun setEnabled(task: ScheduledTaskEntity, enabled: Boolean) {
        viewModelScope.launch {
            val updated = task.copy(enabled = enabled)
            taskDao.upsert(updated)
            if (enabled) scheduleWork(updated) else cancelWork(task.id)
            refresh()
        }
    }

    fun delete(task: ScheduledTaskEntity) {
        viewModelScope.launch {
            cancelWork(task.id)
            taskDao.delete(task.id)
            refresh()
        }
    }

    private fun scheduleWork(task: ScheduledTaskEntity) {
        runCatching {
            val request = PeriodicWorkRequestBuilder<ScheduledTaskWorker>(task.intervalMinutes, TimeUnit.MINUTES)
                .setInputData(Data.Builder().putString(ScheduledTaskWorker.KEY_TASK_ID, task.id).build())
                .build()
            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                uniqueWorkName(task.id), ExistingPeriodicWorkPolicy.UPDATE, request
            )
        }
    }

    private fun cancelWork(taskId: String) {
        runCatching { WorkManager.getInstance(appContext).cancelUniqueWork(uniqueWorkName(taskId)) }
    }

    private fun uniqueWorkName(taskId: String) = "scheduled-task-$taskId"
}

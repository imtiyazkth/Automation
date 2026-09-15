package com.personalai.os.util

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {
    private const val TAG = "CrashLogger"
    private const val MAX_LOG_FILES = 20
    private lateinit var logDir: File

    fun init(context: Context) {
        logDir = File(context.filesDir, "crash_logs")
        logDir.mkdirs()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                saveCrashLog(throwable, thread.name, "UNCAUGHT")
                Log.e(TAG, "UNCAUGHT on ${thread.name}", throwable)
            } catch (e: Exception) {
                Log.e(TAG, "CrashLogger itself failed", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
        Log.d(TAG, "CrashLogger initialized, dir=${logDir.absolutePath}")
    }

    fun logException(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
        runCatching { saveCrashLog(throwable, tag, "HANDLED", message) }
    }

    private fun saveCrashLog(
        throwable: Throwable?,
        thread: String,
        type: String,
        message: String = ""
    ) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val file = File(logDir, "crash_$timestamp.txt")

        val sb = StringBuilder()
        sb.appendLine("=== Personal AI Automation OS Crash Log ===")
        sb.appendLine("Time    : $timestamp")
        sb.appendLine("Type    : $type")
        sb.appendLine("Thread  : $thread")
        sb.appendLine("Message : $message")
        sb.appendLine()
        sb.appendLine("--- Device Info ---")
        sb.appendLine("Model   : ${Build.MANUFACTURER} ${Build.MODEL}")
        sb.appendLine("Android : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("Brand   : ${Build.BRAND}")
        sb.appendLine()
        if (throwable != null) {
            sb.appendLine("--- Exception ---")
            sb.appendLine("${throwable.javaClass.name}: ${throwable.message}")
            sb.appendLine()
            sb.appendLine("--- Stack Trace ---")
            sb.appendLine(throwable.stackTraceToString())
            var cause = throwable.cause
            while (cause != null) {
                sb.appendLine("--- Caused By ---")
                sb.appendLine("${cause.javaClass.name}: ${cause.message}")
                sb.appendLine(cause.stackTraceToString())
                cause = cause.cause
            }
        }
        file.writeText(sb.toString())
        pruneOldLogs()
    }

    fun getRecentLogs(): List<File> =
        if (::logDir.isInitialized) logDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
        else emptyList()

    fun clearLogs() {
        if (::logDir.isInitialized) logDir.listFiles()?.forEach { it.delete() }
    }

    private fun pruneOldLogs() {
        val files = logDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        files.drop(MAX_LOG_FILES).forEach { it.delete() }
    }
}

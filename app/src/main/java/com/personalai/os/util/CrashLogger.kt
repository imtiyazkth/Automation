package com.personalai.os.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {
    private const val TAG = "CrashLogger"
    private const val MAX_LOG_FILES = 20
    private lateinit var appContext: Context
    private lateinit var logDir: File

    fun init(context: Context) {
        appContext = context.applicationContext
        logDir = File(appContext.filesDir, "crash_logs")
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
        val fileName = "crash_$timestamp.txt"

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
        val content = sb.toString()

        // Primary copy: internal storage, always succeeds, readable from the
        // in-app Diagnostics screen once the app manages to open.
        runCatching { File(logDir, fileName).writeText(content) }
        pruneOldLogs()

        // Best-effort SECOND copy: the public Downloads folder, via
        // MediaStore (no special permission needed on API 29+, and this is
        // the exact folder Termux's `termux-setup-storage` already exposes
        // at ~/storage/downloads/). This is what lets you read a crash log
        // even if the app never successfully opens at all.
        runCatching { writePublicCopy(fileName, content) }
    }

    private fun writePublicCopy(fileName: String, content: String) {
        if (!::appContext.isInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = appContext.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/AutomationOSCrashLogs")
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
            resolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
        } else {
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AutomationOSCrashLogs")
            dir.mkdirs()
            File(dir, fileName).writeText(content)
        }
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

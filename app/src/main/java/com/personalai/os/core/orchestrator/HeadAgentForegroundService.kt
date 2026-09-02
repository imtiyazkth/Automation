package com.personalai.os.core.orchestrator

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Keeps the Head Agent's workflow engine alive for scheduled/background
 * automations (e.g. the "daily job search" rule from blueprint Part 23).
 * Deliberately minimal here - wire actual WorkManager-driven triggers in
 * core/workflows/WorkflowEngine.kt and have this service just host the
 * always-on parts (e.g. rule listeners for Marketing Mode).
 */
class HeadAgentForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: start foreground notification + attach WorkflowEngine listeners.
        return START_STICKY
    }
}

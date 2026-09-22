package com.personalai.os

import android.app.Application
import android.util.Log
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import com.personalai.os.core.agents.AgentRegistry
import com.personalai.os.core.agents.CareerOpsAgent
import com.personalai.os.core.agents.CommunicationAgent
import com.personalai.os.core.agents.DocumentAgent
import com.personalai.os.core.agents.HrAgent
import com.personalai.os.core.agents.JobSearchAgent
import com.personalai.os.core.agents.LinkSafetyAgent
import com.personalai.os.core.agents.MarketingAgent
import com.personalai.os.core.agents.MediaAgent
import com.personalai.os.core.ai.AiRouter
import com.personalai.os.core.ai.GeminiProvider
import com.personalai.os.core.ai.LocalAiProvider
import com.personalai.os.core.ai.PrivacyGateway
import com.personalai.os.core.automation.ApprovalManager
import com.personalai.os.core.automation.AutomationMode
import com.personalai.os.core.automation.AutomationModeStore
import com.personalai.os.core.automation.InMemoryAutomationModeStore
import com.personalai.os.core.orchestrator.HeadAgent
import com.personalai.os.core.orchestrator.IntentDetector
import com.personalai.os.core.orchestrator.TaskPlanner
import com.personalai.os.core.security.AuditLogger
import com.personalai.os.core.security.InMemoryAuditLogger
import com.personalai.os.core.security.InMemoryPermissionStore
import com.personalai.os.core.security.PermissionManager
import com.personalai.os.core.security.PolicyEngine
import com.personalai.os.core.workflows.ConditionEvaluator
import com.personalai.os.core.workflows.ScheduledTaskWorkerFactory
import com.personalai.os.core.workflows.WorkflowEngine
import com.personalai.os.data.AppDatabase
import com.personalai.os.integrations.telegram.TelegramBotClient
import com.personalai.os.integrations.whatsapp.WhatsAppBusinessClient
import com.personalai.os.tools.ExcelExportTool
import com.personalai.os.tools.LinkReputationTool
import com.personalai.os.tools.PdfExtractTool
import com.personalai.os.util.CrashLogger
import java.io.File

class AutomationOsApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var headAgent: HeadAgent
        private set
    lateinit var workflowEngine: WorkflowEngine
        private set
    lateinit var agentRegistry: AgentRegistry
        private set
    lateinit var permissionManager: PermissionManager
        private set
    lateinit var modeStore: AutomationModeStore
        private set
    lateinit var approvalManager: ApprovalManager
        private set
    lateinit var auditLogger: AuditLogger
        private set

    override fun onCreate() {
        super.onCreate()

        try {
            CrashLogger.init(this)
        } catch (e: Exception) {
            Log.e("AutomationOsApp", "CrashLogger init failed", e)
        }

        runCatching {
            initializeCore()
        }.onFailure { e ->
            CrashLogger.logException("AutomationOsApp", "initializeCore() failed", e)
            throw e
        }
    }

    private fun initializeCore() {
        database = Room.databaseBuilder(this, AppDatabase::class.java, "automation_os.db")
            .fallbackToDestructiveMigration()
            .build()

        val permissionStore = InMemoryPermissionStore()
        permissionManager = PermissionManager(permissionStore)
        auditLogger = InMemoryAuditLogger()
        val policyEngine = PolicyEngine(permissionManager)
        modeStore = InMemoryAutomationModeStore()
        approvalManager = ApprovalManager()

        val localAi = LocalAiProvider(modelLoaded = false)
        val geminiProvider = GeminiProvider()
        val privacyGateway = PrivacyGateway()
        val aiRouter = AiRouter(localAi, geminiProvider, privacyGateway)

        agentRegistry = AgentRegistry(this)
        agentRegistry.loadDefinitions()

        val outputDir = File(getExternalFilesDir(null) ?: filesDir, "reports")
        val excelExportTool = ExcelExportTool(outputDir)
        val pdfExtractTool = PdfExtractTool()
        val linkReputationTool = LinkReputationTool()
        val whatsAppBusinessClient = WhatsAppBusinessClient()
        val telegramClient = TelegramBotClient()

        agentRegistry.register(HrAgent(database.attendanceDao()))
        agentRegistry.register(DocumentAgent(pdfExtractTool, excelExportTool))
        agentRegistry.register(MarketingAgent(modeStore))
        agentRegistry.register(LinkSafetyAgent(linkReputationTool))
        agentRegistry.register(JobSearchAgent(aiRouter, excelExportTool))
        agentRegistry.register(CommunicationAgent(whatsAppBusinessClient, telegramClient, applicationContext))
        agentRegistry.register(CareerOpsAgent(aiRouter, database.jobEvaluationDao()))
        agentRegistry.register(MediaAgent(applicationContext))

        if (BuildConfig.DEBUG) {
            agentRegistry.all().forEach { def ->
                def.permissions.forEach { permissionManager.grant(it, grantedBy = "dev_bypass") }
            }
            modeStore.setGlobalMode(AutomationMode.FULL)
        }

        val intentDetector = IntentDetector(aiRouter)
        val taskPlanner = TaskPlanner()
        headAgent = HeadAgent(
            intentDetector, taskPlanner, agentRegistry, policyEngine,
            modeStore, approvalManager, auditLogger
        )

        workflowEngine = WorkflowEngine(agentRegistry, policyEngine, modeStore, ConditionEvaluator())

        runCatching {
            val workConfig = Configuration.Builder()
                .setWorkerFactory(ScheduledTaskWorkerFactory(headAgent, database.scheduledTaskDao()))
                .build()
            WorkManager.initialize(this, workConfig)
        }.onFailure { e ->
            CrashLogger.logException("AutomationOsApp", "WorkManager.initialize failed", e)
        }
    }
}

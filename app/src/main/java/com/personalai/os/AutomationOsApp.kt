package com.personalai.os

import android.app.Application
import androidx.room.Room
import com.personalai.os.core.agents.AgentRegistry
import com.personalai.os.core.agents.CommunicationAgent
import com.personalai.os.core.agents.DocumentAgent
import com.personalai.os.core.agents.HrAgent
import com.personalai.os.core.agents.JobSearchAgent
import com.personalai.os.core.agents.LinkSafetyAgent
import com.personalai.os.core.agents.MarketingAgent
import com.personalai.os.core.ai.AiRouter
import com.personalai.os.core.ai.GeminiProvider
import com.personalai.os.core.ai.LocalAiProvider
import com.personalai.os.core.ai.PrivacyGateway
import com.personalai.os.core.automation.ApprovalManager
import com.personalai.os.core.automation.InMemoryAutomationModeStore
import com.personalai.os.core.orchestrator.HeadAgent
import com.personalai.os.core.orchestrator.IntentDetector
import com.personalai.os.core.orchestrator.TaskPlanner
import com.personalai.os.core.security.InMemoryAuditLogger
import com.personalai.os.core.security.InMemoryPermissionStore
import com.personalai.os.core.security.PermissionManager
import com.personalai.os.core.security.PolicyEngine
import com.personalai.os.core.workflows.ConditionEvaluator
import com.personalai.os.core.workflows.WorkflowEngine
import com.personalai.os.data.AppDatabase
import com.personalai.os.integrations.telegram.TelegramBotClient
import com.personalai.os.integrations.whatsapp.WhatsAppBusinessClient
import com.personalai.os.tools.ExcelExportTool
import com.personalai.os.tools.LinkReputationTool
import com.personalai.os.tools.PdfExtractTool
import java.io.File

/**
 * Hand-rolled composition root. A real project should replace this with
 * Hilt/Koin, but a plain object graph keeps this scaffold dependency-free
 * and easy to read end-to-end in one file.
 *
 * See AppDatabase.kt for the note on wrapping this with SQLCipher before
 * shipping - this scaffold builds an UNencrypted Room DB.
 */
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

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this, AppDatabase::class.java, "automation_os.db").build()

        // --- security / permissions / audit -------------------------------
        val permissionStore = InMemoryPermissionStore() // swap for PermissionDao-backed store
        permissionManager = PermissionManager(permissionStore)
        val auditLogger = InMemoryAuditLogger()          // swap for AuditDao-backed logger
        val policyEngine = PolicyEngine(permissionManager)
        val modeStore = InMemoryAutomationModeStore()
        val approvalManager = ApprovalManager()

        // --- AI -------------------------------------------------------------
        val localAi = LocalAiProvider(modelLoaded = false) // flip once a runtime is wired in
        val geminiProvider = GeminiProvider()
        val privacyGateway = PrivacyGateway()
        val aiRouter = AiRouter(localAi, geminiProvider, privacyGateway)

        // --- agent registry ---------------------------------------------
        agentRegistry = AgentRegistry(this)
        agentRegistry.loadDefinitions()

        val outputDir = File(getExternalFilesDir(null), "reports")
        val excelExportTool = ExcelExportTool(outputDir)
        val pdfExtractTool = PdfExtractTool()
        val linkReputationTool = LinkReputationTool()
        val whatsAppClient = WhatsAppBusinessClient()
        @Suppress("unused") val telegramClient = TelegramBotClient()

        agentRegistry.register(HrAgent(database.attendanceDao()))
        agentRegistry.register(DocumentAgent(pdfExtractTool, excelExportTool))
        agentRegistry.register(MarketingAgent(modeStore))
        agentRegistry.register(LinkSafetyAgent(linkReputationTool))
        agentRegistry.register(JobSearchAgent(aiRouter, excelExportTool))
        agentRegistry.register(CommunicationAgent(whatsAppClient))

        // --- orchestration ------------------------------------------------
        val intentDetector = IntentDetector(aiRouter)
        val taskPlanner = TaskPlanner()
        headAgent = HeadAgent(
            intentDetector, taskPlanner, agentRegistry, policyEngine,
            modeStore, approvalManager, auditLogger
        )

        workflowEngine = WorkflowEngine(agentRegistry, policyEngine, modeStore, ConditionEvaluator())
    }
}

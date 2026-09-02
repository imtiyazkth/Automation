package com.personalai.os.core.agents

import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep
import com.personalai.os.tools.ExcelExportTool
import com.personalai.os.tools.PdfExtractTool

class DocumentAgent(
    private val pdfExtractTool: PdfExtractTool,
    private val excelExportTool: ExcelExportTool
) : Agent {
    override val definitionId = "document-agent"

    override suspend fun execute(step: TaskStep): ExecutionReport {
        return when (step.action) {
            "extract" -> {
                val path = step.params["filePath"] as? String
                    ?: return ExecutionReport.RequiresUserAction("Which file should I read?", "missing filePath")
                val rows = runCatching { pdfExtractTool.extractTables(path) }
                    .getOrElse { return ExecutionReport.Failed("Could not read PDF at '$path'", it) }
                ExecutionReport.Success("Extracted ${rows.size} row(s) from PDF", mapOf("rows" to rows))
            }
            "clean_and_export" -> {
                @Suppress("UNCHECKED_CAST")
                val rows = step.params["rows"] as? List<List<String>> ?: emptyList()
                val outPath = runCatching { excelExportTool.export(rows, "extracted_report") }
                    .getOrElse { return ExecutionReport.Failed("Excel export failed", it) }
                ExecutionReport.Success("Saved Excel report to $outPath", mapOf("path" to outPath))
            }
            else -> ExecutionReport.Failed("DocumentAgent has no handler for action '${step.action}'")
        }
    }
}

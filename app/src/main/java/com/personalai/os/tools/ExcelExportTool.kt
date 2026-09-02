package com.personalai.os.tools

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

/** Real, working Excel export via Apache POI - the one tool in this scaffold that needs no external service or model. */
class ExcelExportTool(private val outputDir: File) {

    fun export(rows: List<List<String>>, baseFileName: String): String {
        if (!outputDir.exists()) outputDir.mkdirs()
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Report")

        rows.forEachIndexed { rowIndex, row ->
            val sheetRow = sheet.createRow(rowIndex)
            row.forEachIndexed { colIndex, value ->
                sheetRow.createCell(colIndex).setCellValue(value)
            }
        }

        val outFile = File(outputDir, "$baseFileName.xlsx")
        FileOutputStream(outFile).use { workbook.write(it) }
        workbook.close()
        return outFile.absolutePath
    }
}

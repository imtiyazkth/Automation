package com.personalai.os.tools

/**
 * PDF text/table extraction. Wire to PDFBox-Android (or a similar library)
 * for real extraction; OCR fallback for scanned PDFs should call into an
 * on-device OCR engine (ML Kit Text Recognition or Tesseract).
 */
class PdfExtractTool {
    fun extractTables(filePath: String): List<List<String>> {
        // TODO: replace with real PDFBox-Android parsing + OCR fallback.
        return listOf(listOf("column stub - wire PdfExtractTool to a real PDF parser"))
    }
}

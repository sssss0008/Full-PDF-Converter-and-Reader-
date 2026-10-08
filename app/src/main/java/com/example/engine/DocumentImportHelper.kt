package com.example.engine

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DocumentImportHelper {

    suspend fun importPdfFromUri(
        context: Context,
        database: AppDatabase,
        uri: Uri
    ): DocumentEntity? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val rawName = uri.lastPathSegment?.substringAfterLast('/') ?: "Document.pdf"
            val cleanName = if (rawName.endsWith(".pdf", ignoreCase = true)) rawName else "$rawName.pdf"

            val docsDir = File(context.filesDir, "documents").apply { mkdirs() }
            val targetFile = File(docsDir, "${cleanName.removeSuffix(".pdf")}_${System.currentTimeMillis()}.pdf")

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext null

            val pageCount = try {
                val tempEngine = PdfRendererEngine(context, targetFile)
                val count = tempEngine.pageCount
                tempEngine.close()
                count
            } catch (e: Exception) {
                1
            }

            val newDoc = DocumentEntity(
                id = "imported_${System.currentTimeMillis()}",
                title = cleanName.removeSuffix(".pdf"),
                filePath = targetFile.absolutePath,
                pageCount = pageCount,
                fileSize = targetFile.length(),
                lastOpenedTimestamp = System.currentTimeMillis()
            )
            database.documentDao().insertOrUpdate(newDoc)
            newDoc
        } catch (e: Exception) {
            null
        }
    }
}

package com.example.engine

import android.content.Context
import com.example.data.local.DocumentDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object SamplePdfGenerator {

    /**
     * Purges all default template samples so the user's workspace is completely blank,
     * allowing them to upload and manage their own PDF files.
     */
    suspend fun cleanupDefaultSamples(context: Context, documentDao: DocumentDao) = withContext(Dispatchers.IO) {
        val sampleIds = listOf(
            "sample_architecture_guide",
            "sample_nda_agreement",
            "sample_grid_notes"
        )
        sampleIds.forEach { id ->
            documentDao.deleteById(id)
        }

        val docsDir = File(context.filesDir, "documents")
        if (docsDir.exists()) {
            listOf(
                "Enterprise_Architecture_Guide.pdf",
                "Executive_NDA_Agreement.pdf",
                "Engineering_Field_Notes.pdf"
            ).forEach { filename ->
                val f = File(docsDir, filename)
                if (f.exists()) {
                    f.delete()
                }
            }
        }
    }
}

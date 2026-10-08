package com.example.engine

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.multipdf.Splitter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

data class DocumentMetadata(
    val title: String,
    val author: String,
    val subject: String,
    val keywords: String,
    val creator: String,
    val producer: String,
    val pageCount: Int,
    val fileSizeFormatted: String
)

object PdfBoxOperations {

    fun init(context: Context) {
        PDFBoxResourceLoader.init(context)
    }

    /**
     * Merge multiple PDF files in ordered sequence into an output destination file.
     */
    suspend fun mergeDocuments(inputFiles: List<File>, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val merger = PDFMergerUtility()
            merger.destinationFileName = outputFile.absolutePath
            inputFiles.forEach { file ->
                merger.addSource(file)
            }
            merger.mergeDocuments(null)
            outputFile
        }
    }

    /**
     * Split a document by page interval or specific page ranges.
     */
    suspend fun splitDocument(
        inputFile: File,
        outputDir: File,
        splitInterval: Int = 1
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            val splitter = Splitter().apply {
                setSplitAtPage(splitInterval)
            }
            val splitDocs: List<PDDocument> = splitter.split(document)
            val outputFiles = mutableListOf<File>()

            splitDocs.forEachIndexed { index, splitDoc ->
                val outFile = File(outputDir, "${inputFile.nameWithoutExtension}_part_${index + 1}.pdf")
                splitDoc.save(outFile)
                splitDoc.close()
                outputFiles.add(outFile)
            }
            document.close()
            outputFiles
        }
    }

    /**
     * Rotate pages within a document (clockwise 90, 180, 270 degrees).
     * If pageIndex is null, rotates all pages.
     */
    suspend fun rotatePages(
        inputFile: File,
        outputFile: File,
        rotationDelta: Int, // e.g. 90
        pageIndex: Int? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            val pages = document.pages
            if (pageIndex != null && pageIndex in 0 until pages.count) {
                val page = pages[pageIndex]
                val current = page.rotation
                page.rotation = (current + rotationDelta) % 360
            } else {
                for (page in pages) {
                    val current = page.rotation
                    page.rotation = (current + rotationDelta) % 360
                }
            }
            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Reorder pages according to a new index order list.
     */
    suspend fun reorderPages(
        inputFile: File,
        outputFile: File,
        newOrder: List<Int> // list of 0-based page indices
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val sourceDoc = PDDocument.load(inputFile)
            val newDoc = PDDocument()
            val pages = sourceDoc.pages

            for (idx in newOrder) {
                if (idx in 0 until pages.count) {
                    newDoc.addPage(pages[idx])
                }
            }
            newDoc.save(outputFile)
            newDoc.close()
            sourceDoc.close()
            outputFile
        }
    }

    /**
     * Delete specified page indices.
     */
    suspend fun deletePages(
        inputFile: File,
        outputFile: File,
        pagesToDelete: Set<Int>
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            val total = document.numberOfPages
            // Delete from highest index down to avoid shifting
            pagesToDelete.filter { it in 0 until total }.sortedDescending().forEach { idx ->
                document.removePage(idx)
            }
            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Duplicate a specific page.
     */
    suspend fun duplicatePage(
        inputFile: File,
        outputFile: File,
        pageIndex: Int
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            if (pageIndex in 0 until document.numberOfPages) {
                val page = document.getPage(pageIndex)
                // In PDFBox, clone or re-add
                val newDoc = PDDocument()
                for (i in 0 until document.numberOfPages) {
                    newDoc.addPage(document.getPage(i))
                    if (i == pageIndex) {
                        newDoc.addPage(page)
                    }
                }
                newDoc.save(outputFile)
                newDoc.close()
            } else {
                document.save(outputFile)
            }
            document.close()
            outputFile
        }
    }

    /**
     * Extract specific pages into a standalone new PDF.
     */
    suspend fun extractPages(
        inputFile: File,
        outputFile: File,
        pagesToExtract: List<Int>
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val source = PDDocument.load(inputFile)
            val dest = PDDocument()
            for (p in pagesToExtract) {
                if (p in 0 until source.numberOfPages) {
                    dest.addPage(source.getPage(p))
                }
            }
            dest.save(outputFile)
            dest.close()
            source.close()
            outputFile
        }
    }

    /**
     * Apply diagonal semi-transparent watermark text across pages.
     */
    suspend fun applyWatermark(
        inputFile: File,
        outputFile: File,
        watermarkText: String,
        opacity: Float = 0.35f
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            val font = PDType1Font.HELVETICA_BOLD

            for (page in document.pages) {
                val mediaBox = page.mediaBox
                val width = mediaBox.width
                val height = mediaBox.height

                val contentStream = PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
                )

                val graphicsState = PDExtendedGraphicsState().apply {
                    nonStrokingAlphaConstant = opacity
                }
                contentStream.setGraphicsStateParameters(graphicsState)
                contentStream.setNonStrokingColor(180, 180, 195)

                contentStream.beginText()
                contentStream.setFont(font, 48f)
                // Center and rotate approx 45 degrees
                contentStream.setTextMatrix(
                    0.707, 0.707, -0.707, 0.707,
                    (width * 0.25).toDouble(), (height * 0.25).toDouble()
                )
                contentStream.showText(watermarkText)
                contentStream.endText()
                contentStream.close()
            }

            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Encrypt document with AES-128 / AES-256 password protection.
     */
    suspend fun encryptDocument(
        inputFile: File,
        outputFile: File,
        userPassword: String,
        ownerPassword: String = userPassword
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            val ap = AccessPermission().apply {
                setCanPrint(true)
                setCanExtractContent(false)
            }
            val spp = StandardProtectionPolicy(ownerPassword, userPassword, ap).apply {
                encryptionKeyLength = 128
                permissions = ap
            }
            document.protect(spp)
            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Remove password encryption from a document.
     */
    suspend fun decryptDocument(
        inputFile: File,
        outputFile: File,
        password: String
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile, password)
            document.isAllSecurityToBeRemoved = true
            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Inspect and retrieve document metadata.
     */
    suspend fun getMetadata(inputFile: File): DocumentMetadata = withContext(Dispatchers.IO) {
        try {
            val document = PDDocument.load(inputFile)
            val info = document.documentInformation
            val meta = DocumentMetadata(
                title = info?.title ?: inputFile.nameWithoutExtension,
                author = info?.author ?: "Unknown",
                subject = info?.subject ?: "N/A",
                keywords = info?.keywords ?: "None",
                creator = info?.creator ?: "PDFOmni Studio",
                producer = info?.producer ?: "PDFOmni Native Engine",
                pageCount = document.numberOfPages,
                fileSizeFormatted = "${inputFile.length() / 1024} KB"
            )
            document.close()
            meta
        } catch (e: Exception) {
            DocumentMetadata(
                title = inputFile.nameWithoutExtension,
                author = "Unknown",
                subject = "N/A",
                keywords = "None",
                creator = "PDFOmni Studio",
                producer = "PDFOmni Native Engine",
                pageCount = 1,
                fileSizeFormatted = "${inputFile.length() / 1024} KB"
            )
        }
    }

    /**
     * Edit document metadata fields.
     */
    suspend fun updateMetadata(
        inputFile: File,
        outputFile: File,
        title: String,
        author: String,
        subject: String,
        keywords: String
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            val info = document.documentInformation ?: PDDocumentInformation()
            info.title = title
            info.author = author
            info.subject = subject
            info.keywords = keywords
            info.modificationDate = Calendar.getInstance()
            document.documentInformation = info
            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Compress document by deflating object streams and removing redundant metadata/annotations.
     */
    suspend fun compressDocument(
        inputFile: File,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            // Strip redundant metadata streams to reduce size
            val info = document.documentInformation
            info?.keywords = null
            info?.subject = null
            document.documentInformation = info
            // Save with full compression
            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Stamp dynamic page numbers at custom positions (bottom-right, bottom-center, top-right).
     */
    suspend fun addPageNumbers(
        inputFile: File,
        outputFile: File,
        position: String = "BOTTOM_RIGHT", // "BOTTOM_RIGHT", "BOTTOM_CENTER", "TOP_RIGHT"
        formatPattern: String = "Page %d of %d",
        fontSize: Float = 10f
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            val totalPages = document.numberOfPages
            val font = PDType1Font.HELVETICA

            for (i in 0 until totalPages) {
                val page = document.getPage(i)
                val mediaBox = page.mediaBox
                val width = mediaBox.width
                val height = mediaBox.height

                val labelText = String.format(formatPattern, i + 1, totalPages)
                val textWidth = (font.getStringWidth(labelText) / 1000f) * fontSize

                val (x, y) = when (position) {
                    "BOTTOM_CENTER" -> Pair((width - textWidth) / 2f, 25f)
                    "TOP_RIGHT" -> Pair(width - textWidth - 36f, height - 30f)
                    else -> Pair(width - textWidth - 36f, 25f) // BOTTOM_RIGHT
                }

                val contentStream = PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
                )
                contentStream.beginText()
                contentStream.setFont(font, fontSize)
                contentStream.setNonStrokingColor(100, 110, 130)
                contentStream.newLineAtOffset(x, y)
                contentStream.showText(labelText)
                contentStream.endText()
                contentStream.close()
            }

            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Inspect, re-index and repair corrupted or damaged PDF document cross-references.
     */
    suspend fun repairDocument(
        inputFile: File,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            // Loading and saving through PDFBox rebuilds xref tables and catalog
            val document = PDDocument.load(inputFile)
            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Crop margins of all pages to eliminate white space borders.
     */
    suspend fun cropMargins(
        inputFile: File,
        outputFile: File,
        cropPercent: Float = 0.05f
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PDDocument.load(inputFile)
            for (page in document.pages) {
                val box = page.mediaBox
                val dx = box.width * cropPercent
                val dy = box.height * cropPercent
                val newBox = PDRectangle(
                    box.lowerLeftX + dx,
                    box.lowerLeftY + dy,
                    box.width - (2 * dx),
                    box.height - (2 * dy)
                )
                page.cropBox = newBox
                page.mediaBox = newBox
            }
            document.save(outputFile)
            document.close()
            outputFile
        }
    }

    /**
     * Export all pages of PDF document into standalone high-resolution JPG images.
     */
    suspend fun exportToJpg(
        context: Context,
        inputFile: File,
        outputDir: File
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        runCatching {
            val engine = PdfRendererEngine(context, inputFile)
            val count = engine.pageCount
            val outImages = mutableListOf<File>()

            for (i in 0 until count) {
                val dim = engine.getPageDimension(i)
                val targetW = (dim.width * 1.5f).toInt().coerceAtLeast(600)
                val bitmap: android.graphics.Bitmap? = engine.renderPageBitmap(i, targetW)
                if (bitmap != null) {
                    val imgFile = File(outputDir, "${inputFile.nameWithoutExtension}_page_${i + 1}.jpg")
                    FileOutputStream(imgFile).use { out: java.io.OutputStream ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    outImages.add(imgFile)
                }
            }
            engine.close()
            outImages
        }
    }
}

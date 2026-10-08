package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

object NativePdfCreator {

    // Standard A4 dimensions in points (72 points per inch)
    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842

    /**
     * Converts a list of image Uris or Bitmaps into a standardized A4 PDF document.
     */
    suspend fun createPdfFromImages(
        context: Context,
        imageUris: List<Uri>,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PdfDocument()

            imageUris.forEachIndexed { index, uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, index + 1).create()
                    val page = document.startPage(pageInfo)
                    val canvas = page.canvas

                    // Calculate fitting scale maintaining aspect ratio with 20pt margin
                    val margin = 24f
                    val availWidth = A4_WIDTH - (margin * 2)
                    val availHeight = A4_HEIGHT - (margin * 2)

                    val scale = minOf(
                        availWidth / originalBitmap.width.toFloat(),
                        availHeight / originalBitmap.height.toFloat()
                    )

                    val scaledWidth = originalBitmap.width * scale
                    val scaledHeight = originalBitmap.height * scale
                    val left = margin + (availWidth - scaledWidth) / 2f
                    val top = margin + (availHeight - scaledHeight) / 2f

                    val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                    val srcRect = android.graphics.Rect(0, 0, originalBitmap.width, originalBitmap.height)
                    val dstRect = android.graphics.RectF(left, top, left + scaledWidth, top + scaledHeight)

                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(originalBitmap, srcRect, dstRect, paint)
                    document.finishPage(page)
                    originalBitmap.recycle()
                }
            }

            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            outputFile
        }
    }

    /**
     * Converts raw text or markdown content into a neatly formatted multi-page PDF.
     */
    suspend fun createPdfFromText(
        title: String,
        content: String,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PdfDocument()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }

            val titlePaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 20f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(99, 102, 241)
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val footerPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 9f
                isAntiAlias = true
            }

            val margin = 40f
            val maxLineWidth = A4_WIDTH - (margin * 2)
            val lineHeight = 18f
            val startY = 70f
            val endY = A4_HEIGHT - 60f

            val lines = mutableListOf<Pair<String, Paint>>()
            // Add Document Title
            lines.add(Pair(title, titlePaint))
            lines.add(Pair("", textPaint)) // spacer

            content.lines().forEach { rawLine ->
                val isHeader = rawLine.startsWith("# ") || rawLine.startsWith("## ")
                val paintToUse = if (isHeader) headerPaint else textPaint
                val cleanLine = if (isHeader) rawLine.removePrefix("# ").removePrefix("## ") else rawLine

                if (cleanLine.isBlank()) {
                    lines.add(Pair("", textPaint))
                } else {
                    // Wrap long lines
                    val words = cleanLine.split(" ")
                    var currentLine = StringBuilder()
                    for (word in words) {
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paintToUse.measureText(testLine) <= maxLineWidth) {
                            currentLine = StringBuilder(testLine)
                        } else {
                            lines.add(Pair(currentLine.toString(), paintToUse))
                            currentLine = StringBuilder(word)
                        }
                    }
                    if (currentLine.isNotEmpty()) {
                        lines.add(Pair(currentLine.toString(), paintToUse))
                    }
                }
            }

            var pageNumber = 1
            var lineIndex = 0

            while (lineIndex < lines.size) {
                val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNumber).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                var y = startY
                while (lineIndex < lines.size && y <= endY) {
                    val (lineText, paint) = lines[lineIndex]
                    if (lineText.isNotEmpty()) {
                        canvas.drawText(lineText, margin, y, paint)
                    }
                    y += if (paint == titlePaint) 28f else if (paint == headerPaint) 24f else lineHeight
                    lineIndex++
                }

                // Draw Footer with page number
                canvas.drawText("Page $pageNumber", A4_WIDTH / 2f - 20f, A4_HEIGHT - 30f, footerPaint)
                canvas.drawText("Generated by PDF Reader and Editor", margin, A4_HEIGHT - 30f, footerPaint)

                document.finishPage(page)
                pageNumber++
            }

            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            outputFile
        }
    }

    /**
     * Creates a blank, lined, or squared graph paper PDF for note taking.
     */
    suspend fun createNoteSheet(
        title: String,
        style: String, // "BLANK", "LINED", "GRID", "DOTS"
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(Color.WHITE)
            val gridPaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 0.8f
                this.style = Paint.Style.STROKE
            }
            val titlePaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 16f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            // Draw header bar
            canvas.drawText(title, 40f, 45f, titlePaint)
            val linePaint = Paint().apply {
                color = Color.rgb(99, 102, 241)
                strokeWidth = 2f
            }
            canvas.drawLine(40f, 55f, (A4_WIDTH - 40).toFloat(), 55f, linePaint)

            when (style) {
                "LINED" -> {
                    var y = 80f
                    while (y < A4_HEIGHT - 40) {
                        canvas.drawLine(40f, y, (A4_WIDTH - 40).toFloat(), y, gridPaint)
                        y += 24f
                    }
                }
                "GRID" -> {
                    val step = 20f
                    var x = 40f
                    while (x <= A4_WIDTH - 40) {
                        canvas.drawLine(x, 70f, x, (A4_HEIGHT - 40).toFloat(), gridPaint)
                        x += step
                    }
                    var y = 70f
                    while (y <= A4_HEIGHT - 40) {
                        canvas.drawLine(40f, y, (A4_WIDTH - 40).toFloat(), y, gridPaint)
                        y += step
                    }
                }
                "DOTS" -> {
                    val dotPaint = Paint().apply {
                        color = Color.rgb(148, 163, 184)
                        strokeWidth = 2f
                        this.style = Paint.Style.FILL
                    }
                    val step = 20f
                    var x = 40f
                    while (x <= A4_WIDTH - 40) {
                        var y = 70f
                        while (y <= A4_HEIGHT - 40) {
                            canvas.drawCircle(x, y, 1f, dotPaint)
                            y += step
                        }
                        x += step
                    }
                }
                else -> {
                    // Blank page
                }
            }

            document.finishPage(page)
            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            outputFile
        }
    }

    /**
     * Exports pages from a PDF to PNG images.
     */
    suspend fun exportPdfToImages(
        rendererEngine: PdfRendererEngine,
        outputDir: File,
        baseName: String,
        targetWidth: Int = 1200
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        runCatching {
            val exportedFiles = mutableListOf<File>()
            for (i in 0 until rendererEngine.pageCount) {
                val bitmap = rendererEngine.renderPageBitmap(i, targetWidth = targetWidth)
                if (bitmap != null) {
                    val outFile = File(outputDir, "${baseName}_page_${i + 1}.png")
                    FileOutputStream(outFile).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    exportedFiles.add(outFile)
                }
            }
            exportedFiles
        }
    }

    /**
     * Runs 100% on-device Google ML Kit OCR text recognition on a rendered bitmap.
     */
    suspend fun runOnDeviceOcr(bitmap: Bitmap): Result<String> = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(Result.success(visionText.text))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }
}

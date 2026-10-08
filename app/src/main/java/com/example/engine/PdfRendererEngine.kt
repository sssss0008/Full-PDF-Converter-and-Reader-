package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import com.example.domain.model.OutlineNode
import com.example.domain.model.PageDimension
import com.example.domain.model.SearchMatch
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class PdfRendererEngine(
    private val context: Context,
    private val pdfFile: File
) : AutoCloseable {

    private var fileDescriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    private val mutex = Mutex()

    // Capped memory cache for rendered page bitmaps (approx 64MB cap)
    private val maxCacheSize = (Runtime.getRuntime().maxMemory() / 8).toInt().coerceAtLeast(16 * 1024 * 1024)
    private val bitmapCache = object : LruCache<String, Bitmap>(maxCacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }
    }

    private var _pageCount: Int = 0
    val pageCount: Int get() = _pageCount

    private val dimensionsCache = mutableMapOf<Int, PageDimension>()
    private val extractedTextCache = mutableMapOf<Int, String>()

    init {
        try {
            PDFBoxResourceLoader.init(context)
            fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(fileDescriptor!!)
            _pageCount = renderer!!.pageCount
        } catch (e: Exception) {
            e.printStackTrace()
            _pageCount = 0
        }
    }

    suspend fun getPageDimension(pageIndex: Int): PageDimension = withContext(Dispatchers.IO) {
        if (pageIndex < 0 || pageIndex >= _pageCount) return@withContext PageDimension(pageIndex, 1080, 1920)
        dimensionsCache[pageIndex]?.let { return@withContext it }

        mutex.withLock {
            val rend = renderer ?: return@withContext PageDimension(pageIndex, 1080, 1920)
            try {
                val page = rend.openPage(pageIndex)
                val dim = PageDimension(pageIndex, page.width, page.height)
                page.close()
                dimensionsCache[pageIndex] = dim
                dim
            } catch (e: Exception) {
                PageDimension(pageIndex, 1080, 1920)
            }
        }
    }

    suspend fun renderPageBitmap(
        pageIndex: Int,
        targetWidth: Int,
        invertColors: Boolean = false,
        colorFilterMode: String = if (invertColors) "OLED_INVERT" else "NORMAL"
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (pageIndex < 0 || pageIndex >= _pageCount) return@withContext null
        val cacheKey = "p_${pageIndex}_w_${targetWidth}_filter_${colorFilterMode}"
        bitmapCache.get(cacheKey)?.let { return@withContext it }

        mutex.withLock {
            val rend = renderer ?: return@withContext null
            try {
                val page = rend.openPage(pageIndex)
                val originalWidth = page.width
                val originalHeight = page.height
                val scale = if (targetWidth > 0) targetWidth.toFloat() / originalWidth else 1.0f
                val destWidth = (originalWidth * scale).toInt().coerceAtLeast(1)
                val destHeight = (originalHeight * scale).toInt().coerceAtLeast(1)

                val bitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(android.graphics.Color.WHITE)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val resultBitmap = when (colorFilterMode) {
                    "OLED_INVERT" -> invertBitmapColors(bitmap)
                    "SEPIA" -> applySepiaFilter(bitmap)
                    "SLATE" -> applySlateFilter(bitmap)
                    else -> bitmap
                }

                bitmapCache.put(cacheKey, resultBitmap)
                resultBitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun renderThumbnail(pageIndex: Int, size: Int = 180): Bitmap? = withContext(Dispatchers.IO) {
        renderPageBitmap(pageIndex, targetWidth = size, invertColors = false)
    }

    private fun invertBitmapColors(src: Bitmap): Bitmap {
        val inverted = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(inverted)
        val paint = Paint()

        // OLED Inversion Color Matrix: invert RGB, slightly preserve hues
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                -1f,  0f,  0f, 0f, 255f,
                 0f, -1f,  0f, 0f, 255f,
                 0f,  0f, -1f, 0f, 255f,
                 0f,  0f,  0f, 1f,   0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return inverted
    }

    private fun applySepiaFilter(src: Bitmap): Bitmap {
        val sepia = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sepia)
        val paint = Paint()

        val colorMatrix = ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f,     0f,     0f,     1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return sepia
    }

    private fun applySlateFilter(src: Bitmap): Bitmap {
        val slate = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(slate)
        val paint = Paint()

        val colorMatrix = ColorMatrix(
            floatArrayOf(
                -0.85f, 0f,     0f,     0f, 210f,
                0f,     -0.85f, 0f,     0f, 220f,
                0f,     0f,     -0.75f, 0f, 245f,
                0f,     0f,     0f,     1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return slate
    }

    suspend fun extractAllText(): String = withContext(Dispatchers.IO) {
        try {
            val document = PDDocument.load(pdfFile)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            text
        } catch (e: Exception) {
            e.printStackTrace()
            "No extractable text found."
        }
    }

    suspend fun extractPageText(pageIndex: Int): String = withContext(Dispatchers.IO) {
        extractedTextCache[pageIndex]?.let { return@withContext it }
        try {
            val document = PDDocument.load(pdfFile)
            val stripper = PDFTextStripper()
            stripper.startPage = pageIndex + 1
            stripper.endPage = pageIndex + 1
            val text = stripper.getText(document)
            document.close()
            val result = text.trim()
            extractedTextCache[pageIndex] = result
            result
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun searchKeyword(query: String): List<SearchMatch> = withContext(Dispatchers.IO) {
        if (query.isBlank() || _pageCount == 0) return@withContext emptyList()
        val results = mutableListOf<SearchMatch>()
        try {
            val document = PDDocument.load(pdfFile)
            val stripper = PDFTextStripper()
            for (p in 0 until _pageCount) {
                stripper.startPage = p + 1
                stripper.endPage = p + 1
                val text = stripper.getText(document)
                if (text.contains(query, ignoreCase = true)) {
                    val idx = text.indexOf(query, ignoreCase = true)
                    val start = (idx - 30).coerceAtLeast(0)
                    val end = (idx + query.length + 30).coerceAtMost(text.length)
                    val snippet = "…" + text.substring(start, end).replace("\n", " ") + "…"
                    results.add(SearchMatch(p, snippet, 1))
                }
            }
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        results
    }

    suspend fun extractOutline(): List<OutlineNode> = withContext(Dispatchers.IO) {
        val rootNodes = mutableListOf<OutlineNode>()
        try {
            val document = PDDocument.load(pdfFile)
            val outline = document.documentCatalog.documentOutline
            if (outline != null) {
                var current: PDOutlineItem? = outline.firstChild
                while (current != null) {
                    val title = current.title ?: "Section"
                    val destPage = try {
                        val page = current.findDestinationPage(document)
                        if (page != null) document.pages.indexOf(page) else 0
                    } catch (e: Exception) {
                        0
                    }
                    rootNodes.add(OutlineNode(title = title, pageIndex = destPage.coerceAtLeast(0)))
                    current = current.nextSibling
                }
            }
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If document doesn't have an embedded outline, synthesize smart structural chapters
        if (rootNodes.isEmpty() && _pageCount > 0) {
            for (i in 0 until _pageCount) {
                rootNodes.add(OutlineNode(title = "Page ${i + 1}", pageIndex = i))
            }
        }
        rootNodes
    }

    override fun close() {
        try {
            renderer?.close()
            renderer = null
            fileDescriptor?.close()
            fileDescriptor = null
            bitmapCache.evictAll()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

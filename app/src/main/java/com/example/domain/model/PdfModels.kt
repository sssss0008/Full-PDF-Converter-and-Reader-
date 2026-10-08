package com.example.domain.model

import androidx.compose.ui.graphics.Color

data class PageDimension(
    val pageIndex: Int,
    val width: Int,
    val height: Int
)

data class OutlineNode(
    val title: String,
    val pageIndex: Int,
    val level: Int = 0,
    val children: List<OutlineNode> = emptyList()
)

data class SearchMatch(
    val pageIndex: Int,
    val snippet: String,
    val matchCountOnPage: Int
)

enum class AnnotationTool {
    NONE,
    PEN,
    HIGHLIGHTER,
    SHAPE_RECTANGLE,
    SHAPE_CIRCLE,
    SHAPE_ARROW,
    STAMP,
    NOTE,
    SIGNATURE
}

enum class StampType(val label: String, val colorHex: Long) {
    APPROVED("APPROVED", 0xFF10B981),
    CONFIDENTIAL("CONFIDENTIAL", 0xFFEF4444),
    DRAFT("DRAFT", 0xFFF59E0B),
    VOID("VOID", 0xFF6B7280),
    PAID("PAID", 0xFF3B82F6),
    REVIEWED("REVIEWED", 0xFF8B5CF6),
    URGENT("URGENT", 0xFFDC2626),
    COMPLETED("COMPLETED", 0xFF059669),
    OFFICIAL("OFFICIAL", 0xFF2563EB)
}

data class InkPoint(
    val x: Float,
    val y: Float
)

data class InkStroke(
    val id: String = java.util.UUID.randomUUID().toString(),
    val points: List<InkPoint>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false,
    val pageIndex: Int
)

data class ShapeAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tool: AnnotationTool,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val color: Color,
    val strokeWidth: Float,
    val pageIndex: Int
)

data class StampAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val stamp: StampType,
    val customText: String? = null,
    val x: Float,
    val y: Float,
    val pageIndex: Int
)

data class StickyNoteAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val x: Float,
    val y: Float,
    val pageIndex: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class SignatureStampAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bitmapPath: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val pageIndex: Int
)

enum class ViewDisplayMode {
    CONTINUOUS_VERTICAL,
    SINGLE_PAGE_FLIP,
    LIQUID_REFLOW,
    DUAL_SPREAD
}

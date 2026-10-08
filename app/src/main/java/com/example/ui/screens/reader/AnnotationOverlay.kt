package com.example.ui.screens.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AnnotationTool
import com.example.domain.model.InkPoint
import com.example.domain.model.InkStroke
import com.example.domain.model.ShapeAnnotation
import com.example.domain.model.StampAnnotation
import com.example.domain.model.StickyNoteAnnotation

@Composable
fun AnnotationCanvasOverlay(
    modifier: Modifier = Modifier,
    pageIndex: Int,
    activeTool: AnnotationTool,
    currentColor: Color,
    currentStrokeWidth: Float,
    strokes: List<InkStroke>,
    shapes: List<ShapeAnnotation>,
    stamps: List<StampAnnotation>,
    stickyNotes: List<StickyNoteAnnotation>,
    onStrokeAdded: (InkStroke) -> Unit,
    onShapeAdded: (ShapeAnnotation) -> Unit,
    onStampAdded: (Float, Float) -> Unit,
    onStickyNoteClicked: (StickyNoteAnnotation) -> Unit,
    onAddStickyNote: (Float, Float) -> Unit
) {
    val currentPoints = remember { mutableStateListOf<InkPoint>() }
    val shapeStart = remember { mutableStateOf<Offset?>(null) }
    val shapeCurrent = remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(activeTool, currentColor, currentStrokeWidth) {
                if (activeTool == AnnotationTool.NONE) return@pointerInput

                if (activeTool == AnnotationTool.STAMP) {
                    detectTapGestures { offset ->
                        onStampAdded(offset.x, offset.y)
                    }
                } else if (activeTool == AnnotationTool.NOTE) {
                    detectTapGestures { offset ->
                        onAddStickyNote(offset.x, offset.y)
                    }
                } else if (activeTool == AnnotationTool.PEN || activeTool == AnnotationTool.HIGHLIGHTER) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.clear()
                            currentPoints.add(InkPoint(offset.x, offset.y))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints.add(InkPoint(change.position.x, change.position.y))
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                onStrokeAdded(
                                    InkStroke(
                                        points = currentPoints.toList(),
                                        color = if (activeTool == AnnotationTool.HIGHLIGHTER) currentColor.copy(alpha = 0.4f) else currentColor,
                                        strokeWidth = if (activeTool == AnnotationTool.HIGHLIGHTER) currentStrokeWidth * 3f else currentStrokeWidth,
                                        isHighlighter = activeTool == AnnotationTool.HIGHLIGHTER,
                                        pageIndex = pageIndex
                                    )
                                )
                                currentPoints.clear()
                            }
                        },
                        onDragCancel = {
                            currentPoints.clear()
                        }
                    )
                } else if (activeTool == AnnotationTool.SHAPE_RECTANGLE || activeTool == AnnotationTool.SHAPE_CIRCLE || activeTool == AnnotationTool.SHAPE_ARROW) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            shapeStart.value = offset
                            shapeCurrent.value = offset
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            shapeCurrent.value = change.position
                        },
                        onDragEnd = {
                            val start = shapeStart.value
                            val end = shapeCurrent.value
                            if (start != null && end != null) {
                                onShapeAdded(
                                    ShapeAnnotation(
                                        tool = activeTool,
                                        startX = start.x,
                                        startY = start.y,
                                        endX = end.x,
                                        endY = end.y,
                                        color = currentColor,
                                        strokeWidth = currentStrokeWidth,
                                        pageIndex = pageIndex
                                    )
                                )
                            }
                            shapeStart.value = null
                            shapeCurrent.value = null
                        },
                        onDragCancel = {
                            shapeStart.value = null
                            shapeCurrent.value = null
                        }
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Draw existing strokes for this page
            strokes.filter { it.pageIndex == pageIndex }.forEach { stroke ->
                if (stroke.points.size > 1) {
                    val path = Path().apply {
                        moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            val prev = stroke.points[i - 1]
                            val curr = stroke.points[i]
                            val midX = (prev.x + curr.x) / 2f
                            val midY = (prev.y + curr.y) / 2f
                            quadraticTo(prev.x, prev.y, midX, midY)
                        }
                    }
                    drawPath(
                        path = path,
                        color = stroke.color,
                        style = Stroke(
                            width = stroke.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        ),
                        blendMode = if (stroke.isHighlighter) BlendMode.Multiply else BlendMode.SrcOver
                    )
                }
            }

            // 2. Draw active in-progress stroke
            if (currentPoints.size > 1) {
                val path = Path().apply {
                    moveTo(currentPoints[0].x, currentPoints[0].y)
                    for (i in 1 until currentPoints.size) {
                        val prev = currentPoints[i - 1]
                        val curr = currentPoints[i]
                        val midX = (prev.x + curr.x) / 2f
                        val midY = (prev.y + curr.y) / 2f
                        quadraticTo(prev.x, prev.y, midX, midY)
                    }
                }
                val isHighlighter = activeTool == AnnotationTool.HIGHLIGHTER
                drawPath(
                    path = path,
                    color = if (isHighlighter) currentColor.copy(alpha = 0.45f) else currentColor,
                    style = Stroke(
                        width = if (isHighlighter) currentStrokeWidth * 3f else currentStrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    blendMode = if (isHighlighter) BlendMode.Multiply else BlendMode.SrcOver
                )
            }

            // 3. Draw shapes
            shapes.filter { it.pageIndex == pageIndex }.forEach { shape ->
                drawShape(shape.tool, shape.startX, shape.startY, shape.endX, shape.endY, shape.color, shape.strokeWidth)
            }

            // Draw active shape being dragged
            val start = shapeStart.value
            val current = shapeCurrent.value
            if (start != null && current != null) {
                drawShape(activeTool, start.x, start.y, current.x, current.y, currentColor, currentStrokeWidth)
            }
        }

        // 4. Render Stamps overlay
        stamps.filter { it.pageIndex == pageIndex }.forEach { stamp ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(stamp.x.toInt() - 60, stamp.y.toInt() - 24) }
                    .border(
                        width = 2.dp,
                        color = Color(stamp.stamp.colorHex),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(Color(stamp.stamp.colorHex).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stamp.customText ?: stamp.stamp.label,
                    color = Color(stamp.stamp.colorHex),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        // 5. Render Sticky Notes pins
        stickyNotes.filter { it.pageIndex == pageIndex }.forEach { note ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(note.x.toInt() - 16, note.y.toInt() - 16) }
                    .clickable { onStickyNoteClicked(note) }
                    .background(Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Sticky Note",
                    tint = Color.White
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShape(
    tool: AnnotationTool,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    color: Color,
    strokeWidth: Float
) {
    val left = minOf(startX, endX)
    val top = minOf(startY, endY)
    val width = kotlin.math.abs(endX - startX)
    val height = kotlin.math.abs(endY - startY)

    when (tool) {
        AnnotationTool.SHAPE_RECTANGLE -> {
            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = strokeWidth)
            )
        }
        AnnotationTool.SHAPE_CIRCLE -> {
            drawOval(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = strokeWidth)
            )
        }
        AnnotationTool.SHAPE_ARROW -> {
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Arrowhead
            val angle = Math.atan2((endY - startY).toDouble(), (endX - startX).toDouble())
            val arrowLength = 24f
            val arrowAngle = Math.PI / 6
            val x1 = (endX - arrowLength * Math.cos(angle - arrowAngle)).toFloat()
            val y1 = (endY - arrowLength * Math.sin(angle - arrowAngle)).toFloat()
            val x2 = (endX - arrowLength * Math.cos(angle + arrowAngle)).toFloat()
            val y2 = (endY - arrowLength * Math.sin(angle + arrowAngle)).toFloat()
            drawLine(color = color, start = Offset(endX, endY), end = Offset(x1, y1), strokeWidth = strokeWidth)
            drawLine(color = color, start = Offset(endX, endY), end = Offset(x2, y2), strokeWidth = strokeWidth)
        }
        else -> Unit
    }
}

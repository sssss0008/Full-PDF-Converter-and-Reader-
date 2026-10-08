package com.example.ui.screens.reader

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.domain.model.AnnotationTool
import com.example.domain.model.InkStroke
import com.example.domain.model.OutlineNode
import com.example.domain.model.SearchMatch
import com.example.domain.model.ShapeAnnotation
import com.example.domain.model.StampAnnotation
import com.example.domain.model.StampType
import com.example.domain.model.StickyNoteAnnotation
import com.example.domain.model.ViewDisplayMode
import com.example.engine.PdfRendererEngine
import com.example.engine.TtsManager
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHighlight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    documentId: String,
    database: AppDatabase,
    onNavigateBack: () -> Unit,
    onNavigateToOrganizer: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var docTitle by remember { mutableStateOf("Document Viewer") }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var engine by remember { mutableStateOf<PdfRendererEngine?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var initialPage by remember { mutableIntStateOf(0) }
    var initialZoom by remember { mutableFloatStateOf(1f) }

    // Display & Reading Modes
    var viewMode by remember { mutableStateOf(ViewDisplayMode.CONTINUOUS_VERTICAL) }
    var isOledDarkInverted by remember { mutableStateOf(false) }
    var isHudVisible by remember { mutableStateOf(true) }

    // Zoom & Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // TTS Engine
    val ttsManager = remember { TtsManager(context) }
    val isTtsPlaying by ttsManager.isPlaying.collectAsState()
    var isTtsSheetOpen by remember { mutableStateOf(false) }

    // Search Engine
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchMatch>>(emptyList()) }
    var currentMatchIdx by remember { mutableIntStateOf(0) }

    // Table of Contents
    var isTocOpen by remember { mutableStateOf(false) }
    var outlineNodes by remember { mutableStateOf<List<OutlineNode>>(emptyList()) }

    // Liquid Reflow Mode
    var isReflowOpen by remember { mutableStateOf(false) }
    var reflowText by remember { mutableStateOf("") }
    var reflowFontSize by remember { mutableFloatStateOf(16f) }

    // Annotation Tools
    var isAnnotationBarOpen by remember { mutableStateOf(false) }
    var activeTool by remember { mutableStateOf(AnnotationTool.NONE) }
    var currentColor by remember { mutableStateOf(NeonIndigo) }
    var currentStrokeWidth by remember { mutableFloatStateOf(4f) }
    var selectedStamp by remember { mutableStateOf(StampType.APPROVED) }

    // In-memory Annotation state
    val strokes = remember { mutableStateListOf<InkStroke>() }
    val shapes = remember { mutableStateListOf<ShapeAnnotation>() }
    val stamps = remember { mutableStateListOf<StampAnnotation>() }
    val stickyNotes = remember { mutableStateListOf<StickyNoteAnnotation>() }

    // Sticky Note Dialog
    var activeNoteToView by remember { mutableStateOf<StickyNoteAnnotation?>(null) }
    var newNoteCoord by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var newNoteText by remember { mutableStateOf("") }

    // Scrubber
    var scrubberPage by remember { mutableIntStateOf(0) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubberThumbnail by remember { mutableStateOf<Bitmap?>(null) }

    val lazyListState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { pageCount.coerceAtLeast(1) })

    // Track active page
    val activePageIndex by remember {
        derivedStateOf {
            if (viewMode == ViewDisplayMode.CONTINUOUS_VERTICAL) {
                lazyListState.firstVisibleItemIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            } else {
                pagerState.currentPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            }
        }
    }

    // Save reading progress on back
    BackHandler {
        scope.launch(Dispatchers.IO) {
            database.documentDao().updateReadingProgress(
                id = documentId,
                page = activePageIndex,
                zoom = scale,
                timestamp = System.currentTimeMillis()
            )
        }
        ttsManager.stop()
        onNavigateBack()
    }

    // Load Document
    LaunchedEffect(documentId) {
        val docEntity = withContext(Dispatchers.IO) { database.documentDao().getDocumentById(documentId) }
        if (docEntity != null) {
            docTitle = docEntity.title
            val file = File(docEntity.filePath)
            if (file.exists()) {
                pdfFile = file
                val newEngine = PdfRendererEngine(context, file)
                engine = newEngine
                pageCount = newEngine.pageCount
                initialPage = docEntity.lastReadPage
                initialZoom = docEntity.lastZoomFactor.coerceIn(1f, 5f)
                scale = initialZoom

                // Preload TOC
                outlineNodes = newEngine.extractOutline()

                // Scroll to last read page
                if (initialPage in 0 until pageCount) {
                    if (viewMode == ViewDisplayMode.CONTINUOUS_VERTICAL) {
                        lazyListState.scrollToItem(initialPage)
                    } else {
                        pagerState.scrollToPage(initialPage)
                    }
                }
            }
        }
    }

    // Cleanup resources on disposal
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.release()
            engine?.close()
        }
    }

    // Transform gesture state for sub-pixel pinch-to-zoom (100% to 1000%)
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 10f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        // Main Viewer Canvas Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformableState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            // Double tap resets zoom or zooms to 2.5x
                            if (scale > 1.2f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f
                            }
                        },
                        onTap = {
                            if (activeTool == AnnotationTool.NONE) {
                                isHudVisible = !isHudVisible
                            }
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        ) {
            if (engine == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonIndigo)
                }
            } else if (viewMode == ViewDisplayMode.CONTINUOUS_VERTICAL) {
                // Continuous LazyColumn Mode
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp, start = 8.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(pageCount) { pageIdx ->
                        PageRendererItem(
                            engine = engine!!,
                            pageIndex = pageIdx,
                            invertColors = isOledDarkInverted,
                            activeTool = activeTool,
                            currentColor = currentColor,
                            currentStrokeWidth = currentStrokeWidth,
                            strokes = strokes,
                            shapes = shapes,
                            stamps = stamps,
                            stickyNotes = stickyNotes,
                            onStrokeAdded = { strokes.add(it) },
                            onShapeAdded = { shapes.add(it) },
                            onStampAdded = { x, y ->
                                stamps.add(StampAnnotation(stamp = selectedStamp, x = x, y = y, pageIndex = pageIdx))
                            },
                            onStickyNoteClicked = { activeNoteToView = it },
                            onAddStickyNote = { x, y ->
                                newNoteCoord = Pair(x, y)
                            }
                        )
                    }
                }
            } else {
                // Single-Page Snapping Horizontal Pager Mode
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, bottom = 100.dp)
                ) { pageIdx ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PageRendererItem(
                            engine = engine!!,
                            pageIndex = pageIdx,
                            invertColors = isOledDarkInverted,
                            activeTool = activeTool,
                            currentColor = currentColor,
                            currentStrokeWidth = currentStrokeWidth,
                            strokes = strokes,
                            shapes = shapes,
                            stamps = stamps,
                            stickyNotes = stickyNotes,
                            onStrokeAdded = { strokes.add(it) },
                            onShapeAdded = { shapes.add(it) },
                            onStampAdded = { x, y ->
                                stamps.add(StampAnnotation(stamp = selectedStamp, x = x, y = y, pageIndex = pageIdx))
                            },
                            onStickyNoteClicked = { activeNoteToView = it },
                            onAddStickyNote = { x, y ->
                                newNoteCoord = Pair(x, y)
                            }
                        )
                    }
                }
            }
        }

        // Top Floating HUD App Bar
        AnimatedVisibility(
            visible = isHudVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = ObsidianSurface.copy(alpha = 0.94f),
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                database.documentDao().updateReadingProgress(
                                    id = documentId,
                                    page = activePageIndex,
                                    zoom = scale,
                                    timestamp = System.currentTimeMillis()
                                )
                            }
                            ttsManager.stop()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = docTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (pageCount > 0) "Page ${activePageIndex + 1} of $pageCount • ${(scale * 100).toInt()}%" else "Loading…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    // OLED Dark Mode Toggle
                    IconButton(
                        onClick = { isOledDarkInverted = !isOledDarkInverted },
                        modifier = Modifier.testTag("toggle_oled_invert_button")
                    ) {
                        Icon(
                            imageVector = if (isOledDarkInverted) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "OLED Invert",
                            tint = if (isOledDarkInverted) ElectricCyan else Color.White
                        )
                    }

                    // View Mode Toggle (Continuous vs Single Page)
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == ViewDisplayMode.CONTINUOUS_VERTICAL) {
                                ViewDisplayMode.SINGLE_PAGE_FLIP
                            } else {
                                ViewDisplayMode.CONTINUOUS_VERTICAL
                            }
                        },
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = if (viewMode == ViewDisplayMode.CONTINUOUS_VERTICAL) Icons.Default.ViewCarousel else Icons.Default.ViewDay,
                            contentDescription = "View Mode",
                            tint = Color.White
                        )
                    }

                    // Search Button
                    IconButton(
                        onClick = { isSearchActive = !isSearchActive },
                        modifier = Modifier.testTag("reader_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = if (isSearchActive) ElectricCyan else Color.White)
                    }

                    // Annotations Toggle
                    IconButton(
                        onClick = {
                            isAnnotationBarOpen = !isAnnotationBarOpen
                            if (!isAnnotationBarOpen) activeTool = AnnotationTool.NONE
                        },
                        modifier = Modifier.testTag("reader_annotation_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Annotate", tint = if (isAnnotationBarOpen) NeonIndigo else Color.White)
                    }

                    // Table of Contents
                    IconButton(
                        onClick = { isTocOpen = true },
                        modifier = Modifier.testTag("reader_toc_button")
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Outline", tint = Color.White)
                    }
                }
            }
        }

        // Search Overlay Panel
        AnimatedVisibility(
            visible = isSearchActive,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 74.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = ObsidianSurface,
                tonalElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                if (it.length >= 2) {
                                    scope.launch {
                                        searchResults = engine?.searchKeyword(it) ?: emptyList()
                                        currentMatchIdx = 0
                                    }
                                } else {
                                    searchResults = emptyList()
                                }
                            },
                            placeholder = { Text("Find text in document…", fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_text_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = ObsidianSurfaceHighlight,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                            searchResults = emptyList()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color.White)
                        }
                    }

                    if (searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${searchResults.size} occurrences found",
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            searchResults.forEachIndexed { index, match ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable {
                                            scope.launch {
                                                if (viewMode == ViewDisplayMode.CONTINUOUS_VERTICAL) {
                                                    lazyListState.animateScrollToItem(match.pageIndex)
                                                } else {
                                                    pagerState.animateScrollToPage(match.pageIndex)
                                                }
                                            }
                                        },
                                    color = if (index == currentMatchIdx) NeonIndigo.copy(alpha = 0.2f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Page ${match.pageIndex + 1}: ",
                                            color = NeonIndigo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = match.snippet,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Annotation Floating Palette Bar
        AnimatedVisibility(
            visible = isAnnotationBarOpen,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = if (isSearchActive) 210.dp else 74.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = ObsidianSurface,
                tonalElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonIndigo.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pen Tool
                    IconButton(
                        onClick = { activeTool = if (activeTool == AnnotationTool.PEN) AnnotationTool.NONE else AnnotationTool.PEN }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Pen",
                            tint = if (activeTool == AnnotationTool.PEN) NeonIndigo else Color.White
                        )
                    }

                    // Highlighter Tool
                    IconButton(
                        onClick = { activeTool = if (activeTool == AnnotationTool.HIGHLIGHTER) AnnotationTool.NONE else AnnotationTool.HIGHLIGHTER }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Highlight,
                            contentDescription = "Highlighter",
                            tint = if (activeTool == AnnotationTool.HIGHLIGHTER) ElectricCyan else Color.White
                        )
                    }

                    // Rectangle Shape Tool
                    IconButton(
                        onClick = { activeTool = if (activeTool == AnnotationTool.SHAPE_RECTANGLE) AnnotationTool.NONE else AnnotationTool.SHAPE_RECTANGLE }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Square,
                            contentDescription = "Rectangle",
                            tint = if (activeTool == AnnotationTool.SHAPE_RECTANGLE) NeonIndigo else Color.White
                        )
                    }

                    // Stamp Tool
                    IconButton(
                        onClick = { activeTool = if (activeTool == AnnotationTool.STAMP) AnnotationTool.NONE else AnnotationTool.STAMP }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatColorFill,
                            contentDescription = "Stamp",
                            tint = if (activeTool == AnnotationTool.STAMP) CrimsonWarning else Color.White
                        )
                    }

                    // Sticky Note
                    IconButton(
                        onClick = { activeTool = if (activeTool == AnnotationTool.NOTE) AnnotationTool.NONE else AnnotationTool.NOTE }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Note",
                            tint = if (activeTool == AnnotationTool.NOTE) Color(0xFFF59E0B) else Color.White
                        )
                    }

                    // Undo / Clear
                    IconButton(
                        onClick = {
                            if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                            else if (shapes.isNotEmpty()) shapes.removeAt(shapes.lastIndex)
                            else if (stamps.isNotEmpty()) stamps.removeAt(stamps.lastIndex)
                        }
                    ) {
                        Text("Undo", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom HUD: Page Scrubber Bar, Page Jump, TTS & Reflow Actions
        AnimatedVisibility(
            visible = isHudVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = ObsidianSurface.copy(alpha = 0.94f),
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Floating Thumbnail Scrubber Bar Preview
                    if (isScrubbing && scrubberThumbnail != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ObsidianSurfaceHighlight,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonIndigo),
                                shadowElevation = 12.dp
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(6.dp)
                                ) {
                                    Image(
                                        bitmap = scrubberThumbnail!!.asImageBitmap(),
                                        contentDescription = "Page Preview",
                                        modifier = Modifier
                                            .size(90.dp, 120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Text(
                                        text = "Page ${scrubberPage + 1}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Scrubber Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activePageIndex + 1}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = activePageIndex.toFloat(),
                            onValueChange = { newVal ->
                                isScrubbing = true
                                val targetP = newVal.toInt().coerceIn(0, (pageCount - 1).coerceAtLeast(0))
                                scrubberPage = targetP
                                scope.launch {
                                    scrubberThumbnail = engine?.renderThumbnail(targetP, size = 140)
                                }
                            },
                            onValueChangeFinished = {
                                isScrubbing = false
                                scope.launch {
                                    if (viewMode == ViewDisplayMode.CONTINUOUS_VERTICAL) {
                                        lazyListState.scrollToItem(scrubberPage)
                                    } else {
                                        pagerState.scrollToPage(scrubberPage)
                                    }
                                }
                            },
                            valueRange = 0f..(pageCount - 1).coerceAtLeast(0).toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("page_scrubber_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonIndigo,
                                activeTrackColor = NeonIndigo,
                                inactiveTrackColor = ObsidianSurfaceHighlight
                            )
                        )
                        Text(
                            text = "$pageCount",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    // Quick Actions Row: TTS, Text Reflow, Organize Pages, Tools
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // TTS Button
                        TextButton(
                            onClick = {
                                isTtsSheetOpen = true
                                if (!isTtsPlaying) {
                                    scope.launch {
                                        val text = engine?.extractPageText(activePageIndex) ?: ""
                                        ttsManager.speak(text.ifBlank { "No extractable text on page ${activePageIndex + 1}." })
                                    }
                                }
                            },
                            modifier = Modifier.testTag("tts_reader_button")
                        ) {
                            Icon(
                                imageVector = if (isTtsPlaying) Icons.Default.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "TTS",
                                tint = if (isTtsPlaying) ElectricCyan else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTtsPlaying) "Reading…" else "Read Aloud",
                                color = if (isTtsPlaying) ElectricCyan else Color.White,
                                fontSize = 12.sp
                            )
                        }

                        // Liquid Text Reflow
                        TextButton(
                            onClick = {
                                scope.launch {
                                    reflowText = engine?.extractPageText(activePageIndex) ?: "No text content found on this page."
                                    isReflowOpen = true
                                }
                            },
                            modifier = Modifier.testTag("text_reflow_button")
                        ) {
                            Icon(Icons.Default.FormatSize, contentDescription = "Reflow", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Text Reflow", color = Color.White, fontSize = 12.sp)
                        }

                        // Organize Pages Shortcut
                        TextButton(
                            onClick = { onNavigateToOrganizer(documentId) },
                            modifier = Modifier.testTag("organize_pages_button")
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = "Organize", tint = NeonIndigo, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Organize", color = NeonIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Table of Contents Modal Bottom Sheet
        if (isTocOpen) {
            ModalBottomSheet(
                onDismissRequest = { isTocOpen = false },
                containerColor = ObsidianSurface,
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Table of Contents & Bookmarks",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (outlineNodes.isEmpty()) {
                        Text("No outline sections detected in this document.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            items(outlineNodes.size) { idx ->
                                val node = outlineNodes[idx]
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            isTocOpen = false
                                            scope.launch {
                                                if (viewMode == ViewDisplayMode.CONTINUOUS_VERTICAL) {
                                                    lazyListState.scrollToItem(node.pageIndex)
                                                } else {
                                                    pagerState.scrollToPage(node.pageIndex)
                                                }
                                            }
                                        },
                                    color = ObsidianSurfaceHighlight,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = node.title, color = Color.White, fontWeight = FontWeight.Medium)
                                        Text(text = "p. ${node.pageIndex + 1}", color = NeonIndigo, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Liquid Text Reflow Modal Sheet
        if (isReflowOpen) {
            ModalBottomSheet(
                onDismissRequest = { isReflowOpen = false },
                containerColor = ObsidianBackground,
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Liquid Text Reflow (Page ${activePageIndex + 1})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { reflowFontSize = (reflowFontSize - 2f).coerceAtLeast(12f) }) {
                                Text("A-", color = ElectricCyan, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { reflowFontSize = (reflowFontSize + 2f).coerceAtMost(32f) }) {
                                Text("A+", color = ElectricCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    HorizontalDivider(color = ObsidianSurfaceHighlight, modifier = Modifier.padding(vertical = 12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = reflowText,
                            color = Color(0xFFF1F5F9),
                            fontSize = reflowFontSize.sp,
                            lineHeight = (reflowFontSize * 1.6f).sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Text-to-Speech Control Sheet
        if (isTtsSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { isTtsSheetOpen = false },
                containerColor = ObsidianSurface,
                sheetState = rememberModalBottomSheetState()
            ) {
                val speechRate by ttsManager.speechRate.collectAsState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Text-to-Speech Audio Playback",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isTtsPlaying) {
                                    ttsManager.stop()
                                } else {
                                    scope.launch {
                                        val text = engine?.extractPageText(activePageIndex) ?: ""
                                        ttsManager.speak(text)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeonIndigo)
                        ) {
                            Icon(
                                imageVector = if (isTtsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { ttsManager.stop() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ObsidianSurfaceHighlight)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = CrimsonWarning)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Reading Speed: ${"%.1f".format(speechRate)}x", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Slider(
                        value = speechRate,
                        onValueChange = { ttsManager.setRate(it) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // New Sticky Note Dialog
        if (newNoteCoord != null) {
            AlertDialog(
                onDismissRequest = { newNoteCoord = null },
                containerColor = ObsidianSurface,
                title = { Text("Add Sticky Note Comment", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        placeholder = { Text("Type annotation or comment…") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = ObsidianSurfaceHighlight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newNoteText.isNotBlank()) {
                                stickyNotes.add(
                                    StickyNoteAnnotation(
                                        text = newNoteText,
                                        x = newNoteCoord!!.first,
                                        y = newNoteCoord!!.second,
                                        pageIndex = activePageIndex
                                    )
                                )
                                newNoteText = ""
                            }
                            newNoteCoord = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                    ) {
                        Text("Add Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { newNoteCoord = null }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }

        // View Sticky Note Dialog
        if (activeNoteToView != null) {
            AlertDialog(
                onDismissRequest = { activeNoteToView = null },
                containerColor = ObsidianSurface,
                title = { Text("Sticky Note Comment", color = Color.White) },
                text = {
                    Text(text = activeNoteToView!!.text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            stickyNotes.remove(activeNoteToView)
                            activeNoteToView = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonWarning)
                    ) {
                        Text("Delete Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeNoteToView = null }) {
                        Text("Close", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun PageRendererItem(
    engine: PdfRendererEngine,
    pageIndex: Int,
    invertColors: Boolean,
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
    var pageBitmap by remember(pageIndex, invertColors) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pageIndex, invertColors) {
        pageBitmap = engine.renderPageBitmap(pageIndex, targetWidth = 1080, invertColors = invertColors)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(4.dp))
            .background(if (invertColors) Color.Black else Color.White, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (pageBitmap != null) {
            Box {
                Image(
                    bitmap = pageBitmap!!.asImageBitmap(),
                    contentDescription = "PDF Page ${pageIndex + 1}",
                    modifier = Modifier.fillMaxWidth()
                )

                // Annotation Canvas Overlay layer
                AnnotationCanvasOverlay(
                    modifier = Modifier.matchParentSize(),
                    pageIndex = pageIndex,
                    activeTool = activeTool,
                    currentColor = currentColor,
                    currentStrokeWidth = currentStrokeWidth,
                    strokes = strokes,
                    shapes = shapes,
                    stamps = stamps,
                    stickyNotes = stickyNotes,
                    onStrokeAdded = onStrokeAdded,
                    onShapeAdded = onShapeAdded,
                    onStampAdded = onStampAdded,
                    onStickyNoteClicked = onStickyNoteClicked,
                    onAddStickyNote = onAddStickyNote
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonIndigo, modifier = Modifier.size(36.dp))
            }
        }
    }
}

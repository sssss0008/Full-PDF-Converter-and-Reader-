package com.example.ui.screens.organizer

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import com.example.engine.PdfBoxOperations
import com.example.engine.PdfRendererEngine
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
fun PageOrganizerScreen(
    documentId: String,
    database: AppDatabase,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var docTitle by remember { mutableStateOf("Page Manager") }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var engine by remember { mutableStateOf<PdfRendererEngine?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // List of page orders (0-indexed)
    val pageOrder = remember { mutableStateListOf<Int>() }
    val selectedPages = remember { mutableStateListOf<Int>() }

    // Dialog states
    var showWatermarkDialog by remember { mutableStateOf(false) }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }

    fun refreshEngine(file: File) {
        scope.launch {
            isLoading = true
            engine?.close()
            val newEngine = PdfRendererEngine(context, file)
            engine = newEngine
            pageCount = newEngine.pageCount
            pageOrder.clear()
            for (i in 0 until pageCount) pageOrder.add(i)
            selectedPages.clear()
            isLoading = false
        }
    }

    LaunchedEffect(documentId) {
        val entity = withContext(Dispatchers.IO) { database.documentDao().getDocumentById(documentId) }
        if (entity != null) {
            docTitle = entity.title
            val file = File(entity.filePath)
            if (file.exists()) {
                pdfFile = file
                refreshEngine(file)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { engine?.close() }
    }

    BackHandler { onNavigateBack() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(docTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("$pageCount pages • ${selectedPages.size} selected", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("organizer_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Select All / Deselect All
                    TextButton(onClick = {
                        if (selectedPages.size == pageCount) {
                            selectedPages.clear()
                        } else {
                            selectedPages.clear()
                            selectedPages.addAll(pageOrder)
                        }
                    }) {
                        Text(
                            text = if (selectedPages.size == pageCount) "Clear" else "Select All",
                            color = ElectricCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianSurface
                )
            )
        },
        bottomBar = {
            // Action Bar for selected pages
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = ObsidianSurface,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rotate
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                val file = pdfFile ?: return@IconButton
                                scope.launch {
                                    isLoading = true
                                    val tempOut = File(file.parentFile, "rotated_${System.currentTimeMillis()}.pdf")
                                    val targetIdx = if (selectedPages.size == 1) selectedPages.first() else null
                                    val res = PdfBoxOperations.rotatePages(file, tempOut, 90, targetIdx)
                                    if (res.isSuccess) {
                                        tempOut.renameTo(file)
                                        refreshEngine(file)
                                        snackbarHostState.showSnackbar("Rotated page(s) 90° clockwise")
                                    } else {
                                        isLoading = false
                                        snackbarHostState.showSnackbar("Failed to rotate page: ${res.exceptionOrNull()?.message}")
                                    }
                                }
                            },
                            modifier = Modifier.testTag("organizer_rotate_button")
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = Color.White)
                        }
                        Text("Rotate", color = Color.White, fontSize = 11.sp)
                    }

                    // Delete
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            enabled = selectedPages.isNotEmpty() && selectedPages.size < pageCount,
                            onClick = {
                                val file = pdfFile ?: return@IconButton
                                scope.launch {
                                    isLoading = true
                                    val tempOut = File(file.parentFile, "deleted_${System.currentTimeMillis()}.pdf")
                                    val res = PdfBoxOperations.deletePages(file, tempOut, selectedPages.toSet())
                                    if (res.isSuccess) {
                                        tempOut.renameTo(file)
                                        // Update database pageCount
                                        database.documentDao().insertOrUpdate(
                                            DocumentEntity(
                                                id = documentId,
                                                title = docTitle,
                                                filePath = file.absolutePath,
                                                pageCount = pageCount - selectedPages.size,
                                                fileSize = file.length(),
                                                lastOpenedTimestamp = System.currentTimeMillis()
                                            )
                                        )
                                        refreshEngine(file)
                                        snackbarHostState.showSnackbar("Deleted ${selectedPages.size} page(s)")
                                    } else {
                                        isLoading = false
                                        snackbarHostState.showSnackbar("Error deleting pages")
                                    }
                                }
                            },
                            modifier = Modifier.testTag("organizer_delete_button")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = if (selectedPages.isNotEmpty() && selectedPages.size < pageCount) CrimsonWarning else Color.Gray
                            )
                        }
                        Text("Delete", color = if (selectedPages.isNotEmpty()) CrimsonWarning else Color.Gray, fontSize = 11.sp)
                    }

                    // Duplicate Page
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            enabled = selectedPages.size == 1,
                            onClick = {
                                val file = pdfFile ?: return@IconButton
                                val pageToDup = selectedPages.first()
                                scope.launch {
                                    isLoading = true
                                    val tempOut = File(file.parentFile, "dup_${System.currentTimeMillis()}.pdf")
                                    val res = PdfBoxOperations.duplicatePage(file, tempOut, pageToDup)
                                    if (res.isSuccess) {
                                        tempOut.renameTo(file)
                                        refreshEngine(file)
                                        snackbarHostState.showSnackbar("Duplicated page ${pageToDup + 1}")
                                    } else {
                                        isLoading = false
                                        snackbarHostState.showSnackbar("Error duplicating page")
                                    }
                                }
                            },
                            modifier = Modifier.testTag("organizer_duplicate_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = if (selectedPages.size == 1) NeonIndigo else Color.Gray)
                        }
                        Text("Duplicate", color = if (selectedPages.size == 1) NeonIndigo else Color.Gray, fontSize = 11.sp)
                    }

                    // Extract Pages
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            enabled = selectedPages.isNotEmpty(),
                            onClick = {
                                val file = pdfFile ?: return@IconButton
                                scope.launch {
                                    val extractOut = File(file.parentFile, "${file.nameWithoutExtension}_extracted_${System.currentTimeMillis()}.pdf")
                                    val res = PdfBoxOperations.extractPages(file, extractOut, selectedPages.toList())
                                    if (res.isSuccess) {
                                        database.documentDao().insertOrUpdate(
                                            DocumentEntity(
                                                id = "extracted_${System.currentTimeMillis()}",
                                                title = "${docTitle} (Extracted)",
                                                filePath = extractOut.absolutePath,
                                                pageCount = selectedPages.size,
                                                fileSize = extractOut.length(),
                                                lastOpenedTimestamp = System.currentTimeMillis()
                                            )
                                        )
                                        snackbarHostState.showSnackbar("Extracted ${selectedPages.size} page(s) to new PDF!")
                                    } else {
                                        snackbarHostState.showSnackbar("Error extracting pages")
                                    }
                                }
                            },
                            modifier = Modifier.testTag("organizer_extract_button")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Extract", tint = if (selectedPages.isNotEmpty()) ElectricCyan else Color.Gray)
                        }
                        Text("Extract", color = if (selectedPages.isNotEmpty()) ElectricCyan else Color.Gray, fontSize = 11.sp)
                    }

                    // Watermark
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { showWatermarkDialog = true },
                            modifier = Modifier.testTag("organizer_watermark_button")
                        ) {
                            Icon(Icons.Default.WaterDrop, contentDescription = "Watermark", tint = Color.White)
                        }
                        Text("Watermark", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        },
        containerColor = ObsidianBackground
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            if (isLoading || engine == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonIndigo)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pageOrder.size) { index ->
                        val pageIdx = pageOrder[index]
                        val isSelected = selectedPages.contains(pageIdx)

                        PageThumbnailCard(
                            engine = engine!!,
                            pageIndex = pageIdx,
                            isSelected = isSelected,
                            onToggleSelect = {
                                if (isSelected) selectedPages.remove(pageIdx)
                                else selectedPages.add(pageIdx)
                            },
                            onMoveLeft = if (index > 0) {
                                {
                                    val item = pageOrder.removeAt(index)
                                    pageOrder.add(index - 1, item)
                                    // Save new order to file
                                    pdfFile?.let { file ->
                                        scope.launch {
                                            val tempOut = File(file.parentFile, "reorder_${System.currentTimeMillis()}.pdf")
                                            val res = PdfBoxOperations.reorderPages(file, tempOut, pageOrder.toList())
                                            if (res.isSuccess) {
                                                tempOut.renameTo(file)
                                                refreshEngine(file)
                                            }
                                        }
                                    }
                                }
                            } else null,
                            onMoveRight = if (index < pageOrder.size - 1) {
                                {
                                    val item = pageOrder.removeAt(index)
                                    pageOrder.add(index + 1, item)
                                    pdfFile?.let { file ->
                                        scope.launch {
                                            val tempOut = File(file.parentFile, "reorder_${System.currentTimeMillis()}.pdf")
                                            val res = PdfBoxOperations.reorderPages(file, tempOut, pageOrder.toList())
                                            if (res.isSuccess) {
                                                tempOut.renameTo(file)
                                                refreshEngine(file)
                                            }
                                        }
                                    }
                                }
                            } else null
                        )
                    }
                }
            }

            // Watermark Modal Dialog
            if (showWatermarkDialog) {
                AlertDialog(
                    onDismissRequest = { showWatermarkDialog = false },
                    containerColor = ObsidianSurface,
                    title = { Text("Apply Document Watermark", color = Color.White) },
                    text = {
                        Column {
                            Text("Add diagonal custom stamp watermark across pages:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = watermarkText,
                                onValueChange = { watermarkText = it },
                                label = { Text("Watermark Text") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonIndigo,
                                    unfocusedBorderColor = ObsidianSurfaceHighlight,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val file = pdfFile ?: return@Button
                                showWatermarkDialog = false
                                scope.launch {
                                    isLoading = true
                                    val tempOut = File(file.parentFile, "watermark_${System.currentTimeMillis()}.pdf")
                                    val res = PdfBoxOperations.applyWatermark(file, tempOut, watermarkText)
                                    if (res.isSuccess) {
                                        tempOut.renameTo(file)
                                        refreshEngine(file)
                                        snackbarHostState.showSnackbar("Watermark applied successfully!")
                                    } else {
                                        isLoading = false
                                        snackbarHostState.showSnackbar("Error applying watermark")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                        ) {
                            Text("Apply")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showWatermarkDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PageThumbnailCard(
    engine: PdfRendererEngine,
    pageIndex: Int,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onMoveLeft: (() -> Unit)?,
    onMoveRight: (() -> Unit)?
) {
    var thumbBitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pageIndex) {
        thumbBitmap = engine.renderThumbnail(pageIndex, size = 260)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggleSelect() }
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) NeonIndigo else ObsidianSurfaceHighlight,
                shape = RoundedCornerShape(12.dp)
            ),
        color = ObsidianSurface,
        tonalElevation = 4.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            // Top page badge & selection check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "p. ${pageIndex + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NeonIndigo else Color.Transparent)
                        .border(1.5.dp, if (isSelected) NeonIndigo else MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rendered Bitmap
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (thumbBitmap != null) {
                    Image(
                        bitmap = thumbBitmap!!.asImageBitmap(),
                        contentDescription = "Thumbnail ${pageIndex + 1}",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator(color = NeonIndigo, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reorder Shift Arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMoveLeft?.invoke() },
                    enabled = onMoveLeft != null,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Move Left",
                        tint = if (onMoveLeft != null) Color.White else Color.DarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { onMoveRight?.invoke() },
                    enabled = onMoveRight != null,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Move Right",
                        tint = if (onMoveRight != null) Color.White else Color.DarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

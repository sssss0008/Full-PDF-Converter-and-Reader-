package com.example.ui.screens.dashboard

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import com.example.engine.DocumentImportHelper
import com.example.engine.NativePdfCreator
import com.example.engine.PdfRendererEngine
import com.example.ui.components.FeatureTrustBanner
import com.example.ui.components.PulsingStatusBadge
import com.example.ui.components.interactiveHoverEffect
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHighlight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    database: AppDatabase,
    onOpenDrawer: () -> Unit,
    onOpenDocument: (String) -> Unit,
    onOpenScanner: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Recent", "Starred", "All Files", "Vault")

    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }
    var showCreateNoteDialog by remember { mutableStateOf(false) }

    // Collect Documents from Room
    val allDocs by database.documentDao().getPublicDocuments().collectAsState(initial = emptyList())
    val starredDocs by database.documentDao().getStarredDocuments().collectAsState(initial = emptyList())
    val vaultDocs by database.documentDao().getVaultDocuments().collectAsState(initial = emptyList())

    val displayedDocs = remember(selectedTab, searchQuery, allDocs, starredDocs, vaultDocs) {
        val baseList = when (selectedTab) {
            0 -> allDocs.sortedByDescending { it.lastOpenedTimestamp }
            1 -> starredDocs
            2 -> allDocs.sortedBy { it.title.lowercase() }
            3 -> vaultDocs
            else -> allDocs
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Storage Access Framework (SAF) document picker - single import
    val openPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val newDoc = DocumentImportHelper.importPdfFromUri(context, database, uri)
                withContext(Dispatchers.Main) {
                    if (newDoc != null) {
                        Toast.makeText(context, "Imported ${newDoc.title}", Toast.LENGTH_SHORT).show()
                        onOpenDocument(newDoc.id)
                    } else {
                        Toast.makeText(context, "Error reading PDF file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Storage Access Framework (SAF) document picker - batch multiple import
    val batchImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                var count = 0
                var firstDocId: String? = null
                uris.forEach { uri ->
                    val doc = DocumentImportHelper.importPdfFromUri(context, database, uri)
                    if (doc != null) {
                        count++
                        if (firstDocId == null) firstDocId = doc.id
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Imported $count PDF document(s)", Toast.LENGTH_SHORT).show()
                    if (count == 1 && firstDocId != null) {
                        onOpenDocument(firstDocId!!)
                    }
                }
            }
        }
    }

    var docToRename by remember { mutableStateOf<DocumentEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("PDF Reader & Editor", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonIndigo.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "LOCAL",
                                color = NeonIndigo,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("dashboard_menu_button")) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { batchImportLauncher.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.testTag("dashboard_batch_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DriveFolderUpload,
                            contentDescription = "Batch Upload Multiple PDFs",
                            tint = ElectricCyan
                        )
                    }
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openPdfLauncher.launch(arrayOf("application/pdf")) },
                containerColor = NeonIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .interactiveHoverEffect(
                        scaleOnHover = 1.09f,
                        scaleOnPress = 0.94f,
                        glowColor = ElectricCyan,
                        shape = CircleShape,
                        onClick = { openPdfLauncher.launch(arrayOf("application/pdf")) }
                    )
                    .testTag("dashboard_fab_import")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import PDF")
            }
        },
        containerColor = ObsidianBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search documents, keywords…", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianSurface,
                        unfocusedContainerColor = ObsidianSurface,
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = ObsidianSurfaceHighlight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Tabs Row
            androidx.compose.material3.TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ObsidianSurface,
                contentColor = NeonIndigo
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // Document List / Grid
            if (displayedDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonIndigo.copy(alpha = 0.15f),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = NeonIndigo,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching files" else "Workspace is Blank",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Try searching for a different keyword or document title."
                            } else {
                                "Zero default templates loaded. Upload your own documents or capture with camera below:"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // 4 Quick Start Action Cards
                        Column(
                            modifier = Modifier.fillMaxWidth(0.95f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EmptyStateActionCard(
                                title = "Open / Upload PDF",
                                subtitle = "Pick a PDF file from device storage or downloads",
                                icon = Icons.Default.FileOpen,
                                accentColor = NeonIndigo,
                                isPrimary = true,
                                onClick = { openPdfLauncher.launch(arrayOf("application/pdf")) }
                            )

                            EmptyStateActionCard(
                                title = "Batch Upload Multiple PDFs",
                                subtitle = "Import multiple files simultaneously with one tap",
                                icon = Icons.Default.DriveFolderUpload,
                                accentColor = ElectricCyan,
                                isPrimary = false,
                                onClick = { batchImportLauncher.launch(arrayOf("application/pdf")) }
                            )

                            EmptyStateActionCard(
                                title = "Scan Document with Camera",
                                subtitle = "Capture receipts, contracts, and notes into clean PDFs",
                                icon = Icons.Default.CameraAlt,
                                accentColor = EmeraldSuccess,
                                isPrimary = false,
                                onClick = onOpenScanner
                            )

                            EmptyStateActionCard(
                                title = "Create Blank PDF / Quick Note",
                                subtitle = "Author text notes or grid paper on-device",
                                icon = Icons.Default.NoteAdd,
                                accentColor = Color(0xFFFBBF24),
                                isPrimary = false,
                                onClick = { showCreateNoteDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        FeatureTrustBanner(modifier = Modifier.fillMaxWidth(0.95f))
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedDocs, key = { it.id }) { doc ->
                        DocumentGridCard(
                            doc = doc,
                            onClick = { onOpenDocument(doc.id) },
                            onToggleStar = {
                                scope.launch(Dispatchers.IO) {
                                    database.documentDao().setStarred(doc.id, !doc.isStarred)
                                }
                            },
                            onToggleVault = {
                                scope.launch(Dispatchers.IO) {
                                    database.documentDao().setVaultProtected(doc.id, !doc.isVaultProtected)
                                }
                            },
                            onRename = {
                                docToRename = doc
                                renameText = doc.title
                            },
                            onDuplicate = {
                                scope.launch(Dispatchers.IO) {
                                    val src = File(doc.filePath)
                                    val dest = File(src.parentFile, "${doc.title}_copy_${System.currentTimeMillis()}.pdf")
                                    src.copyTo(dest, overwrite = true)
                                    database.documentDao().insertOrUpdate(
                                        doc.copy(
                                            id = "doc_${System.currentTimeMillis()}",
                                            title = "${doc.title} (Copy)",
                                            filePath = dest.absolutePath,
                                            lastOpenedTimestamp = System.currentTimeMillis()
                                        )
                                    )
                                }
                            },
                            onDelete = {
                                scope.launch(Dispatchers.IO) {
                                    database.documentDao().deleteById(doc.id)
                                    File(doc.filePath).delete()
                                }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedDocs, key = { it.id }) { doc ->
                        DocumentListItem(
                            doc = doc,
                            onClick = { onOpenDocument(doc.id) },
                            onToggleStar = {
                                scope.launch(Dispatchers.IO) {
                                    database.documentDao().setStarred(doc.id, !doc.isStarred)
                                }
                            },
                            onToggleVault = {
                                scope.launch(Dispatchers.IO) {
                                    database.documentDao().setVaultProtected(doc.id, !doc.isVaultProtected)
                                }
                            },
                            onRename = {
                                docToRename = doc
                                renameText = doc.title
                            },
                            onDuplicate = {
                                scope.launch(Dispatchers.IO) {
                                    val src = File(doc.filePath)
                                    val dest = File(src.parentFile, "${doc.title}_copy_${System.currentTimeMillis()}.pdf")
                                    src.copyTo(dest, overwrite = true)
                                    database.documentDao().insertOrUpdate(
                                        doc.copy(
                                            id = "doc_${System.currentTimeMillis()}",
                                            title = "${doc.title} (Copy)",
                                            filePath = dest.absolutePath,
                                            lastOpenedTimestamp = System.currentTimeMillis()
                                        )
                                    )
                                }
                            },
                            onDelete = {
                                scope.launch(Dispatchers.IO) {
                                    database.documentDao().deleteById(doc.id)
                                    File(doc.filePath).delete()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Rename Document Dialog
        if (docToRename != null) {
            AlertDialog(
                onDismissRequest = { docToRename = null },
                containerColor = ObsidianSurface,
                title = { Text("Rename Document", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = { Text("Document Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = ObsidianSurfaceHighlight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = docToRename ?: return@Button
                            if (renameText.isNotBlank()) {
                                scope.launch(Dispatchers.IO) {
                                    database.documentDao().updateTitle(target.id, renameText.trim())
                                }
                            }
                            docToRename = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { docToRename = null }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }

        // Create Note Sheet Dialog
        if (showCreateNoteDialog) {
            CreateNoteDialog(
                database = database,
                onDismiss = { showCreateNoteDialog = false },
                onCreated = { docId ->
                    showCreateNoteDialog = false
                    onOpenDocument(docId)
                }
            )
        }
    }
}

@Composable
fun DocumentListItem(
    doc: DocumentEntity,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onToggleVault: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val formattedDate = remember(doc.lastOpenedTimestamp) {
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(doc.lastOpenedTimestamp))
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHoveredBySource by interactionSource.collectIsHoveredAsState()
    var isPointerHovered by remember { mutableStateOf(false) }
    val isHovered = isHoveredBySource || isPointerHovered

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter, PointerEventType.Move -> isPointerHovered = true
                            PointerEventType.Exit -> isPointerHovered = false
                        }
                    }
                }
            }
            .interactiveHoverEffect(
                scaleOnHover = 1.025f,
                scaleOnPress = 0.98f,
                glowColor = NeonIndigo,
                shape = RoundedCornerShape(14.dp),
                interactionSource = interactionSource,
                onClick = onClick
            )
            .testTag("doc_item_${doc.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isHovered) ObsidianSurfaceHighlight else ObsidianSurface,
        tonalElevation = if (isHovered) 6.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHovered) NeonIndigo.copy(alpha = 0.7f) else ObsidianSurfaceHighlight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonIndigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = NeonIndigo,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (doc.isVaultProtected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Shield, contentDescription = "Vault", tint = ElectricCyan, modifier = Modifier.size(15.dp))
                    }
                    if (doc.isPasswordProtected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Lock, contentDescription = "Protected", tint = CrimsonWarning, modifier = Modifier.size(15.dp))
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${doc.pageCount} pages • ${doc.fileSize / 1024} KB • $formattedDate",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onToggleStar) {
                Icon(
                    imageVector = if (doc.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star",
                    tint = if (doc.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(ObsidianSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename Document", color = Color.White) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate Document", color = Color.White) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (doc.isVaultProtected) "Remove from Safe" else "Move to Safe Vault", color = Color.White) },
                        onClick = {
                            menuExpanded = false
                            onToggleVault()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Document", color = CrimsonWarning) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentGridCard(
    doc: DocumentEntity,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onToggleVault: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isHoveredBySource by interactionSource.collectIsHoveredAsState()
    var isPointerHovered by remember { mutableStateOf(false) }
    val isHovered = isHoveredBySource || isPointerHovered

    val previewScale by animateFloatAsState(
        targetValue = if (isHovered) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "grid_preview_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter, PointerEventType.Move -> isPointerHovered = true
                            PointerEventType.Exit -> isPointerHovered = false
                        }
                    }
                }
            }
            .interactiveHoverEffect(
                scaleOnHover = 1.04f,
                scaleOnPress = 0.97f,
                glowColor = NeonIndigo,
                shape = RoundedCornerShape(14.dp),
                interactionSource = interactionSource,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (isHovered) ObsidianSurfaceHighlight else ObsidianSurface,
        tonalElevation = if (isHovered) 8.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHovered) NeonIndigo.copy(alpha = 0.8f) else ObsidianSurfaceHighlight
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = NeonIndigo,
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            scaleX = previewScale
                            scaleY = previewScale
                        }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(ObsidianSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename", color = Color.White) },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = Color.White) },
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (doc.isStarred) "Unstar" else "Star", color = Color.White) },
                            onClick = {
                                menuExpanded = false
                                onToggleStar()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (doc.isVaultProtected) "Remove Safe" else "Move Safe", color = Color.White) },
                            onClick = {
                                menuExpanded = false
                                onToggleVault()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = CrimsonWarning) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${doc.pageCount} pages • ${doc.fileSize / 1024} KB",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun EmptyStateActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHoveredBySource by interactionSource.collectIsHoveredAsState()
    var isPointerHovered by remember { mutableStateOf(false) }
    val isHovered = isHoveredBySource || isPointerHovered

    val iconScale by animateFloatAsState(
        targetValue = if (isHovered) 1.20f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "empty_icon_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter, PointerEventType.Move -> isPointerHovered = true
                            PointerEventType.Exit -> isPointerHovered = false
                        }
                    }
                }
            }
            .interactiveHoverEffect(
                scaleOnHover = 1.035f,
                scaleOnPress = 0.97f,
                glowColor = accentColor,
                shape = RoundedCornerShape(16.dp),
                interactionSource = interactionSource,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isPrimary) {
            if (isHovered) NeonIndigo.copy(alpha = 0.92f) else NeonIndigo
        } else {
            if (isHovered) ObsidianSurfaceHighlight else ObsidianSurface
        },
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isHovered) accentColor else if (isPrimary) NeonIndigo else accentColor.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isPrimary) Color.White.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) Color.White else accentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    color = if (isPrimary) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isPrimary) Color.White.copy(alpha = 0.9f) else accentColor,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        scaleX = if (isHovered) 1.25f else 1.0f
                    }
            )
        }
    }
}

@Composable
fun CreateNoteDialog(
    database: AppDatabase,
    onDismiss: () -> Unit,
    onCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("New Project Note") }
    var selectedStyle by remember { mutableStateOf("LINED") } // BLANK, LINED, GRID, DOTS
    val styles = listOf("LINED", "GRID", "DOTS", "BLANK")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = { Text("Create Note Sheet", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = ObsidianSurfaceHighlight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Paper Pattern:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    styles.forEach { style ->
                        val isSelected = selectedStyle == style
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedStyle = style },
                            color = if (isSelected) NeonIndigo else ObsidianSurfaceHighlight
                        ) {
                            Text(
                                text = style,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    scope.launch(Dispatchers.IO) {
                        val docsDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val safeName = title.replace(Regex("[^a-zA-Z0-9_]"), "_")
                        val outFile = File(docsDir, "${safeName}_${System.currentTimeMillis()}.pdf")
                        NativePdfCreator.createNoteSheet(title, selectedStyle, outFile)

                        val docId = "note_${System.currentTimeMillis()}"
                        database.documentDao().insertOrUpdate(
                            DocumentEntity(
                                id = docId,
                                title = title,
                                filePath = outFile.absolutePath,
                                pageCount = 1,
                                fileSize = outFile.length(),
                                lastOpenedTimestamp = System.currentTimeMillis()
                            )
                        )
                        withContext(Dispatchers.Main) {
                            onCreated(docId)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

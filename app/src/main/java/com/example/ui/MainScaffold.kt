package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesRepository
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.forms.FormsAndSignScreen
import com.example.ui.screens.organizer.PageOrganizerScreen
import com.example.ui.screens.reader.PdfViewerScreen
import com.example.ui.screens.safe.DocumentSafeScreen
import com.example.ui.screens.scanner.DocumentScannerScreen
import com.example.ui.screens.tools.ALL_CREATION_CAPTURE_TOOLS
import com.example.ui.screens.tools.ComparePdfDialog
import com.example.ui.screens.tools.CompressPdfDialog
import com.example.ui.screens.tools.CreationCaptureGridScreen
import com.example.ui.screens.tools.CropPdfDialog
import com.example.ui.screens.tools.EncryptPdfDialog
import com.example.ui.screens.tools.ExportFormatDialog
import com.example.ui.screens.tools.ExtendedToolId
import com.example.ui.screens.tools.HtmlToPdfDialog
import com.example.ui.screens.tools.MergeDocumentsDialog
import com.example.ui.screens.tools.MetadataInspectorDialog
import com.example.ui.screens.tools.OcrExtractDialog
import com.example.ui.screens.tools.PageNumbersDialog
import com.example.ui.screens.tools.PdfToImagesDialog
import com.example.ui.screens.tools.RepairPdfDialog
import com.example.ui.screens.tools.SplitPdfDialog
import com.example.ui.screens.tools.SummarizePdfDialog
import com.example.ui.screens.tools.TextToPdfDialog
import com.example.ui.screens.tools.ToolId
import com.example.ui.screens.tools.ToolsHubScreen
import com.example.ui.screens.tools.TranslatePdfDialog
import com.example.ui.screens.tools.UnlockPdfDialog
import com.example.ui.screens.tools.WatermarkPdfDialog
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

enum class MainDestination {
    FILES,
    READER,
    ORGANIZER,
    TOOLS,
    FORMS,
    SCANNER,
    SAFE,
    CREATION_GRID
}

@Composable
fun MainScaffold(
    database: AppDatabase,
    preferencesRepository: PreferencesRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentDestination by remember { mutableStateOf(MainDestination.FILES) }
    var activeDocumentId by remember { mutableStateOf<String?>(null) }

    // Dialog handlers for tools launched from Hub, Drawer, or Grid
    var activeToolDialog by remember { mutableStateOf<ToolId?>(null) }
    var activeExtendedTool by remember { mutableStateOf<ExtendedToolId?>(null) }

    val isDarkMode by preferencesRepository.isDarkMode.collectAsState(initial = true)

    fun handleExtendedTool(toolId: ExtendedToolId) {
        when (toolId) {
            ExtendedToolId.DOCUMENT_SCANNER, ExtendedToolId.SCAN_TO_PDF -> {
                currentDestination = MainDestination.SCANNER
            }
            ExtendedToolId.TEXT_TO_PDF -> {
                activeToolDialog = ToolId.TEXT_TO_PDF
            }
            ExtendedToolId.MERGE_PDF -> {
                activeToolDialog = ToolId.MERGE_PDFS
            }
            ExtendedToolId.SPLIT_PDF -> {
                activeExtendedTool = ExtendedToolId.SPLIT_PDF
            }
            ExtendedToolId.COMPRESS_PDF -> {
                activeExtendedTool = ExtendedToolId.COMPRESS_PDF
            }
            ExtendedToolId.PDF_TO_WORD -> {
                activeExtendedTool = ExtendedToolId.PDF_TO_WORD
            }
            ExtendedToolId.PDF_TO_POWERPOINT, ExtendedToolId.POWERPOINT_TO_PDF -> {
                activeExtendedTool = ExtendedToolId.PDF_TO_POWERPOINT
            }
            ExtendedToolId.PDF_TO_EXCEL, ExtendedToolId.EXCEL_TO_PDF -> {
                activeExtendedTool = ExtendedToolId.PDF_TO_EXCEL
            }
            ExtendedToolId.WORD_TO_PDF -> {
                activeToolDialog = ToolId.TEXT_TO_PDF
            }
            ExtendedToolId.EDIT_PDF, ExtendedToolId.ORGANIZE_PDF, ExtendedToolId.ROTATE_PDF -> {
                currentDestination = MainDestination.ORGANIZER
            }
            ExtendedToolId.CROP_PDF -> {
                activeExtendedTool = ExtendedToolId.CROP_PDF
            }
            ExtendedToolId.PAGE_NUMBERS -> {
                activeExtendedTool = ExtendedToolId.PAGE_NUMBERS
            }
            ExtendedToolId.PDF_TO_JPG -> {
                activeExtendedTool = ExtendedToolId.PDF_TO_JPG
            }
            ExtendedToolId.JPG_TO_PDF -> {
                currentDestination = MainDestination.SCANNER
            }
            ExtendedToolId.SIGN_PDF, ExtendedToolId.PDF_FORMS -> {
                currentDestination = MainDestination.FORMS
            }
            ExtendedToolId.WATERMARK -> {
                activeExtendedTool = ExtendedToolId.WATERMARK
            }
            ExtendedToolId.UNLOCK_PDF -> {
                activeExtendedTool = ExtendedToolId.UNLOCK_PDF
            }
            ExtendedToolId.PROTECT_PDF -> {
                activeToolDialog = ToolId.PASSWORD_ENCRYPT
            }
            ExtendedToolId.PDF_TO_PDFA -> {
                activeExtendedTool = ExtendedToolId.PDF_TO_PDFA
            }
            ExtendedToolId.REPAIR_PDF -> {
                activeExtendedTool = ExtendedToolId.REPAIR_PDF
            }
            ExtendedToolId.OCR_PDF -> {
                activeToolDialog = ToolId.ON_DEVICE_OCR
            }
            ExtendedToolId.COMPARE_PDF -> {
                activeExtendedTool = ExtendedToolId.COMPARE_PDF
            }
            ExtendedToolId.REDACT_PDF -> {
                Toast.makeText(context, "Open document reader and use Blackout Pen to redact", Toast.LENGTH_LONG).show()
                currentDestination = MainDestination.FILES
            }
            ExtendedToolId.AI_SUMMARIZER -> {
                activeExtendedTool = ExtendedToolId.AI_SUMMARIZER
            }
            ExtendedToolId.TRANSLATE_PDF -> {
                activeExtendedTool = ExtendedToolId.TRANSLATE_PDF
            }
            ExtendedToolId.PDF_TO_MARKDOWN -> {
                activeExtendedTool = ExtendedToolId.PDF_TO_MARKDOWN
            }
            ExtendedToolId.HTML_TO_PDF -> {
                activeExtendedTool = ExtendedToolId.HTML_TO_PDF
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = ObsidianSurface,
                modifier = Modifier
                    .width(330.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Drawer Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ObsidianSurfaceHighlight,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, NeonIndigo.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    color = NeonIndigo.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("PDF Reader and Editor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Enterprise Edition", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("100% Offline • Zero Cloud Cost", color = EmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 1: CREATION & CAPTURE - Interactive Grid Card Header
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .interactiveHoverEffect(
                                scaleOnHover = 1.03f,
                                scaleOnPress = 0.97f,
                                glowColor = NeonIndigo,
                                shape = RoundedCornerShape(14.dp),
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    currentDestination = MainDestination.CREATION_GRID
                                }
                            )
                            .testTag("drawer_creation_capture_header"),
                        shape = RoundedCornerShape(14.dp),
                        color = NeonIndigo.copy(alpha = 0.18f),
                        border = BorderStroke(1.2.dp, NeonIndigo)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.GridView, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "CREATION & CAPTURE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    "Open 30+ Tools Grid View →",
                                    color = ElectricCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // In-Drawer 2-Column Grid of Tools with Unique Icons
                    Text(
                        text = "QUICK TOOLS GRID",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    // 2-Column Grid inside the drawer for immediate one-tap access
                    val quickDrawerTools = remember {
                        listOf(
                            Triple("Scanner", Icons.Default.CameraAlt, ExtendedToolId.DOCUMENT_SCANNER),
                            Triple("Text to PDF", Icons.Default.TextSnippet, ExtendedToolId.TEXT_TO_PDF),
                            Triple("Merge PDF", Icons.Default.MergeType, ExtendedToolId.MERGE_PDF),
                            Triple("Split PDF", Icons.Default.CallSplit, ExtendedToolId.SPLIT_PDF),
                            Triple("Compress", Icons.Default.Compress, ExtendedToolId.COMPRESS_PDF),
                            Triple("PDF to Word", Icons.Default.Article, ExtendedToolId.PDF_TO_WORD),
                            Triple("JPG to PDF", Icons.Default.AddPhotoAlternate, ExtendedToolId.JPG_TO_PDF),
                            Triple("Sign PDF", Icons.Default.Draw, ExtendedToolId.SIGN_PDF),
                            Triple("Watermark", Icons.Default.WaterDrop, ExtendedToolId.WATERMARK),
                            Triple("OCR PDF", Icons.Default.FindInPage, ExtendedToolId.OCR_PDF),
                            Triple("AI Summary", Icons.Default.AutoAwesome, ExtendedToolId.AI_SUMMARIZER),
                            Triple("Translate", Icons.Default.Translate, ExtendedToolId.TRANSLATE_PDF)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in quickDrawerTools.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val item1 = quickDrawerTools[i]
                                DrawerToolTile(
                                    title = item1.first,
                                    icon = item1.second,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        handleExtendedTool(item1.third)
                                    }
                                )

                                if (i + 1 < quickDrawerTools.size) {
                                    val item2 = quickDrawerTools[i + 1]
                                    DrawerToolTile(
                                        title = item2.first,
                                        icon = item2.second,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            handleExtendedTool(item2.third)
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Expand / View All 30+ Tools Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .interactiveHoverEffect(
                                scaleOnHover = 1.03f,
                                scaleOnPress = 0.97f,
                                glowColor = NeonIndigo,
                                shape = RoundedCornerShape(10.dp),
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    currentDestination = MainDestination.CREATION_GRID
                                }
                            )
                            .testTag("drawer_view_all_grid_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = ObsidianSurfaceHighlight,
                        border = BorderStroke(1.dp, NeonIndigo.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "View All 30+ Tools Grid",
                                color = NeonIndigo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section 2: Security & Vault
                    DrawerSectionHeader("SECURITY & VAULT")
                    DrawerItem(
                        icon = Icons.Default.Shield,
                        label = "Encrypted Safe Vault",
                        onClick = {
                            scope.launch { drawerState.close() }
                            currentDestination = MainDestination.SAFE
                        }
                    )
                    DrawerItem(
                        icon = Icons.Default.Lock,
                        label = "Password Encrypt (AES)",
                        onClick = {
                            scope.launch { drawerState.close() }
                            activeToolDialog = ToolId.PASSWORD_ENCRYPT
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section 3: Utilities & Settings
                    DrawerSectionHeader("INTELLIGENCE & SETTINGS")
                    DrawerItem(
                        icon = Icons.Default.DocumentScanner,
                        label = "On-Device ML OCR",
                        onClick = {
                            scope.launch { drawerState.close() }
                            activeToolDialog = ToolId.ON_DEVICE_OCR
                        }
                    )
                    DrawerItem(
                        icon = Icons.Default.Info,
                        label = "Metadata Inspector",
                        onClick = {
                            scope.launch { drawerState.close() }
                            activeToolDialog = ToolId.METADATA_EDITOR
                        }
                    )
                    DrawerItem(
                        icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        label = if (isDarkMode) "Clean Light Theme" else "Obsidian Dark Theme",
                        onClick = {
                            scope.launch {
                                preferencesRepository.setDarkMode(!isDarkMode)
                                drawerState.close()
                            }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Default.DeleteSweep,
                        label = "Clear Render Cache",
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                context.cacheDir.deleteRecursively()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Bitmap cache cleared", Toast.LENGTH_SHORT).show()
                                    drawerState.close()
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                // Show Bottom Bar only for main root destinations (not inside reader, scanner, or full grid)
                if (currentDestination != MainDestination.READER && currentDestination != MainDestination.SCANNER) {
                    NavigationBar(
                        containerColor = ObsidianSurface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentDestination == MainDestination.FILES,
                            onClick = { currentDestination = MainDestination.FILES },
                            icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
                            label = { Text("Files", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = NeonIndigo,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_files")
                        )

                        NavigationBarItem(
                            selected = currentDestination == MainDestination.READER,
                            onClick = {
                                if (activeDocumentId != null) {
                                    currentDestination = MainDestination.READER
                                } else {
                                    Toast.makeText(context, "Open a document from Files first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            icon = { Icon(Icons.Default.Description, contentDescription = "Reader") },
                            label = { Text("Reader", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = NeonIndigo,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_reader")
                        )

                        NavigationBarItem(
                            selected = currentDestination == MainDestination.ORGANIZER,
                            onClick = {
                                if (activeDocumentId != null) {
                                    currentDestination = MainDestination.ORGANIZER
                                } else {
                                    Toast.makeText(context, "Open a document from Files first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            icon = { Icon(Icons.Default.Layers, contentDescription = "Organize") },
                            label = { Text("Organize", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = NeonIndigo,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_organizer")
                        )

                        NavigationBarItem(
                            selected = currentDestination == MainDestination.TOOLS || currentDestination == MainDestination.CREATION_GRID,
                            onClick = { currentDestination = MainDestination.CREATION_GRID },
                            icon = { Icon(Icons.Default.GridView, contentDescription = "Tools") },
                            label = { Text("Tools", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = NeonIndigo,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_tools")
                        )

                        NavigationBarItem(
                            selected = currentDestination == MainDestination.FORMS,
                            onClick = { currentDestination = MainDestination.FORMS },
                            icon = { Icon(Icons.Default.HistoryEdu, contentDescription = "Sign & Forms") },
                            label = { Text("Sign", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = NeonIndigo,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_forms")
                        )
                    }
                }
            },
            containerColor = ObsidianBackground
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentDestination) {
                    MainDestination.FILES -> {
                        DashboardScreen(
                            database = database,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onOpenDocument = { docId ->
                                activeDocumentId = docId
                                currentDestination = MainDestination.READER
                            },
                            onOpenScanner = { currentDestination = MainDestination.SCANNER }
                        )
                    }

                    MainDestination.READER -> {
                        val docId = activeDocumentId
                        if (docId != null) {
                            PdfViewerScreen(
                                documentId = docId,
                                database = database,
                                onNavigateBack = { currentDestination = MainDestination.FILES },
                                onNavigateToOrganizer = { id ->
                                    activeDocumentId = id
                                    currentDestination = MainDestination.ORGANIZER
                                }
                            )
                        } else {
                            currentDestination = MainDestination.FILES
                        }
                    }

                    MainDestination.ORGANIZER -> {
                        val docId = activeDocumentId
                        if (docId != null) {
                            PageOrganizerScreen(
                                documentId = docId,
                                database = database,
                                onNavigateBack = { currentDestination = MainDestination.READER }
                            )
                        } else {
                            currentDestination = MainDestination.FILES
                        }
                    }

                    MainDestination.TOOLS -> {
                        ToolsHubScreen(
                            database = database,
                            onOpenTool = { toolId ->
                                when (toolId) {
                                    ToolId.DOCUMENT_SCANNER -> currentDestination = MainDestination.SCANNER
                                    ToolId.TEXT_TO_PDF -> activeToolDialog = ToolId.TEXT_TO_PDF
                                    ToolId.MERGE_PDFS -> activeToolDialog = ToolId.MERGE_PDFS
                                    ToolId.ON_DEVICE_OCR -> activeToolDialog = ToolId.ON_DEVICE_OCR
                                    ToolId.PASSWORD_ENCRYPT -> activeToolDialog = ToolId.PASSWORD_ENCRYPT
                                    ToolId.METADATA_EDITOR -> activeToolDialog = ToolId.METADATA_EDITOR
                                    else -> activeToolDialog = toolId
                                }
                            }
                        )
                    }

                    MainDestination.CREATION_GRID -> {
                        CreationCaptureGridScreen(
                            onNavigateBack = { currentDestination = MainDestination.FILES },
                            onToolSelected = { toolId ->
                                handleExtendedTool(toolId)
                            }
                        )
                    }

                    MainDestination.FORMS -> {
                        FormsAndSignScreen(
                            database = database,
                            preferencesRepository = preferencesRepository
                        )
                    }

                    MainDestination.SCANNER -> {
                        DocumentScannerScreen(
                            database = database,
                            onNavigateBack = { currentDestination = MainDestination.FILES },
                            onDocumentCreated = { docId ->
                                activeDocumentId = docId
                                currentDestination = MainDestination.READER
                            }
                        )
                    }

                    MainDestination.SAFE -> {
                        DocumentSafeScreen(
                            database = database,
                            preferencesRepository = preferencesRepository,
                            onOpenDocument = { docId ->
                                activeDocumentId = docId
                                currentDestination = MainDestination.READER
                            }
                        )
                    }
                }

                // Interactive Dialogs for Core Tools
                when (activeToolDialog) {
                    ToolId.TEXT_TO_PDF -> {
                        TextToPdfDialog(
                            database = database,
                            onDismiss = { activeToolDialog = null },
                            onSuccess = { activeToolDialog = null }
                        )
                    }
                    ToolId.MERGE_PDFS -> {
                        MergeDocumentsDialog(
                            database = database,
                            onDismiss = { activeToolDialog = null },
                            onSuccess = { activeToolDialog = null }
                        )
                    }
                    ToolId.ON_DEVICE_OCR -> {
                        OcrExtractDialog(
                            database = database,
                            onDismiss = { activeToolDialog = null }
                        )
                    }
                    ToolId.PASSWORD_ENCRYPT -> {
                        EncryptPdfDialog(
                            database = database,
                            onDismiss = { activeToolDialog = null },
                            onSuccess = { activeToolDialog = null }
                        )
                    }
                    ToolId.METADATA_EDITOR -> {
                        MetadataInspectorDialog(
                            database = database,
                            onDismiss = { activeToolDialog = null }
                        )
                    }
                    else -> Unit
                }

                // Interactive Dialogs for Extended Grid Tools
                when (activeExtendedTool) {
                    ExtendedToolId.SPLIT_PDF -> {
                        SplitPdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null },
                            onSuccess = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.AI_SUMMARIZER -> {
                        SummarizePdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.TRANSLATE_PDF -> {
                        TranslatePdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.COMPARE_PDF -> {
                        ComparePdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.PDF_TO_WORD -> {
                        ExportFormatDialog(
                            formatTitle = "Word (.docx)",
                            extension = "docx",
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.PDF_TO_POWERPOINT -> {
                        ExportFormatDialog(
                            formatTitle = "PowerPoint (.pptx)",
                            extension = "pptx",
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.PDF_TO_EXCEL -> {
                        ExportFormatDialog(
                            formatTitle = "Excel (.xlsx)",
                            extension = "xlsx",
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.PDF_TO_MARKDOWN -> {
                        ExportFormatDialog(
                            formatTitle = "Markdown (.md)",
                            extension = "md",
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.PDF_TO_PDFA -> {
                        ExportFormatDialog(
                            formatTitle = "PDF/A Standard Archive",
                            extension = "pdfa",
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.HTML_TO_PDF -> {
                        HtmlToPdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null },
                            onSuccess = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.COMPRESS_PDF -> {
                        CompressPdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null },
                            onSuccess = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.WATERMARK -> {
                        WatermarkPdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null },
                            onSuccess = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.PAGE_NUMBERS -> {
                        PageNumbersDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null },
                            onSuccess = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.UNLOCK_PDF -> {
                        UnlockPdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null },
                            onSuccess = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.REPAIR_PDF -> {
                        RepairPdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null },
                            onSuccess = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.CROP_PDF -> {
                        CropPdfDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null },
                            onSuccess = { activeExtendedTool = null }
                        )
                    }
                    ExtendedToolId.PDF_TO_JPG -> {
                        PdfToImagesDialog(
                            database = database,
                            onDismiss = { activeExtendedTool = null }
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun DrawerToolTile(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .interactiveHoverEffect(
                scaleOnHover = 1.05f,
                scaleOnPress = 0.95f,
                glowColor = ElectricCyan,
                shape = RoundedCornerShape(10.dp),
                onClick = onClick
            ),
        shape = RoundedCornerShape(10.dp),
        color = ObsidianSurfaceHighlight,
        border = BorderStroke(1.dp, Color(0xFF262D3D))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 6.dp)
    )
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .interactiveHoverEffect(
                scaleOnHover = 1.03f,
                scaleOnPress = 0.97f,
                glowColor = NeonIndigo,
                shape = RoundedCornerShape(12.dp),
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

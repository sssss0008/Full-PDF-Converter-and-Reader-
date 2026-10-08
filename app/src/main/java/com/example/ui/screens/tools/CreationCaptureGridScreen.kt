package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PulsingStatusBadge
import com.example.ui.components.interactiveHoverEffect
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHighlight

enum class ExtendedToolId {
    DOCUMENT_SCANNER,
    TEXT_TO_PDF,
    MERGE_PDF,
    SPLIT_PDF,
    COMPRESS_PDF,
    PDF_TO_WORD,
    PDF_TO_POWERPOINT,
    PDF_TO_EXCEL,
    WORD_TO_PDF,
    POWERPOINT_TO_PDF,
    EXCEL_TO_PDF,
    EDIT_PDF,
    PDF_TO_JPG,
    JPG_TO_PDF,
    SIGN_PDF,
    WATERMARK,
    ROTATE_PDF,
    HTML_TO_PDF,
    UNLOCK_PDF,
    PROTECT_PDF,
    ORGANIZE_PDF,
    PDF_TO_PDFA,
    REPAIR_PDF,
    PAGE_NUMBERS,
    SCAN_TO_PDF,
    OCR_PDF,
    COMPARE_PDF,
    REDACT_PDF,
    CROP_PDF,
    PDF_FORMS,
    AI_SUMMARIZER,
    TRANSLATE_PDF,
    PDF_TO_MARKDOWN
}

data class ExtendedToolItem(
    val id: ExtendedToolId,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val categoryBadge: String
)

val ALL_CREATION_CAPTURE_TOOLS = listOf(
    // 1. Existing core tools
    ExtendedToolItem(
        id = ExtendedToolId.DOCUMENT_SCANNER,
        title = "Document Scanner",
        description = "Capture document scans from your mobile device and enhance them into clean PDFs.",
        icon = Icons.Default.CameraAlt,
        color = ElectricCyan,
        categoryBadge = "CAPTURE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.TEXT_TO_PDF,
        title = "Text to PDF",
        description = "Create formatted documents, note sheets, grid paper, and meeting minutes on-device.",
        icon = Icons.Default.TextSnippet,
        color = EmeraldSuccess,
        categoryBadge = "CREATION"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.MERGE_PDF,
        title = "Merge PDF",
        description = "Combine PDFs in the order you want with the easiest PDF merger available.",
        icon = Icons.Default.MergeType,
        color = NeonIndigo,
        categoryBadge = "ORGANIZE"
    ),
    // 2. Full requested suite
    ExtendedToolItem(
        id = ExtendedToolId.SPLIT_PDF,
        title = "Split PDF",
        description = "Separate one page or a whole set for easy conversion into independent PDF files.",
        icon = Icons.Default.CallSplit,
        color = Color(0xFF38BDF8),
        categoryBadge = "ORGANIZE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.COMPRESS_PDF,
        title = "Compress PDF",
        description = "Reduce file size while optimizing for maximal PDF quality.",
        icon = Icons.Default.Compress,
        color = EmeraldSuccess,
        categoryBadge = "OPTIMIZE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PDF_TO_WORD,
        title = "PDF to Word",
        description = "Easily convert your PDF files into easy to edit DOC and DOCX documents.",
        icon = Icons.Default.Article,
        color = Color(0xFF2563EB),
        categoryBadge = "CONVERT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PDF_TO_POWERPOINT,
        title = "PDF to PowerPoint",
        description = "Turn your PDF files into easy to edit PPT and PPTX slideshows.",
        icon = Icons.Default.Slideshow,
        color = Color(0xFFEA580C),
        categoryBadge = "CONVERT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PDF_TO_EXCEL,
        title = "PDF to Excel",
        description = "Pull data straight from PDFs into Excel spreadsheets in a few short seconds.",
        icon = Icons.Default.TableChart,
        color = Color(0xFF16A34A),
        categoryBadge = "CONVERT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.WORD_TO_PDF,
        title = "Word to PDF",
        description = "Make DOC and DOCX files easy to read by converting them to PDF.",
        icon = Icons.Default.Description,
        color = Color(0xFF3B82F6),
        categoryBadge = "CONVERT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.POWERPOINT_TO_PDF,
        title = "PowerPoint to PDF",
        description = "Make PPT and PPTX slideshows easy to view by converting them to PDF.",
        icon = Icons.Default.PresentToAll,
        color = Color(0xFFF97316),
        categoryBadge = "CONVERT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.EXCEL_TO_PDF,
        title = "Excel to PDF",
        description = "Make EXCEL spreadsheets easy to read by converting them to PDF.",
        icon = Icons.Default.GridOn,
        color = Color(0xFF22C55E),
        categoryBadge = "CONVERT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.EDIT_PDF,
        title = "Edit PDF",
        description = "Add text, images, shapes or freehand annotations to a PDF document.",
        icon = Icons.Default.Edit,
        color = NeonIndigo,
        categoryBadge = "EDIT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PDF_TO_JPG,
        title = "PDF to JPG",
        description = "Convert each PDF page into a JPG or extract all images contained in a PDF.",
        icon = Icons.Default.Image,
        color = Color(0xFFEAB308),
        categoryBadge = "CONVERT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.JPG_TO_PDF,
        title = "JPG to PDF",
        description = "Convert JPG images to PDF in seconds. Easily adjust orientation and margins.",
        icon = Icons.Default.AddPhotoAlternate,
        color = Color(0xFFA855F7),
        categoryBadge = "CREATION"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.SIGN_PDF,
        title = "Sign PDF",
        description = "Sign yourself or request electronic signatures from others.",
        icon = Icons.Default.Draw,
        color = ElectricCyan,
        categoryBadge = "FORMS"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.WATERMARK,
        title = "Watermark",
        description = "Stamp an image or text over your PDF in seconds. Choose position and opacity.",
        icon = Icons.Default.WaterDrop,
        color = Color(0xFF818CF8),
        categoryBadge = "SECURITY"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.ROTATE_PDF,
        title = "Rotate PDF",
        description = "Rotate your PDFs the way you need them. Rotate single or multiple pages at once.",
        icon = Icons.Default.RotateRight,
        color = Color(0xFF6366F1),
        categoryBadge = "ORGANIZE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.HTML_TO_PDF,
        title = "HTML to PDF",
        description = "Convert webpages and raw HTML into vector PDF documents.",
        icon = Icons.Default.Language,
        color = Color(0xFF06B6D4),
        categoryBadge = "CAPTURE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.UNLOCK_PDF,
        title = "Unlock PDF",
        description = "Remove PDF password security, giving you the freedom to use your PDFs.",
        icon = Icons.Default.LockOpen,
        color = EmeraldSuccess,
        categoryBadge = "SECURITY"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PROTECT_PDF,
        title = "Protect PDF",
        description = "Protect PDF files with a password. Encrypt PDF documents with AES encryption.",
        icon = Icons.Default.Lock,
        color = CrimsonWarning,
        categoryBadge = "SECURITY"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.ORGANIZE_PDF,
        title = "Organize PDF",
        description = "Sort pages of your PDF file however you like. Delete, duplicate, or reorder pages.",
        icon = Icons.Default.Layers,
        color = NeonIndigo,
        categoryBadge = "ORGANIZE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PDF_TO_PDFA,
        title = "PDF to PDF/A",
        description = "Transform your PDF to PDF/A, the ISO-standardized version for long-term archiving.",
        icon = Icons.Default.Archive,
        color = Color(0xFF64748B),
        categoryBadge = "ARCHIVE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.REPAIR_PDF,
        title = "Repair PDF",
        description = "Repair damaged PDFs and recover corrupted text and media streams.",
        icon = Icons.Default.Build,
        color = Color(0xFFF59E0B),
        categoryBadge = "REPAIR"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PAGE_NUMBERS,
        title = "Page numbers",
        description = "Add page numbers into PDFs with ease. Choose positions, dimensions, typography.",
        icon = Icons.Default.FormatListNumbered,
        color = Color(0xFF14B8A6),
        categoryBadge = "EDIT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.SCAN_TO_PDF,
        title = "Scan to PDF",
        description = "Capture document scans from your mobile device camera instantly.",
        icon = Icons.Default.DocumentScanner,
        color = ElectricCyan,
        categoryBadge = "CAPTURE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.OCR_PDF,
        title = "OCR PDF",
        description = "Easily convert scanned PDFs into searchable and selectable documents using on-device ML.",
        icon = Icons.Default.FindInPage,
        color = Color(0xFF38BDF8),
        categoryBadge = "INTELLIGENCE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.COMPARE_PDF,
        title = "Compare PDF",
        description = "Show a side-by-side document comparison and easily spot differences between versions.",
        icon = Icons.Default.Compare,
        color = Color(0xFFA855F7),
        categoryBadge = "INTELLIGENCE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.REDACT_PDF,
        title = "Redact PDF",
        description = "Redact text and graphics to permanently remove sensitive information and PII.",
        icon = Icons.Default.VisibilityOff,
        color = CrimsonWarning,
        categoryBadge = "SECURITY"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.CROP_PDF,
        title = "Crop PDF",
        description = "Crop margins of PDF documents or select specific areas to trim white borders.",
        icon = Icons.Default.Crop,
        color = Color(0xFFEC4899),
        categoryBadge = "EDIT"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PDF_FORMS,
        title = "PDF Forms",
        description = "Detect form fields, create interactive fillable PDFs, and auto-populate text inputs.",
        icon = Icons.Default.Assignment,
        color = NeonIndigo,
        categoryBadge = "FORMS"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.AI_SUMMARIZER,
        title = "AI Summarizer",
        description = "Quickly generate concise summaries, extracts, and key takeaways from documents on-device.",
        icon = Icons.Default.AutoAwesome,
        color = Color(0xFFFBBF24),
        categoryBadge = "INTELLIGENCE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.TRANSLATE_PDF,
        title = "Translate PDF",
        description = "Translate PDF text content while keeping fonts, layout, and document structure intact.",
        icon = Icons.Default.Translate,
        color = Color(0xFF06B6D4),
        categoryBadge = "INTELLIGENCE"
    ),
    ExtendedToolItem(
        id = ExtendedToolId.PDF_TO_MARKDOWN,
        title = "PDF to Markdown",
        description = "Easily turn PDFs into Markdown files with preserved headings, lists, tables, and links.",
        icon = Icons.Default.Code,
        color = EmeraldSuccess,
        categoryBadge = "CONVERT"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationCaptureGridScreen(
    onNavigateBack: () -> Unit,
    onToolSelected: (ExtendedToolId) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterCategories = listOf("ALL", "CAPTURE", "CREATION", "CONVERT", "ORGANIZE", "EDIT", "SECURITY", "INTELLIGENCE")

    val filteredTools = remember(searchQuery, selectedFilter) {
        ALL_CREATION_CAPTURE_TOOLS.filter { tool ->
            val matchesSearch = searchQuery.isBlank() ||
                    tool.title.contains(searchQuery, ignoreCase = true) ||
                    tool.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedFilter == "ALL" || tool.categoryBadge.equals(selectedFilter, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Creation & Capture Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${ALL_CREATION_CAPTURE_TOOLS.size} Professional Tools • 100% Offline",
                            color = ElectricCyan,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("creation_grid_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSurface)
            )
        },
        containerColor = ObsidianBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Stats / Trust Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROFESSIONAL WORKSTATION",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp
                )
                PulsingStatusBadge(
                    text = "${filteredTools.size} Tools Active",
                    color = EmeraldSuccess
                )
            }

            // Search Bar with Hover Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 30+ PDF tools…", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("creation_grid_search_input"),
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

            // Quick Filter Chips Row with Interactive Hover Effects
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterCategories.size) { idx ->
                    val cat = filterCategories[idx]
                    val isSelected = selectedFilter == cat
                    Surface(
                        modifier = Modifier
                            .interactiveHoverEffect(
                                scaleOnHover = 1.05f,
                                scaleOnPress = 0.95f,
                                glowColor = NeonIndigo,
                                shape = RoundedCornerShape(10.dp),
                                onClick = { selectedFilter = cat }
                            ),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) NeonIndigo else ObsidianSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeonIndigo else ObsidianSurfaceHighlight
                        )
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2-Column Responsive Grid View with Unique Icons for Every Tool
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 155.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTools, key = { it.id.name }) { tool ->
                    ToolGridCard(
                        tool = tool,
                        onClick = { onToolSelected(tool.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ToolGridCard(
    tool: ExtendedToolItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHoveredBySource by interactionSource.collectIsHoveredAsState()
    var isPointerHovered by remember { mutableStateOf(false) }
    val isHovered = isHoveredBySource || isPointerHovered

    val iconScale by animateFloatAsState(
        targetValue = if (isHovered) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "icon_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
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
                glowColor = tool.color,
                shape = RoundedCornerShape(16.dp),
                interactionSource = interactionSource,
                onClick = onClick
            )
            .testTag("tool_grid_item_${tool.id.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isHovered) ObsidianSurfaceHighlight else ObsidianSurface,
        tonalElevation = if (isHovered) 8.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHovered) tool.color.copy(alpha = 0.8f) else ObsidianSurfaceHighlight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Unique Icon + Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tool.color.copy(alpha = if (isHovered) 0.25f else 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = tool.color,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tool.color.copy(alpha = if (isHovered) 0.22f else 0.12f)
                ) {
                    Text(
                        text = tool.categoryBadge,
                        color = tool.color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }

            // Body: Title & Description
            Column {
                Text(
                    text = tool.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.description,
                    color = if (isHovered) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Micro-Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = isHovered) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Launch",
                            color = tool.color,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = tool.color,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

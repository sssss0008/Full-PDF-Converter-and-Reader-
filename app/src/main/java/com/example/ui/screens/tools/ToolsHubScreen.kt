package com.example.ui.screens.tools

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHighlight

enum class ToolId {
    IMAGES_TO_PDF,
    TEXT_TO_PDF,
    DOCUMENT_SCANNER,
    PDF_TO_IMAGES,
    MERGE_PDFS,
    SPLIT_PDF,
    ON_DEVICE_OCR,
    PASSWORD_ENCRYPT,
    PASSWORD_DECRYPT,
    WATERMARK,
    METADATA_EDITOR,
    COMPRESS_PDF
}

data class ToolItem(
    val id: ToolId,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    database: AppDatabase,
    onOpenTool: (ToolId) -> Unit
) {
    val toolsList = remember {
        listOf(
            // Category: Creation & Conversion
            ToolItem(
                id = ToolId.IMAGES_TO_PDF,
                title = "Images to PDF",
                description = "Convert gallery photos into standardized A4 documents",
                icon = Icons.Default.Image,
                accentColor = NeonIndigo,
                category = "Creation & Conversion"
            ),
            ToolItem(
                id = ToolId.DOCUMENT_SCANNER,
                title = "Camera Scanner",
                description = "Capture documents with perspective filtering & B&W enhancement",
                icon = Icons.Default.CameraAlt,
                accentColor = ElectricCyan,
                category = "Creation & Conversion"
            ),
            ToolItem(
                id = ToolId.TEXT_TO_PDF,
                title = "Text & Notes to PDF",
                description = "Generate formatted documents, grid sheets & meeting notes",
                icon = Icons.Default.TextSnippet,
                accentColor = EmeraldSuccess,
                category = "Creation & Conversion"
            ),
            ToolItem(
                id = ToolId.PDF_TO_IMAGES,
                title = "PDF to Images",
                description = "Export all pages to high-resolution PNG format",
                icon = Icons.Default.PictureAsPdf,
                accentColor = Color(0xFFF59E0B),
                category = "Creation & Conversion"
            ),

            // Category: Organization
            ToolItem(
                id = ToolId.MERGE_PDFS,
                title = "Multi-Document Merge",
                description = "Combine two or more PDF files into a single unified document",
                icon = Icons.Default.MergeType,
                accentColor = NeonIndigo,
                category = "Organization & Structure"
            ),
            ToolItem(
                id = ToolId.SPLIT_PDF,
                title = "Document Splitter",
                description = "Extract pages or split into individual single-page documents",
                icon = Icons.Default.Description,
                accentColor = ElectricCyan,
                category = "Organization & Structure"
            ),

            // Category: Security & Redaction
            ToolItem(
                id = ToolId.PASSWORD_ENCRYPT,
                title = "Password Encrypt (AES)",
                description = "Lock confidential files with 128/256-bit AES encryption",
                icon = Icons.Default.Lock,
                accentColor = CrimsonWarning,
                category = "Security & Protection"
            ),
            ToolItem(
                id = ToolId.PASSWORD_DECRYPT,
                title = "Unlock & Remove Password",
                description = "Permanently decrypt password-protected PDF files",
                icon = Icons.Default.LockOpen,
                accentColor = EmeraldSuccess,
                category = "Security & Protection"
            ),
            ToolItem(
                id = ToolId.WATERMARK,
                title = "Digital Watermark",
                description = "Stamp custom security text diagonally across all pages",
                icon = Icons.Default.WaterDrop,
                accentColor = Color(0xFF8B5CF6),
                category = "Security & Protection"
            ),

            // Category: Inspection & Utilities
            ToolItem(
                id = ToolId.ON_DEVICE_OCR,
                title = "On-Device ML OCR",
                description = "Extract text from scanned non-searchable pages with ML Kit",
                icon = Icons.Default.DocumentScanner,
                accentColor = ElectricCyan,
                category = "Intelligence & Utilities"
            ),
            ToolItem(
                id = ToolId.METADATA_EDITOR,
                title = "Metadata Inspector",
                description = "View and edit document Title, Author, Subject & Producer",
                icon = Icons.Default.Info,
                accentColor = NeonIndigo,
                category = "Intelligence & Utilities"
            ),
            ToolItem(
                id = ToolId.COMPRESS_PDF,
                title = "File Compressor",
                description = "Optimize document size and remove redundant objects",
                icon = Icons.Default.Compress,
                accentColor = EmeraldSuccess,
                category = "Intelligence & Utilities"
            )
        )
    }

    val groupedTools = remember(toolsList) {
        toolsList.groupBy { it.category }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tools Workstation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("100% Offline • Zero Backend Cost", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSurface)
            )
        },
        containerColor = ObsidianBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedTools.forEach { (category, tools) ->
                item {
                    Text(
                        text = category.uppercase(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                items(tools) { tool ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onOpenTool(tool.id) }
                            .testTag("tool_card_${tool.id.name.lowercase()}"),
                        color = ObsidianSurface,
                        tonalElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(tool.accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = null,
                                    tint = tool.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tool.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tool.description,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

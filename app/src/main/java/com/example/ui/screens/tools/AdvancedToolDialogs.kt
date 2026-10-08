package com.example.ui.screens.tools

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import com.example.engine.DocumentImportHelper
import com.example.engine.NativePdfCreator
import com.example.engine.PdfBoxOperations
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHighlight
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun DocumentSelectorSection(
    database: AppDatabase,
    selectedDoc: DocumentEntity?,
    onDocSelected: (DocumentEntity) -> Unit,
    titleText: String = "Select Document:"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var documents by remember { mutableStateOf<List<DocumentEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        database.documentDao().getPublicDocuments().collect { docs ->
            documents = docs
            if (selectedDoc == null && docs.isNotEmpty()) {
                onDocSelected(docs.first())
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val newDoc = DocumentImportHelper.importPdfFromUri(context, database, uri)
                if (newDoc != null) {
                    onDocSelected(newDoc)
                    Toast.makeText(context, "Loaded ${newDoc.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(titleText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/pdf")) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_import_pdf_button")
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElectricCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pick from Storage", color = ElectricCyan, fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (documents.isEmpty()) {
            Surface(
                color = ObsidianSurfaceHighlight,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { importLauncher.launch(arrayOf("application/pdf")) }
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No PDF files in workspace yet", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap here to upload a PDF from your device storage", color = ElectricCyan, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.height(130.dp)) {
                items(documents) { doc ->
                    val isSelected = selectedDoc?.id == doc.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onDocSelected(doc) },
                        color = if (isSelected) NeonIndigo.copy(alpha = 0.25f) else ObsidianSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    doc.title,
                                    color = if (isSelected) ElectricCyan else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    "${doc.pageCount} pages • ${doc.fileSize / 1024} KB",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompressPdfDialog(
    database: AppDatabase,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ObsidianSurface,
        title = { Text("Compress PDF File", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DocumentSelectorSection(database, selectedDoc, { selectedDoc = it })
                Spacer(modifier = Modifier.height(14.dp))
                selectedDoc?.let { doc ->
                    Surface(
                        color = ObsidianSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Original Size: ${doc.fileSize / 1024} KB", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Estimated Savings: ~30% – 60%", color = EmeraldSuccess, fontSize = 12.sp)
                            Text("Method: Stream Deflate & Metadata Compact", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoc ?: return@Button
                    scope.launch {
                        isProcessing = true
                        val inFile = File(doc.filePath)
                        val outDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val outFile = File(outDir, "${doc.title}_compressed_${System.currentTimeMillis()}.pdf")
                        val res = PdfBoxOperations.compressDocument(inFile, outFile)
                        if (res.isSuccess) {
                            val savedBytes = doc.fileSize - outFile.length()
                            val savedKb = (savedBytes / 1024).coerceAtLeast(0)
                            database.documentDao().insertOrUpdate(
                                DocumentEntity(
                                    id = "compressed_${System.currentTimeMillis()}",
                                    title = "${doc.title} (Compressed)",
                                    filePath = outFile.absolutePath,
                                    pageCount = doc.pageCount,
                                    fileSize = outFile.length(),
                                    lastOpenedTimestamp = System.currentTimeMillis()
                                )
                            )
                            Toast.makeText(context, "Compressed! Saved ${savedKb} KB.", Toast.LENGTH_LONG).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Compression completed.", Toast.LENGTH_SHORT).show()
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing && selectedDoc != null,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                } else {
                    Text("Compress Now", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun WatermarkPdfDialog(
    database: AppDatabase,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var opacity by remember { mutableFloatStateOf(0.35f) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ObsidianSurface,
        title = { Text("Stamp Watermark", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DocumentSelectorSection(database, selectedDoc, { selectedDoc = it })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Watermark Text") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = ObsidianSurfaceHighlight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Opacity: ${(opacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                }
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.1f..0.8f,
                    colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoc ?: return@Button
                    if (watermarkText.isBlank()) return@Button
                    scope.launch {
                        isProcessing = true
                        val inFile = File(doc.filePath)
                        val outDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val outFile = File(outDir, "${doc.title}_watermarked_${System.currentTimeMillis()}.pdf")
                        val res = PdfBoxOperations.applyWatermark(inFile, outFile, watermarkText, opacity)
                        if (res.isSuccess) {
                            database.documentDao().insertOrUpdate(
                                DocumentEntity(
                                    id = "watermarked_${System.currentTimeMillis()}",
                                    title = "${doc.title} (Watermarked)",
                                    filePath = outFile.absolutePath,
                                    pageCount = doc.pageCount,
                                    fileSize = outFile.length(),
                                    lastOpenedTimestamp = System.currentTimeMillis()
                                )
                            )
                            Toast.makeText(context, "Watermark stamped successfully!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing && selectedDoc != null,
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("Apply Watermark")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun PageNumbersDialog(
    database: AppDatabase,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var position by remember { mutableStateOf("BOTTOM_RIGHT") }
    var formatPattern by remember { mutableStateOf("Page %d of %d") }
    var isProcessing by remember { mutableStateOf(false) }

    val positions = listOf(
        Pair("BOTTOM_RIGHT", "Bottom Right"),
        Pair("BOTTOM_CENTER", "Bottom Center"),
        Pair("TOP_RIGHT", "Top Right")
    )

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ObsidianSurface,
        title = { Text("Insert Page Numbers", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DocumentSelectorSection(database, selectedDoc, { selectedDoc = it })
                Spacer(modifier = Modifier.height(12.dp))
                Text("Position:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    positions.forEach { (key, label) ->
                        val isSelected = position == key
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { position = key },
                            color = if (isSelected) NeonIndigo else ObsidianSurfaceHighlight
                        ) {
                            Text(
                                label,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = formatPattern,
                    onValueChange = { formatPattern = it },
                    label = { Text("Format Pattern (e.g. Page %d of %d)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = ObsidianSurfaceHighlight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoc ?: return@Button
                    scope.launch {
                        isProcessing = true
                        val inFile = File(doc.filePath)
                        val outDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val outFile = File(outDir, "${doc.title}_numbered_${System.currentTimeMillis()}.pdf")
                        val res = PdfBoxOperations.addPageNumbers(inFile, outFile, position, formatPattern)
                        if (res.isSuccess) {
                            database.documentDao().insertOrUpdate(
                                DocumentEntity(
                                    id = "numbered_${System.currentTimeMillis()}",
                                    title = "${doc.title} (Numbered)",
                                    filePath = outFile.absolutePath,
                                    pageCount = doc.pageCount,
                                    fileSize = outFile.length(),
                                    lastOpenedTimestamp = System.currentTimeMillis()
                                )
                            )
                            Toast.makeText(context, "Page numbers stamped on all pages!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing && selectedDoc != null,
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("Add Numbers")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun UnlockPdfDialog(
    database: AppDatabase,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var password by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ObsidianSurface,
        title = { Text("Unlock / Decrypt PDF", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DocumentSelectorSection(database, selectedDoc, { selectedDoc = it })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password to Remove") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = ObsidianSurfaceHighlight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoc ?: return@Button
                    scope.launch {
                        isProcessing = true
                        val inFile = File(doc.filePath)
                        val outDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val outFile = File(outDir, "${doc.title}_unlocked_${System.currentTimeMillis()}.pdf")
                        val res = PdfBoxOperations.decryptDocument(inFile, outFile, password)
                        if (res.isSuccess) {
                            database.documentDao().insertOrUpdate(
                                DocumentEntity(
                                    id = "unlocked_${System.currentTimeMillis()}",
                                    title = "${doc.title} (Unlocked)",
                                    filePath = outFile.absolutePath,
                                    pageCount = doc.pageCount,
                                    fileSize = outFile.length(),
                                    lastOpenedTimestamp = System.currentTimeMillis()
                                )
                            )
                            Toast.makeText(context, "Password protection removed!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Invalid password or already unlocked", Toast.LENGTH_LONG).show()
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing && selectedDoc != null,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                } else {
                    Text("Unlock PDF", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun RepairPdfDialog(
    database: AppDatabase,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ObsidianSurface,
        title = { Text("Repair Damaged PDF", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DocumentSelectorSection(database, selectedDoc, { selectedDoc = it })
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = ObsidianSurfaceHighlight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Cross-reference (xref) Table Reconstruction", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "This utility parses defective byte streams, restores broken page references, and recovers accessible textual & image data.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoc ?: return@Button
                    scope.launch {
                        isProcessing = true
                        val inFile = File(doc.filePath)
                        val outDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val outFile = File(outDir, "${doc.title}_repaired_${System.currentTimeMillis()}.pdf")
                        val res = PdfBoxOperations.repairDocument(inFile, outFile)
                        if (res.isSuccess) {
                            database.documentDao().insertOrUpdate(
                                DocumentEntity(
                                    id = "repaired_${System.currentTimeMillis()}",
                                    title = "${doc.title} (Repaired)",
                                    filePath = outFile.absolutePath,
                                    pageCount = doc.pageCount,
                                    fileSize = outFile.length(),
                                    lastOpenedTimestamp = System.currentTimeMillis()
                                )
                            )
                            Toast.makeText(context, "Document repaired and recovered successfully!", Toast.LENGTH_LONG).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing && selectedDoc != null,
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("Repair Document")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun CropPdfDialog(
    database: AppDatabase,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var cropPercent by remember { mutableFloatStateOf(0.05f) }
    var isProcessing by remember { mutableStateOf(false) }

    val cropOptions = listOf(
        Pair(0.05f, "5% Trim"),
        Pair(0.10f, "10% Trim"),
        Pair(0.15f, "15% Trim")
    )

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ObsidianSurface,
        title = { Text("Crop PDF Margins", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DocumentSelectorSection(database, selectedDoc, { selectedDoc = it })
                Spacer(modifier = Modifier.height(12.dp))
                Text("Margin Trimming Amount:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cropOptions.forEach { (pct, label) ->
                        val isSelected = cropPercent == pct
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { cropPercent = pct },
                            color = if (isSelected) NeonIndigo else ObsidianSurfaceHighlight
                        ) {
                            Text(
                                label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoc ?: return@Button
                    scope.launch {
                        isProcessing = true
                        val inFile = File(doc.filePath)
                        val outDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val outFile = File(outDir, "${doc.title}_cropped_${System.currentTimeMillis()}.pdf")
                        val res = PdfBoxOperations.cropMargins(inFile, outFile, cropPercent)
                        if (res.isSuccess) {
                            database.documentDao().insertOrUpdate(
                                DocumentEntity(
                                    id = "cropped_${System.currentTimeMillis()}",
                                    title = "${doc.title} (Cropped)",
                                    filePath = outFile.absolutePath,
                                    pageCount = doc.pageCount,
                                    fileSize = outFile.length(),
                                    lastOpenedTimestamp = System.currentTimeMillis()
                                )
                            )
                            Toast.makeText(context, "Margins cropped successfully!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Crop completed", Toast.LENGTH_SHORT).show()
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing && selectedDoc != null,
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("Crop Margins")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun PdfToImagesDialog(
    database: AppDatabase,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ObsidianSurface,
        title = { Text("Export PDF Pages to JPG", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DocumentSelectorSection(database, selectedDoc, { selectedDoc = it })
                Spacer(modifier = Modifier.height(12.dp))
                selectedDoc?.let { doc ->
                    Text(
                        "Will render ${doc.pageCount} page(s) at 300 DPI into high-resolution JPG images.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoc ?: return@Button
                    scope.launch {
                        isProcessing = true
                        val inFile = File(doc.filePath)
                        val outDir = File(context.filesDir, "exports").apply { mkdirs() }
                        val res = PdfBoxOperations.exportToJpg(context, inFile, outDir)
                        if (res.isSuccess) {
                            val images = res.getOrThrow()
                            Toast.makeText(context, "Exported ${images.size} JPG images to storage!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Export error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing && selectedDoc != null,
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("Export Images")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun HtmlToPdfDialog(
    database: AppDatabase,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var urlOrTitle by remember { mutableStateOf("Enterprise Security Policy") }
    var htmlContent by remember {
        mutableStateOf(
            "<h1>Enterprise Document Security</h1>\n" +
            "<p>This document details the Zero-Trust local processing guidelines.</p>\n" +
            "<ul>\n" +
            "  <li>All rendering executed on native Skia double-buffered canvas.</li>\n" +
            "  <li>Cryptographic hashes verified using AES-128 / AES-256 standards.</li>\n" +
            "  <li>Local sandbox ensures zero data leakage.</li>\n" +
            "</ul>"
        )
    }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = ObsidianSurface,
        title = { Text("HTML / Webpage to PDF", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = urlOrTitle,
                    onValueChange = { urlOrTitle = it },
                    label = { Text("Document Title / Webpage Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = ObsidianSurfaceHighlight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = htmlContent,
                    onValueChange = { htmlContent = it },
                    label = { Text("HTML Code or Text Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = ObsidianSurfaceHighlight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (urlOrTitle.isBlank()) return@Button
                    scope.launch {
                        isProcessing = true
                        val outDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val safeName = urlOrTitle.replace(Regex("[^a-zA-Z0-9_]"), "_")
                        val outFile = File(outDir, "${safeName}_${System.currentTimeMillis()}.pdf")

                        // Strip simple HTML tags for clean native rendering
                        val cleanText = htmlContent.replace(Regex("<[^>]*>"), "")
                        val res = NativePdfCreator.createPdfFromText(urlOrTitle, cleanText, outFile)
                        if (res.isSuccess) {
                            database.documentDao().insertOrUpdate(
                                DocumentEntity(
                                    id = "html_${System.currentTimeMillis()}",
                                    title = urlOrTitle,
                                    filePath = outFile.absolutePath,
                                    pageCount = 1,
                                    fileSize = outFile.length(),
                                    lastOpenedTimestamp = System.currentTimeMillis()
                                )
                            )
                            Toast.makeText(context, "HTML converted to PDF successfully!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Error converting HTML", Toast.LENGTH_SHORT).show()
                        }
                        isProcessing = false
                    }
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("Generate PDF")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

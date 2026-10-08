package com.example.ui.screens.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import com.example.engine.NativePdfCreator
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
fun DocumentScannerScreen(
    database: AppDatabase,
    onNavigateBack: () -> Unit,
    onDocumentCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission required to scan", Toast.LENGTH_SHORT).show()
        }
    }

    val capturedUris = remember { mutableStateListOf<Uri>() }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var docTitle by remember { mutableStateOf("Scanned Document") }
    var isSaving by remember { mutableStateOf(false) }

    // Photo picker fallback for zero-permission gallery scanning
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                capturedUris.add(uri)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        if (hasCameraPermission) {
            // Live CameraX Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Camera Access Needed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Grant permission or select images from gallery to scan.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                    ) {
                        Text("Grant Permission")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceHighlight)
                    ) {
                        Text("Pick from Gallery", color = ElectricCyan)
                    }
                }
            }
        }

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .testTag("scanner_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "${capturedUris.size} Pages Scanned",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            IconButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Collections, contentDescription = "Gallery", tint = ElectricCyan)
            }
        }

        // Bottom Capture & Finish Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Thumbnails of captured pages
            if (capturedUris.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(capturedUris) { uri ->
                        Box(
                            modifier = Modifier
                                .size(60.dp, 80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, NeonIndigo, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { capturedUris.remove(uri) },
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = ObsidianSurface.copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (capturedUris.isNotEmpty()) {
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { docTitle = it },
                            label = { Text("PDF Document Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scanner_doc_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = ObsidianSurfaceHighlight,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Capture shutter button
                        IconButton(
                            onClick = {
                                val capture = imageCapture ?: return@IconButton
                                val photoFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                capture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            capturedUris.add(Uri.fromFile(photoFile))
                                            Toast.makeText(context, "Page captured!", Toast.LENGTH_SHORT).show()
                                        }
                                        override fun onError(exc: ImageCaptureException) {
                                            Toast.makeText(context, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, NeonIndigo, CircleShape)
                                .testTag("scanner_shutter_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(NeonIndigo)
                            )
                        }

                        // Save as PDF Button
                        if (capturedUris.isNotEmpty()) {
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        isSaving = true
                                        val docsDir = File(context.filesDir, "documents").apply { mkdirs() }
                                        val cleanName = docTitle.replace(Regex("[^a-zA-Z0-9_]"), "_")
                                        val outFile = File(docsDir, "${cleanName}_${System.currentTimeMillis()}.pdf")

                                        val res = NativePdfCreator.createPdfFromImages(context, capturedUris.toList(), outFile)
                                        if (res.isSuccess) {
                                            val docId = "scan_${System.currentTimeMillis()}"
                                            database.documentDao().insertOrUpdate(
                                                DocumentEntity(
                                                    id = docId,
                                                    title = docTitle,
                                                    filePath = outFile.absolutePath,
                                                    pageCount = capturedUris.size,
                                                    fileSize = outFile.length(),
                                                    lastOpenedTimestamp = System.currentTimeMillis()
                                                )
                                            )
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Scanned PDF created!", Toast.LENGTH_SHORT).show()
                                                onDocumentCreated(docId)
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Error saving scanned PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        isSaving = false
                                    }
                                },
                                enabled = !isSaving,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("scanner_save_pdf_button")
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save PDF", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

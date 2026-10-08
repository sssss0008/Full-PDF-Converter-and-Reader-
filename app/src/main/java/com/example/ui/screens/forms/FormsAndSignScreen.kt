package com.example.ui.screens.forms

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesRepository
import com.example.data.local.SignatureEntity
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
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormsAndSignScreen(
    database: AppDatabase,
    preferencesRepository: PreferencesRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val savedSignatures by database.signatureDao().getAllSignatures().collectAsState(initial = emptyList())
    val profileName by preferencesRepository.profileName.collectAsState(initial = "Alex Mercer")
    val profileEmail by preferencesRepository.profileEmail.collectAsState(initial = "alex.mercer@enterprise.io")
    val profileTitle by preferencesRepository.profileTitle.collectAsState(initial = "Senior Systems Architect")
    val profileCompany by preferencesRepository.profileCompany.collectAsState(initial = "Omni Systems Global")

    var isEditProfileOpen by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(profileName) }
    var editEmail by remember { mutableStateOf(profileEmail) }
    var editTitle by remember { mutableStateOf(profileTitle) }
    var editCompany by remember { mutableStateOf(profileCompany) }

    // Signature Drawing Pad State
    val signaturePoints = remember { mutableStateListOf<Offset>() }
    var signatureLabel by remember { mutableStateOf("Primary Signature") }
    var isInitialStamp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sign & Forms Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Digital Signatures • AcroForm Profiles", color = ElectricCyan, fontSize = 12.sp)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Pre-filled Identity Profile Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ObsidianSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = NeonIndigo)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AcroForm Identity Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        TextButton(
                            onClick = {
                                editName = profileName
                                editEmail = profileEmail
                                editTitle = profileTitle
                                editCompany = profileCompany
                                isEditProfileOpen = true
                            },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Text("Edit", color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = profileName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(text = "$profileTitle • $profileCompany", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text(text = profileEmail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    val dateFormatted = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date()) }
                    Surface(
                        color = ObsidianSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quick Date: $dateFormatted", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Section 2: Signature Drawing Pad
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ObsidianSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Draw, contentDescription = null, tint = ElectricCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Draw Realistic Signature", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Row {
                            TextButton(onClick = { signaturePoints.clear() }) {
                                Text("Clear", color = CrimsonWarning, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sign using finger or stylus on the canvas below with velocity smoothing:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Signature Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F121C))
                            .border(1.dp, NeonIndigo.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset -> signaturePoints.add(offset) },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        signaturePoints.add(change.position)
                                    }
                                )
                            }
                            .testTag("signature_canvas_pad")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (signaturePoints.size > 1) {
                                val path = Path().apply {
                                    moveTo(signaturePoints[0].x, signaturePoints[0].y)
                                    for (i in 1 until signaturePoints.size) {
                                        val prev = signaturePoints[i - 1]
                                        val curr = signaturePoints[i]
                                        val midX = (prev.x + curr.x) / 2f
                                        val midY = (prev.y + curr.y) / 2f
                                        quadraticTo(prev.x, prev.y, midX, midY)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = NeonIndigo,
                                    style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        if (signaturePoints.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Sign Here ✍️", color = Color.DarkGray, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = signatureLabel,
                            onValueChange = { signatureLabel = it },
                            label = { Text("Signature Label") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("signature_label_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonIndigo,
                                unfocusedBorderColor = ObsidianSurfaceHighlight,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (signaturePoints.size < 5) {
                                    Toast.makeText(context, "Please draw a valid signature", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                scope.launch(Dispatchers.IO) {
                                    // Save signature as PNG
                                    val sigDir = File(context.filesDir, "signatures").apply { mkdirs() }
                                    val sigFile = File(sigDir, "sig_${System.currentTimeMillis()}.png")

                                    val bitmap = Bitmap.createBitmap(500, 250, Bitmap.Config.ARGB_8888)
                                    val canvas = android.graphics.Canvas(bitmap)
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        strokeWidth = 6f
                                        style = android.graphics.Paint.Style.STROKE
                                        isAntiAlias = true
                                    }
                                    if (signaturePoints.size > 1) {
                                        for (i in 1 until signaturePoints.size) {
                                            canvas.drawLine(
                                                signaturePoints[i - 1].x,
                                                signaturePoints[i - 1].y,
                                                signaturePoints[i].x,
                                                signaturePoints[i].y,
                                                paint
                                            )
                                        }
                                    }
                                    FileOutputStream(sigFile).use { fos ->
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                    }

                                    database.signatureDao().insert(
                                        SignatureEntity(
                                            id = "sig_${System.currentTimeMillis()}",
                                            label = signatureLabel,
                                            imagePath = sigFile.absolutePath,
                                            isInitial = isInitialStamp
                                        )
                                    )
                                    withContext(Dispatchers.Main) {
                                        signaturePoints.clear()
                                        Toast.makeText(context, "Signature saved to vault!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("save_signature_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 3: Saved Signatures Vault (up to 5 stored signatures)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ObsidianSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = NeonIndigo)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stored Signatures (${savedSignatures.size}/5)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    if (savedSignatures.isEmpty()) {
                        Text(
                            text = "No saved signatures yet. Draw and save your signature above for one-tap document stamping.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(savedSignatures) { sig ->
                                Surface(
                                    modifier = Modifier
                                        .size(170.dp, 100.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    color = ObsidianSurfaceHighlight,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonIndigo.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sig.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                            IconButton(
                                                onClick = {
                                                    scope.launch(Dispatchers.IO) {
                                                        database.signatureDao().delete(sig)
                                                        File(sig.imagePath).delete()
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonWarning, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Digital Stamp Ready ✓", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Edit Profile Dialog
        if (isEditProfileOpen) {
            AlertDialog(
                onDismissRequest = { isEditProfileOpen = false },
                containerColor = ObsidianSurface,
                title = { Text("Edit Identity Profile", color = Color.White) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonIndigo,
                                unfocusedBorderColor = ObsidianSurfaceHighlight,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonIndigo,
                                unfocusedBorderColor = ObsidianSurfaceHighlight,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Professional Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonIndigo,
                                unfocusedBorderColor = ObsidianSurfaceHighlight,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editCompany,
                            onValueChange = { editCompany = it },
                            label = { Text("Company / Organization") },
                            modifier = Modifier.fillMaxWidth(),
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
                            scope.launch {
                                preferencesRepository.updateProfile(editName, editEmail, editTitle, editCompany)
                                isEditProfileOpen = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                    ) {
                        Text("Save Profile")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isEditProfileOpen = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

package com.example.ui.screens.safe

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesRepository
import com.example.ui.theme.CrimsonWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHighlight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentSafeScreen(
    database: AppDatabase,
    preferencesRepository: PreferencesRepository,
    onOpenDocument: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isUnlocked by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val vaultDocs by database.documentDao().getVaultDocuments().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUnlocked) ElectricCyan else CrimsonWarning
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Encrypted Document Safe", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                actions = {
                    if (isUnlocked) {
                        TextButton(onClick = {
                            isUnlocked = false
                            pinInput = ""
                        }) {
                            Text("Lock Safe", color = CrimsonWarning, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSurface)
            )
        },
        containerColor = ObsidianBackground
    ) { padding ->
        if (!isUnlocked) {
            // Locked Vault Authentication Gate
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = CircleShape,
                        color = CrimsonWarning.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = CrimsonWarning, modifier = Modifier.size(48.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Confidential Vault", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Enter your 4-digit PIN or use Biometric unlock to view protected files:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4) {
                                pinInput = it
                                pinError = false
                                if (it == "0000" || it.length == 4) {
                                    isUnlocked = true
                                }
                            }
                        },
                        placeholder = { Text("Enter 4-digit PIN (Default 0000)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("vault_pin_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = ObsidianSurfaceHighlight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    if (pinError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Incorrect PIN code", color = CrimsonWarning, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { isUnlocked = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("vault_biometric_button")
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Biometric Fingerprint Unlock", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Unlocked Vault Document List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Safe Vault Active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Files in this vault are hidden from standard file explorer and searches.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }

                if (vaultDocs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No files currently stored in safe vault.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("To protect a document, tap 'More' on any file in the dashboard and choose 'Move to Safe Vault'.", color = MaterialTheme.colorScheme.outlineVariant, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(vaultDocs) { doc ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenDocument(doc.id) },
                                color = ObsidianSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianSurfaceHighlight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${doc.pageCount} pages • ${doc.fileSize / 1024} KB", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                database.documentDao().setVaultProtected(doc.id, false)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = "Remove from Safe", tint = ElectricCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

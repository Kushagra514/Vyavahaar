package org.vyavahaar.app.ui.screens.prescriptions

import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.ApiClient
import org.vyavahaar.app.data.remote.MedicineSearchDto
import org.vyavahaar.app.data.remote.PrescriptionResponseDto
import org.vyavahaar.app.data.repository.PrescriptionRepository
import org.vyavahaar.app.theme.*
import org.vyavahaar.app.ui.components.VyavahaarButton
import org.vyavahaar.app.ui.components.VyavahaarTopBar

@Composable
fun PrescriptionsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prescriptionRepo = remember { PrescriptionRepository(ApiClient.apiService, PreferencesDataStore(context)) }

    var searchQuery by remember { mutableStateOf("Metformin") }
    var searchResult by remember { mutableStateOf<MedicineSearchDto?>(null) }
    var scanResult by remember { mutableStateOf<PrescriptionResponseDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            isLoading = true
            coroutineScope.launch {
                try {
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                    val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    val res = prescriptionRepo.scanPrescription(base64Image)
                    isLoading = false
                    res.onSuccess { scanResult = it }
                        .onFailure {
                            Toast.makeText(context, "Prescription scan vifal raha. Kripya punah prayas karein.", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    isLoading = false
                    Toast.makeText(context, "Photo process karne mein truti: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun runSampleOcrDemo() {
        isLoading = true
        coroutineScope.launch {
            val sampleRx = (
                "Rx: Dr. A. K. Sharma (MD Internal Med)\n" +
                "Patient: Aadarniya Sadasya, Age 68\n" +
                "1. Tab Metformin 500mg SR - twice daily after meals\n" +
                "2. Tab Amlodipine 5mg - once daily morning\n" +
                "3. Tab Pan 40 - before breakfast\n" +
                "Follow up in 2 weeks."
            )
            val b64 = Base64.encodeToString(sampleRx.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val res = prescriptionRepo.scanPrescription(b64)
            isLoading = false
            res.onSuccess {
                scanResult = it
                Toast.makeText(context, "Sample Prescription OCR Safal!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Scan vifal: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { VyavahaarTopBar(title = "Dawai & Parchi Jankaari", onBackClick = onBack) },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Camera Scan Card for Prescription Photo
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Doctor Ki Parchi Scan Karein (OCR)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Parchi ki saaf photo lein aur dawaiyon ke naam dekhein.", style = MaterialTheme.typography.bodyMedium, color = TextMediumContrast)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Camera uplabdh nahi hai. Sample demo run kar rahe hain.", Toast.LENGTH_SHORT).show()
                                    runSampleOcrDemo()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isLoading
                        ) {
                            Text(if (isLoading) "Scan..." else "Camera Scan", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { runSampleOcrDemo() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isLoading
                        ) {
                            Text("🧪 Sample Demo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medicine Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Dawai Ka Naam Khojein (e.g. Metformin, Paracetamol)") },
                trailingIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val res = prescriptionRepo.searchMedicine(searchQuery)
                            res.onSuccess { searchResult = it }
                               .onFailure {
                                   Toast.makeText(context, "Dawai nahi mili: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                               }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Display OCR Scan Results if available
                if (scanResult != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "OCR Natije (Doctor Parchi)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${(scanResult!!.confidenceScore * 100).toInt()}% Vishwas") }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = scanResult!!.extractedRawText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMediumContrast
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // List of extracted medicine cards with one-tap alternative search
                                scanResult!!.extractedMedicines.forEach { med ->
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = WarmCreamBackground),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(med.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "${med.formulation} • ${med.strength ?: ""} • ${med.instructions ?: ""}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMediumContrast
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    val queryWord = med.name.split(" ").firstOrNull() ?: med.name
                                                    searchQuery = queryWord
                                                    coroutineScope.launch {
                                                        val res = prescriptionRepo.searchMedicine(queryWord)
                                                        res.onSuccess { searchResult = it }
                                                    }
                                                }
                                            ) {
                                                Text("Saste Vikalp Khojein", color = ForestGreen, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Card(colors = CardDefaults.cardColors(containerColor = SosCrimsonContainer)) {
                                    Text(
                                        text = scanResult!!.medicalSafetyWarning,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SosCrimson,
                                        modifier = Modifier.padding(10.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Display Medicine Info & Generic Alternatives
                if (searchResult != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Satyapit Salt: ${searchResult!!.matchedSalt}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                                Text(
                                    text = searchResult!!.safetyDisclaimer,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMediumContrast
                                )
                            }
                        }
                    }

                    items(searchResult!!.verifiedEquivalents) { alt ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(alt.brandName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("₹${alt.approximatePriceInr}", style = MaterialTheme.typography.titleMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Salt: ${alt.activeIngredient} (${alt.strength})", style = MaterialTheme.typography.bodyMedium)
                                Text("Nirmata: ${alt.manufacturer} • ${alt.dosageForm}", style = MaterialTheme.typography.bodySmall, color = TextMediumContrast)
                                Text("Jankaari: ${alt.clinicalNote}", style = MaterialTheme.typography.bodySmall, color = TextHighContrast)
                            }
                        }
                    }
                }
            }
        }
    }
}

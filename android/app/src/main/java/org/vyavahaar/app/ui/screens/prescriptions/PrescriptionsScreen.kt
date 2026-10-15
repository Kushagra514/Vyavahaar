package org.vyavahaar.app.ui.screens.prescriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
            // Camera Scan Button for Prescription Photo
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
                    VyavahaarButton(
                        text = if (isLoading) "Scan ho raha hai..." else "Parchi Photo Scan Karein",
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                val res = prescriptionRepo.scanPrescription("sample_prescription_base64_payload")
                                isLoading = false
                                res.onSuccess { scanResult = it }
                            }
                        }
                    )
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
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display OCR Scan Results if available
            if (scanResult != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = TerracottaPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parchi OCR Transcription (Provisional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(scanResult!!.extractedRawText, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
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

            // Display Medicine Info & Generic Alternatives
            if (searchResult != null) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text(
                            text = "Dawai Salt: ${searchResult!!.matchedSalt}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = searchResult!!.safetyDisclaimer,
                            style = MaterialTheme.typography.bodySmall,
                            color = TerracottaDark
                        )
                    }
                    items(searchResult!!.verifiedEquivalents) { alt ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmCardSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(alt.brandName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("₹${alt.approximatePriceInr}", style = MaterialTheme.typography.titleMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
                                }
                                Text("Salt: ${alt.activeIngredient} (${alt.strength})", style = MaterialTheme.typography.bodyMedium)
                                Text("Nirmata: ${alt.manufacturer} • ${alt.dosageForm}", style = MaterialTheme.typography.bodySmall, color = TextMediumContrast)
                                Text("Note: ${alt.clinicalNote}", style = MaterialTheme.typography.bodySmall, color = TextHighContrast)
                            }
                        }
                    }
                }
            }
        }
    }
}

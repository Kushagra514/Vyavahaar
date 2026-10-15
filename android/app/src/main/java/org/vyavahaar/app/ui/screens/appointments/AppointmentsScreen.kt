package org.vyavahaar.app.ui.screens.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
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
import org.vyavahaar.app.data.remote.AppointmentDto
import org.vyavahaar.app.data.repository.AppointmentsRepository
import org.vyavahaar.app.theme.*
import org.vyavahaar.app.ui.components.VyavahaarButton
import org.vyavahaar.app.ui.components.VyavahaarTopBar

@Composable
fun AppointmentsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apptRepo = remember { AppointmentsRepository(ApiClient.apiService, PreferencesDataStore(context)) }

    var appointments by remember { mutableStateOf<List<AppointmentDto>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var doctorName by remember { mutableStateOf("Dr. Ramesh Gupta") }
    var specialty by remember { mutableStateOf("Geriatric & Cardiology") }
    var clinic by remember { mutableStateOf("Apollo Clinic Indiranagar") }
    var recurrence by remember { mutableStateOf("MONTHLY") }

    fun refresh() {
        coroutineScope.launch {
            val res = apptRepo.getAppointments()
            res.onSuccess { appointments = it }
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Scaffold(
        topBar = { VyavahaarTopBar(title = "Doctor Appointments & Reminders", onBackClick = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = TerracottaPrimary,
                contentColor = WarmCardSurface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Schedule Appointment")
            }
        },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Aapki Doctor Visits",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextHighContrast
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (appointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Abhi koi appointment darj nahi hai.\nNeeche '+' button dabakar jodein.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMediumContrast
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(appointments) { appt ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(appt.doctorName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(appt.recurrenceRule, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                                Text("${appt.specialty} • ${appt.clinicOrHospital}", style = MaterialTheme.typography.bodyMedium, color = TextMediumContrast)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Samay: ${appt.appointmentTime}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nayi Doctor Visit Book Karein", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = doctorName, onValueChange = { doctorName = it }, label = { Text("Doctor Name") })
                        OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Specialty") })
                        OutlinedTextField(value = clinic, onValueChange = { clinic = it }, label = { Text("Hospital / Clinic") })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = recurrence == "NONE", onClick = { recurrence = "NONE" }, label = { Text("One-Time") })
                            FilterChip(selected = recurrence == "MONTHLY", onClick = { recurrence = "MONTHLY" }, label = { Text("Monthly") })
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                apptRepo.createAppointment(doctorName, specialty, clinic, "2026-10-25 10:30 AM", recurrence)
                                showDialog = false
                                refresh()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Text("Schedule Karein")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

package org.vyavahaar.app.ui.screens.transport

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.vyavahaar.app.R
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.ApiClient
import org.vyavahaar.app.data.remote.TransportDto
import org.vyavahaar.app.data.repository.TransportRepository
import org.vyavahaar.app.theme.*
import org.vyavahaar.app.ui.components.VyavahaarButton
import org.vyavahaar.app.ui.components.VyavahaarTopBar

@Composable
fun TransportScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val transportRepo = remember { TransportRepository(ApiClient.apiService, PreferencesDataStore(context)) }

    var selectedMode by remember { mutableStateOf("CAB") }  // CAB or AMBULANCE
    var pickupAddress by remember { mutableStateOf("Home (Indiranagar 12th Main)") }
    var destinationAddress by remember { mutableStateOf("Sri Ram Mandir / Manipal Hospital") }
    var currentTransport by remember { mutableStateOf<TransportDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // String resources for non-Composable lambdas
    val transportModeCab = stringResource(R.string.transport_mode_cab)
    val transportModeAmbulance = stringResource(R.string.transport_mode_ambulance)
    val transportBookRide = stringResource(R.string.transport_book_ride)
    val transportCallingAmbulance = stringResource(R.string.transport_calling_ambulance)
    val transportSelectPickup = stringResource(R.string.transport_select_pickup)
    val transportSelectDrop = stringResource(R.string.transport_select_drop)
    val transportConfirmBooking = stringResource(R.string.transport_confirm_booking)
    val transportCancel = stringResource(R.string.common_cancel)
    val transportCall112 = stringResource(R.string.sos_calling_112)

        Scaffold(
        topBar = { VyavahaarTopBar(title = stringResource(R.string.transport_title), onBackClick = onBack) },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode selector: Cab vs Ambulance
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { selectedMode = "CAB" },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == "CAB") TerracottaPrimary else WarmCardSurface,
                        contentColor = if (selectedMode == "CAB") WarmCardSurface else TextHighContrast
                    ),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = transportModeCab)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(transportModeCab, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { selectedMode = "AMBULANCE" },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == "AMBULANCE") SosCrimson else WarmCardSurface,
                        contentColor = if (selectedMode == "AMBULANCE") OnSosCrimson else SosCrimson
                    ),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Default.LocalHospital, contentDescription = transportModeAmbulance)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(transportModeAmbulance, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pickup & Destination Fields
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(transportSelectPickup, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pickupAddress,
                        onValueChange = { pickupAddress = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(transportSelectDrop, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = destinationAddress,
                        onValueChange = { destinationAddress = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action / Booking Card
            if (currentTransport == null) {
                VyavahaarButton(
                    text = if (selectedMode == "AMBULANCE") transportCallingAmbulance else transportBookRide,
                    containerColor = if (selectedMode == "AMBULANCE") SosCrimson else TerracottaPrimary,
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            val res = if (selectedMode == "AMBULANCE") {
                                transportRepo.requestAmbulance(pickupAddress, destinationAddress)
                            } else {
                                transportRepo.requestCab(pickupAddress, destinationAddress)
                            }
                            isLoading = false
                            res.onSuccess { currentTransport = it }
                        }
                    }
                )

                if (selectedMode == "AMBULANCE") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:112") }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SosCrimson),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(transportCall112, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Booking status display card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.transport_status, currentTransport!!.status),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (currentTransport!!.status == "CONFIRMED") ForestGreen else TerracottaPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (currentTransport!!.fareEstimate != null && currentTransport!!.fareEstimate!! > 0) {
                            Text(stringResource(R.string.transport_fare, currentTransport!!.fareEstimate), style = MaterialTheme.typography.bodyLarge)
                        }

                        if (currentTransport!!.driverName != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.transport_driver, currentTransport!!.driverName), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.transport_vehicle, currentTransport!!.vehicleModel, currentTransport!!.vehicleNumber), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.transport_phone, currentTransport!!.driverPhone), style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentTransport!!.status == "QUOTED") {
                        VyavahaarButton(
                            text = transportConfirmBooking,
                            onClick = {
                                    coroutineScope.launch {
                                        val res = transportRepo.confirmBooking(currentTransport!!.id)
                                        res.onSuccess { currentTransport = it }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val res = transportRepo.cancelBooking(currentTransport!!.id)
                                    res.onSuccess { currentTransport = null }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(transportCancel, color = SosCrimson, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

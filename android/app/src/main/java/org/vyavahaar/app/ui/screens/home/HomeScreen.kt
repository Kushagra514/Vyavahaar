package org.vyavahaar.app.ui.screens.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.vyavahaar.app.R
import org.vyavahaar.app.theme.*

@Composable
fun HomeScreen(
    onNavigateToEvents: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTransport: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToPrescriptions: () -> Unit,
    onNavigateToContacts: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showSosDialog by remember { mutableStateOf(false) }
    var sosAlertDetails by remember { mutableStateOf<String?>(null) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.stopListeningAndProcess(spokenText)
            } else {
                viewModel.onSpeechSilence()
            }
        } else {
            viewModel.dismissAssistant()
        }
    }

    fun startSpeechRecognitionIntent() {
        viewModel.startListening()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("hi-IN", "en-IN", "en-US"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Boliye, main sun raha hoon... (Speak now...)")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Aawaz input is device par uplabdh nahi hai.", Toast.LENGTH_SHORT).show()
            viewModel.onSpeechError("Voice input not supported on this device.")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognitionIntent()
        } else {
            showPermissionRationaleDialog = true
            viewModel.onPermissionDenied()
        }
    }

    fun launchSpeechRecognition() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognitionIntent()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Pulsing animation for microphone when active
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.isListening) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        containerColor = WarmCreamBackground,
        bottomBar = {
            // Accessible 3-Item Bottom Navigation Bar matching reference design
            Surface(
                color = WarmCreamBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Plus in Circle (Care / Transport / Services)
                    IconButton(
                        onClick = onNavigateToTransport,
                        modifier = Modifier
                            .size(56.dp)
                            .semantics {
                                contentDescription = "Medical, Cab, and Care Services"
                                role = Role.Button
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = TextHighContrast,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // 2. Calendar with Checkmark (Events & Community)
                    IconButton(
                        onClick = onNavigateToEvents,
                        modifier = Modifier
                            .size(56.dp)
                            .semantics {
                                contentDescription = "Community Events and Satsangs"
                                role = Role.Button
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = TextHighContrast,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // 3. Gear (Settings & Accessibility)
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(56.dp)
                            .semantics {
                                contentDescription = "Accessibility and Account Settings"
                                role = Role.Button
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = TextHighContrast,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Top Header: "Vyavahaar" in bold, then "Namaste, (user Name)"
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Vyavahaar",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextHighContrast,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Namaste,\n${uiState.userName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextHighContrast,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Upper-middle: Red SOS Button matching reference screenshot
            Button(
                onClick = { showSosDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SosCrimson,
                    contentColor = OnSosCrimson
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(60.dp)
                    .semantics {
                        contentDescription = "SOS emergency button. Tap to call 112 emergency services immediately"
                        role = Role.Button
                    }
            ) {
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pill status indicator below SOS button from mockup
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .border(width = 2.dp, color = SosCrimson, shape = RoundedCornerShape(12.dp))
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "112 Direct",
                    style = MaterialTheme.typography.labelSmall,
                    color = SosCrimson,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1.0f))

            // Center: Large Push-to-Talk Microphone Button (Matching orange-yellow circle)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(170.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(MicOrangeYellow)
                    .clickable {
                        if (uiState.isListening) {
                            viewModel.dismissAssistant()
                        } else if (uiState.voiceState == VoiceAssistantState.IDLE || 
                                   uiState.voiceState == VoiceAssistantState.ERROR || 
                                   uiState.voiceState == VoiceAssistantState.RESPONDING) {
                            launchSpeechRecognition()
                        }
                    }
                    .semantics {
                        contentDescription = if (uiState.isListening) {
                            "Microphone listening. Tap to finish speaking"
                        } else {
                            "Large microphone button. Tap and speak your request"
                        }
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone prompt text
            Text(
                text = if (uiState.isListening) {
                    stringResource(R.string.mic_listening)
                } else {
                    stringResource(R.string.mic_tap_prompt)
                },
                style = MaterialTheme.typography.titleMedium,
                color = TextHighContrast,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Voice command chips for quick senior access
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AssistChip(
                    onClick = { viewModel.stopListeningAndProcess("Mujhe aaj ke satsang dikhao") },
                    label = { Text("Satsang", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, Modifier.size(18.dp)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = { viewModel.stopListeningAndProcess("Cab book karo") },
                    label = { Text("Cab", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, Modifier.size(18.dp)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = { viewModel.stopListeningAndProcess("Call my daughter") },
                    label = { Text("Call Beti", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, Modifier.size(18.dp)) }
                )
            }

            // Live Assistant Feedback Card
            AnimatedVisibility(visible = uiState.assistantFeedback.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = TerracottaPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Vyavahaar Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.assistantFeedback,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextHighContrast
                        )
                        if (uiState.activeToolCall != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    when (uiState.activeToolCall?.toolName) {
                                        "search_events" -> onNavigateToEvents()
                                        "request_ride" -> onNavigateToTransport()
                                        "schedule_appointment" -> onNavigateToAppointments()
                                        "scan_prescription" -> onNavigateToPrescriptions()
                                        "start_call" -> onNavigateToContacts()
                                        "change_accessibility_settings" -> onNavigateToSettings()
                                        "send_sos_alert" -> { showSosDialog = true }
                                        else -> viewModel.dismissAssistant()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.activeToolCall?.toolName == "send_sos_alert") SosCrimson else TerracottaPrimary
                                )
                            ) {
                                Text(
                                    text = if (uiState.activeToolCall?.toolName == "send_sos_alert") "🚨 Aapatkaal SOS Kholein" else "Aage badhein (Open)",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.0f))
        }

        // Consequential Action Confirmation Dialog
        if (uiState.showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissAssistant() },
                title = {
                    Text(
                        text = "Kripya pushti karein (Confirmation)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = uiState.confirmationPrompt,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmConsequentialAction() },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Text("Haan, aage badhein (Yes, Proceed)", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelConsequentialAction() }) {
                        Text("Radd karein (Cancel)", color = TextHighContrast)
                    }
                }
            )
        }

        // Microphone Permission Rationale Dialog
        if (showPermissionRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionRationaleDialog = false },
                title = {
                    Text(
                        text = "Microphone Ki Anumati Chahiye",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Aapki aawaz se madad karne ke liye Vyavahaar ko microphone ki anumati ki aavashyakta hai. Kripya app settings mein permission on karein.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionRationaleDialog = false
                            val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(appSettingsIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Text("Settings Kholein", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationaleDialog = false }) {
                        Text("Theek hai (Dismiss)", color = TextHighContrast)
                    }
                }
            )
        }

        // Accidental-Trigger Protected Emergency SOS Dialog
        if (showSosDialog) {
            AlertDialog(
                onDismissRequest = { showSosDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SosCrimson)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aapatkaal (Emergency SOS)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SosCrimson
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Aap aapatkaal sahayata chun rahe hain. Kripya apna vikalp chunein:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                showSosDialog = false
                                viewModel.triggerSos { intent -> context.startActivity(intent) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SosCrimson),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🚨 112 Direct Call Lagayein", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                showSosDialog = false
                                viewModel.triggerSosAlertBackend(isTestMode = false) { details ->
                                    sosAlertDetails = details
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📢 Contacts Ko Alert Bhejein", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                showSosDialog = false
                                viewModel.triggerSosAlertBackend(isTestMode = true) { details ->
                                    sosAlertDetails = details
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🧪 Safe Demo / Test Mode", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSosDialog = false }) {
                        Text("Radd Karein (Cancel)", color = TextHighContrast)
                    }
                }
            )
        }

        // Safe Demo / SOS Alert Dispatch Details Feedback Dialog
        if (sosAlertDetails != null) {
            AlertDialog(
                onDismissRequest = { sosAlertDetails = null },
                title = {
                    Text(
                        text = "SOS Alert Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                },
                text = {
                    Text(
                        text = sosAlertDetails!!,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { sosAlertDetails = null },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Text("Theek Hai (OK)")
                    }
                }
            )
        }
    }
}

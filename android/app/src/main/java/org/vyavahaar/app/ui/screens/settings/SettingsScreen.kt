package org.vyavahaar.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.ApiClient
import org.vyavahaar.app.data.repository.AuthRepository
import org.vyavahaar.app.theme.*
import org.vyavahaar.app.ui.components.VyavahaarButton
import org.vyavahaar.app.ui.components.VyavahaarTopBar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember { PreferencesDataStore(context) }
    val authRepo = remember { AuthRepository(ApiClient.apiService, preferences) }

    val currentLang by preferences.preferredLanguage.collectAsState(initial = "hi")
    val currentScale by preferences.fontScale.collectAsState(initial = 1.2f)
    val currentSpeed by preferences.voiceSpeed.collectAsState(initial = 0.9f)
    val currentTheme by preferences.themeMode.collectAsState(initial = "light")

    var selectedLang by remember { mutableStateOf(currentLang) }
    var fontScale by remember { mutableFloatStateOf(currentScale) }
    var voiceSpeed by remember { mutableFloatStateOf(currentSpeed) }
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    LaunchedEffect(currentLang, currentScale, currentSpeed, currentTheme) {
        selectedLang = currentLang
        fontScale = currentScale
        voiceSpeed = currentSpeed
        selectedTheme = currentTheme
    }

    Scaffold(
        topBar = { VyavahaarTopBar(title = "Settings & Accessibility", onBackClick = onBack) },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Sugamta & Suvidha (Accessibility)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextHighContrast
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Language Selector Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bhasha Chuniye (Select Language)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedLang == "hi",
                            onClick = {
                                selectedLang = "hi"
                                coroutineScope.launch { preferences.saveAccessibilitySettings(selectedLang, fontScale, voiceSpeed, selectedTheme) }
                            },
                            label = { Text("हिंदी (Hindi)") }
                        )
                        FilterChip(
                            selected = selectedLang == "en",
                            onClick = {
                                selectedLang = "en"
                                coroutineScope.launch { preferences.saveAccessibilitySettings(selectedLang, fontScale, voiceSpeed, selectedTheme) }
                            },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = selectedLang == "kn",
                            onClick = {
                                selectedLang = "kn"
                                coroutineScope.launch { preferences.saveAccessibilitySettings(selectedLang, fontScale, voiceSpeed, selectedTheme) }
                            },
                            label = { Text("ಕನ್ನಡ (Kannada)") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font Scale Slider Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aksharon Ka Aakar (Text Size)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = fontScale,
                        onValueChange = {
                            fontScale = it
                            coroutineScope.launch { preferences.saveAccessibilitySettings(selectedLang, fontScale, voiceSpeed, selectedTheme) }
                        },
                        valueRange = 1.0f..1.6f,
                        steps = 2
                    )
                    Text(
                        text = "Namuna: Yeh aakar kaisa lag raha hai?",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = (18 * fontScale).sp),
                        color = TerracottaPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme Mode Card (Light, Dark, System)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Theme (Light / Dark Mode)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedTheme == "light",
                            onClick = {
                                selectedTheme = "light"
                                coroutineScope.launch { preferences.saveAccessibilitySettings(selectedLang, fontScale, voiceSpeed, selectedTheme) }
                            },
                            label = { Text("Light (Din)") }
                        )
                        FilterChip(
                            selected = selectedTheme == "dark",
                            onClick = {
                                selectedTheme = "dark"
                                coroutineScope.launch { preferences.saveAccessibilitySettings(selectedLang, fontScale, voiceSpeed, selectedTheme) }
                            },
                            label = { Text("Dark (Raat)") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Logout Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        authRepo.logout()
                        onLogout()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SosCrimson),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Karein (Sign Out)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

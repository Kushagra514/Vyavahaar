package org.vyavahaar.app.ui.screens.settings

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale
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
        topBar = { VyavahaarTopBar(title = "सेटिंग्स और पहुँच", onBackClick = onBack) },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = "सुगमता और सुविधा (पहुँच)",
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
                    Text("भाषा चुनें (Select Language)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedLang == "hi",
                            onClick = {
                                selectedLang = "hi"
                                coroutineScope.launch {
                                    preferences.saveAccessibilitySettings(selectedLang, fontScale, voiceSpeed, selectedTheme)
                                    updateAppLocale(context, selectedLang)
                                }
                            },
                            label = { Text("हिंदी (Hindi)") }
                        )
                        FilterChip(
                            selected = selectedLang == "en",
                            onClick = {
                                selectedLang = "en"
                                coroutineScope.launch {
                                    preferences.saveAccessibilitySettings(selectedLang, fontScale, voiceSpeed, selectedTheme)
                                    updateAppLocale(context, selectedLang)
                                }
                            },
                            label = { Text("English") }
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
                    Text("अक्षरों का आकार (टेक्स्ट साइज़)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                        text = "नमूना: यह आकार कैसा लग रहा है?",
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
                    Text("थीम (लाइट / डार्क मोड)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("लॉगआउट करें (साइन आउट)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun updateAppLocale(context: android.content.Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration()
    config.setLocale(locale)
    context.createConfigurationContext(config)
    val resources = context.resources
    val dm = resources.displayMetrics
    val conf = resources.configuration
    conf.setLocale(locale)
    resources.updateConfiguration(conf, dm)
    // Recreate the hosting activity so every screen recomposes in the new language
    (context as? android.app.Activity)?.recreate()
}

package org.vyavahaar.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository(ApiClient.apiService, PreferencesDataStore(context)) }

    var fullName by remember { mutableStateOf("Sharma Ji") }
    var selectedLanguage by remember { mutableStateOf("hi") }
    var fontScale by remember { mutableFloatStateOf(1.2f) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { VyavahaarTopBar(title = "आपका परिचय (प्रोफाइल)") },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "व्यवहार परिवार में स्वागत है",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextHighContrast
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "बस कुछ आसान जानकारी भरें ताकि हम आपकी बेहतर मदद कर सकें।",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMediumContrast
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Name field
            Text("आपका शुभ नाम", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Preferred Language
            Text("आपकी पसंदीदा भाषा (Language)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedLanguage == "hi",
                    onClick = { selectedLanguage = "hi" },
                    label = { Text("हिंदी (Hindi)", style = MaterialTheme.typography.bodyLarge) }
                )
                FilterChip(
                    selected = selectedLanguage == "en",
                    onClick = { selectedLanguage = "en" },
                    label = { Text("English", style = MaterialTheme.typography.bodyLarge) }
                )
                FilterChip(
                    selected = selectedLanguage == "kn",
                    onClick = { selectedLanguage = "kn" },
                    label = { Text("ಕನ್ನಡ (Kannada)", style = MaterialTheme.typography.bodyLarge) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text size accessibility preview
            Text("अक्षर का आकार (टेक्स्ट साइज़)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = fontScale,
                onValueChange = { fontScale = it },
                valueRange = 1.0f..1.6f,
                steps = 2
            )
            Text(
                text = "नमूना: यह आकार पढ़ने में आसान है।",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (18 * fontScale).sp),
                color = TerracottaPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(36.dp))

            VyavahaarButton(
                text = if (isLoading) "सुरक्षित कर रहे हैं..." else "आगे बढ़ें (पूर्ण)",
                enabled = fullName.isNotBlank() && !isLoading,
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        authRepo.updateProfile(
                            name = fullName.trim(),
                            language = selectedLanguage,
                            fontScale = fontScale,
                            themeMode = "light"
                        )
                        isLoading = false
                        onOnboardingComplete()
                    }
                }
            )
        }
    }
}

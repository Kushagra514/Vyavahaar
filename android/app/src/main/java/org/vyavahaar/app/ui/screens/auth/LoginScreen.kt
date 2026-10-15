package org.vyavahaar.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.vyavahaar.app.R
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.ApiClient
import org.vyavahaar.app.data.repository.AuthRepository
import org.vyavahaar.app.theme.*
import org.vyavahaar.app.ui.components.VyavahaarButton
import org.vyavahaar.app.ui.components.VyavahaarTopBar

@Composable
fun LoginScreen(
    onOtpSent: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository(ApiClient.apiService, PreferencesDataStore(context)) }

    var phoneNumber by remember { mutableStateOf("9876543210") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var devHint by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { VyavahaarTopBar(title = "Vyavahaar Pravesh") },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Namaste!",
                style = MaterialTheme.typography.headlineLarge,
                color = TextHighContrast,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kripya apna mobile number darj karein.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMediumContrast
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(stringResource(R.string.phone_label), style = MaterialTheme.typography.bodyLarge) },
                prefix = { Text("+91 ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = SosCrimson,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (devHint != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = devHint!!,
                    color = ForestGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            VyavahaarButton(
                text = if (isLoading) "Bhej rahe hain..." else stringResource(R.string.send_otp),
                enabled = phoneNumber.length >= 10 && !isLoading,
                onClick = {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val fullPhone = "+91" + phoneNumber.trim()
                        val result = authRepo.requestOtp(fullPhone)
                        isLoading = false
                        result.onSuccess { resp ->
                            devHint = if (resp.devOtp != null) "Sandbox OTP: ${resp.devOtp}" else null
                            onOtpSent(fullPhone)
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Failed to send OTP"
                        }
                    }
                }
            )
        }
    }
}

package org.vyavahaar.app.ui.screens.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.ApiClient
import org.vyavahaar.app.data.remote.ContactDto
import org.vyavahaar.app.data.repository.ContactsRepository
import org.vyavahaar.app.theme.*
import org.vyavahaar.app.R
import org.vyavahaar.app.ui.components.VyavahaarTopBar

@Composable
fun ContactsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val contactsRepo = remember { ContactsRepository(ApiClient.apiService, PreferencesDataStore(context)) }

    var contacts by remember { mutableStateOf<List<ContactDto>>(emptyList()) }
    var activeCallingName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val res = contactsRepo.getContacts()
            res.onSuccess { contacts = it }
        }
    }

    Scaffold(
        topBar = { VyavahaarTopBar(title = stringResource(R.string.contacts_bar_title), onBackClick = onBack) },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.contacts_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextHighContrast
            )
            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(contacts) { c ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = c.name.take(1),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = c.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHighContrast
                                    )
                                    if (c.isFavorite) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Star, contentDescription = "Favorite", tint = MicOrangeYellow, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(
                                    text = c.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMediumContrast
                                )
                            }

                            // Voice Call Button
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${c.phoneNumber}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call ${c.name}", tint = ForestGreen, modifier = Modifier.size(30.dp))
                            }

                            // Video Call Button
                            IconButton(
                                onClick = {
                                    activeCallingName = c.name
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = "Video Call ${c.name}", tint = CalmBlue, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }

        // In-App Video Call Simulation Dialog
        if (activeCallingName != null) {
                val callingName = activeCallingName
            AlertDialog(
                onDismissRequest = { activeCallingName = null },
                title = { Text(stringResource(R.string.contacts_video_call_in_progress), fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = CalmBlue, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.contacts_video_calling_name, callingName ?: ""),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { activeCallingName = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SosCrimson)
                    ) {
                        Text(stringResource(R.string.contacts_hang_up_btn))
                    }
                }
            )
        }
    }
}

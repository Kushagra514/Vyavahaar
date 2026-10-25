package org.vyavahaar.app.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
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
import org.vyavahaar.app.data.remote.EventDto
import org.vyavahaar.app.data.repository.EventsRepository
import org.vyavahaar.app.theme.*
import org.vyavahaar.app.ui.components.VyavahaarButton
import org.vyavahaar.app.ui.components.VyavahaarTopBar

@Composable
fun EventsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val eventsRepo = remember { EventsRepository(ApiClient.apiService, PreferencesDataStore(context)) }

    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("ALL", "Satsang", "Walking", "Yoga", "HealthCamp")
    
    // String resources for category labels
    val categoryAll = stringResource(R.string.events_category_all)
    val categorySatsang = stringResource(R.string.events_category_social) // Satsang is social
    val categoryWalking = stringResource(R.string.events_category_health) // Walking is health
    val categoryYoga = stringResource(R.string.events_category_learning) // Yoga is learning
    val categoryHealthCamp = stringResource(R.string.events_category_health) // Health camp is health

    // Hoisted so they can be used inside coroutine callbacks (non-composable)
    val eventsRegisterLabel = stringResource(R.string.events_register)
    val commonCancelLabel = stringResource(R.string.common_cancel)
    val commonSaveLabel = stringResource(R.string.common_save)
    val commonErrorLabel = stringResource(R.string.common_error)

    fun loadEvents() {
        coroutineScope.launch {
            isLoading = true
            loadError = null
            val res = eventsRepo.getEvents(if (selectedCategory == "ALL") null else selectedCategory)
            isLoading = false
            res.onSuccess { events = it }
            res.onFailure { loadError = it.localizedMessage ?: "Failed to load events" }
        }
    }

    LaunchedEffect(selectedCategory) {
        loadEvents()
    }

        Scaffold(
        topBar = { VyavahaarTopBar(title = stringResource(R.string.events_title), onBackClick = onBack) },
        containerColor = WarmCreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Category filter chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = when (cat) {
                                    "ALL" -> categoryAll
                                    "Satsang" -> categorySatsang
                                    "Walking" -> categoryWalking
                                    "Yoga" -> categoryYoga
                                    "HealthCamp" -> categoryHealthCamp
                                    else -> cat
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                }
            }

            if (actionStatusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = actionStatusMessage!!,
                        color = ForestGreen,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TerracottaPrimary)
                }
                } else if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = loadError ?: stringResource(R.string.events_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMediumContrast
                        )
                        if (loadError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { loadEvents() }) {
                                Text(stringResource(R.string.common_retry))
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(events) { ev ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(ev.category, fontWeight = FontWeight.Bold) }
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = stringResource(R.string.events_verified),
                                            tint = ForestGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.events_verified),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ForestGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ev.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighContrast
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ev.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMediumContrast
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${ev.venueName} • ${ev.address ?: ""}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (ev.isRegistered) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = ForestGreen,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.events_registered_text),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = ForestGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val res = eventsRepo.unregisterFromEvent(ev.id)
                                                    res.onSuccess {
                                                        actionStatusMessage = "$eventsRegisterLabel $commonCancelLabel $commonSaveLabel"
                                                        loadEvents()
                                                    }.onFailure {
                                                        actionStatusMessage = "$eventsRegisterLabel $commonErrorLabel: ${it.localizedMessage}"
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SosCrimson),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text(stringResource(R.string.events_register) + " " + stringResource(R.string.common_cancel), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                VyavahaarButton(
                                    text = stringResource(R.string.events_register),
                                    onClick = {
                                            coroutineScope.launch {
                                                val res = eventsRepo.registerForEvent(ev.id)
                                                res.onSuccess {
                                                    actionStatusMessage = it.message
                                                    loadEvents()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

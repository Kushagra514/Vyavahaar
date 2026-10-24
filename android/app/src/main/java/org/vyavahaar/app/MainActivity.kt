package org.vyavahaar.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.Locale
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.navigation.Screen
import org.vyavahaar.app.navigation.VyavahaarNavGraph
import org.vyavahaar.app.theme.TerracottaPrimary
import org.vyavahaar.app.theme.VyavahaarTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferences: PreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedLocale()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = PreferencesDataStore(applicationContext)

        setContent {
            val fontScale by preferences.fontScale.collectAsState(initial = 1.2f)
            val themeMode by preferences.themeMode.collectAsState(initial = "light")

            var authChecked by remember { mutableStateOf(false) }
            var startDestination by remember { mutableStateOf(Screen.Login.route) }

            LaunchedEffect(Unit) {
                val token = preferences.authToken.firstOrNull()
                val isOnboarded = preferences.isOnboarded.firstOrNull() ?: false
                startDestination = if (!token.isNullOrBlank() && isOnboarded) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }
                authChecked = true
            }

            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            VyavahaarTheme(
                darkTheme = isDarkTheme,
                fontScale = fontScale
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (!authChecked) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TerracottaPrimary)
                        }
                    } else {
                        val navController = rememberNavController()
                        VyavahaarNavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }

    private fun applySavedLocale() {
        val prefs = PreferencesDataStore(applicationContext)
        val lang = runBlocking { prefs.preferredLanguage.firstOrNull() } ?: "hi"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

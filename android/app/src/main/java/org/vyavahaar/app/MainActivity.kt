package org.vyavahaar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.navigation.Screen
import org.vyavahaar.app.navigation.VyavahaarNavGraph
import org.vyavahaar.app.theme.VyavahaarTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferences: PreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = PreferencesDataStore(applicationContext)

        setContent {
            val fontScale by preferences.fontScale.collectAsState(initial = 1.2f)
            val themeMode by preferences.themeMode.collectAsState(initial = "light")
            val token by preferences.authToken.collectAsState(initial = null)
            val isOnboarded by preferences.isOnboarded.collectAsState(initial = false)

            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            VyavahaarTheme(
                darkTheme = isDarkTheme,
                fontScale = fontScale
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val startDestination = if (token != null && isOnboarded) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    VyavahaarNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

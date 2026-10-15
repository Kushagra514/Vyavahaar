package org.vyavahaar.app.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.ui.screens.appointments.AppointmentsScreen
import org.vyavahaar.app.ui.screens.auth.LoginScreen
import org.vyavahaar.app.ui.screens.auth.OnboardingScreen
import org.vyavahaar.app.ui.screens.auth.OtpVerifyScreen
import org.vyavahaar.app.ui.screens.contacts.ContactsScreen
import org.vyavahaar.app.ui.screens.events.EventsScreen
import org.vyavahaar.app.ui.screens.home.HomeScreen
import org.vyavahaar.app.ui.screens.prescriptions.PrescriptionsScreen
import org.vyavahaar.app.ui.screens.settings.SettingsScreen
import org.vyavahaar.app.ui.screens.transport.TransportScreen

@Composable
fun VyavahaarNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onOtpSent = { phone ->
                    navController.navigate(Screen.OtpVerify.createRoute(phone))
                }
            )
        }

        composable(
            route = Screen.OtpVerify.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            OtpVerifyScreen(
                phoneNumber = phone,
                onVerificationSuccess = { isOnboarded ->
                    if (isOnboarded) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToTransport = { navController.navigate(Screen.Transport.route) },
                onNavigateToAppointments = { navController.navigate(Screen.Appointments.route) },
                onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) }
            )
        }

        composable(Screen.Events.route) {
            EventsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Transport.route) {
            TransportScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Appointments.route) {
            AppointmentsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Prescriptions.route) {
            PrescriptionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Contacts.route) {
            ContactsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

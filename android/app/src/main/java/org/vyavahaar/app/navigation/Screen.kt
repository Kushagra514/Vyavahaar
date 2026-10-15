package org.vyavahaar.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object LanguageSelection : Screen("language_selection")
    object Login : Screen("login")
    object OtpVerify : Screen("otp_verify/{phone}") {
        fun createRoute(phone: String) = "otp_verify/$phone"
    }
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Events : Screen("events")
    object Transport : Screen("transport")
    object Appointments : Screen("appointments")
    object Prescriptions : Screen("prescriptions")
    object Contacts : Screen("contacts")
    object Settings : Screen("settings")
    object SosEmergency : Screen("sos_emergency")
}

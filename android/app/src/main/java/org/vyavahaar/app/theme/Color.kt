package org.vyavahaar.app.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// VYAVHAAR - PROFESSIONAL COLOR SYSTEM
// Designed for Indian seniors: warm, accessible, culturally resonant, beautiful
// =============================================================================

// ── FOUNDATION: Warm Neutral Base ──
// A custom warm gray scale with subtle brown undertones - never clinical
val Neutral050  = Color(0xFFFDFBF7)  // Warmest white - paper
val Neutral100  = Color(0xFFFAF6F0)  // Ivory linen
val Neutral200  = Color(0xFFF0ECE3)  // Sand dollar
val Neutral300  = Color(0xFFE0D9CC)  // Warm sandstone
val Neutral400  = Color(0xFFC8BFAF)  // Desert stone
val Neutral500  = Color(0xFFA89E8D)  // Weathered clay
val Neutral600  = Color(0xFF8A8172)  // Aged parchment
val Neutral700  = Color(0xFF6D6558)  // Warm graphite
val Neutral800  = Color(0xFF4E483E)  // Deep umber
val Neutral900  = Color(0xFF2E2A23)  // Rich dark earth
val Neutral950  = Color(0xFF1A1814)  // Near-black warmth

// ── BRAND: Terracotta Family - Soul of the App ──
val Terracotta50  = Color(0xFFFEF7F5)
val Terracotta100 = Color(0xFFFDEAE4)
val Terracotta200 = Color(0xFFFBD0C4)
val Terracotta300 = Color(0xFFF8A88E)
val Terracotta400 = Color(0xFFF48060)
val Terracotta500 = Color(0xFFE86B44)  // PRIMARY - Vibrant but warm
val Terracotta600 = Color(0xFFD45A36)  // Pressed state
val Terracotta700 = Color(0xFFB8482E)  // Hover/deep
val Terracotta800 = Color(0xFF963A28)
val Terracotta900 = Color(0xFF732E20)
val Terracotta950 = Color(0xFF3D1710)

// ── ACCENT: Warm Gold/Amber - Voice, Attention ──
val Amber50   = Color(0xFFFFFDF5)
val Amber100  = Color(0xFFFFF9E0)
val Amber200  = Color(0xFFFFF0B8)
val Amber300  = Color(0xFFFFE37A)
val Amber400  = Color(0xFFFFD445)
val Amber500  = Color(0xFFFFC41A)  // Voice active, warnings
val Amber600  = Color(0xFFE6A814)
val Amber700  = Color(0xFFC48A10)
val Amber800  = Color(0xFFA36E10)
val Amber900  = Color(0xFF855712)

// ── SEMANTIC: Sage Green - Health, Success, Calm ──
val Sage50    = Color(0xFFF5FDF7)
val Sage100   = Color(0xFFE8FAED)
val Sage200   = Color(0xFFD0F5D8)
val Sage300   = Color(0xFFAEEAB4)
val Sage400   = Color(0xFF86DA90)
val Sage500   = Color(0xFF6BC772)  // Success, verified
val Sage600   = Color(0xFF54A85A)
val Sage700   = Color(0xFF438547)
val Sage800   = Color(0xFF38693A)
val Sage900   = Color(0xFF305331)

// ── SEMANTIC: Soft Teal - Medical, Trust, Water ──
val Teal50    = Color(0xFFF0FCFD)
val Teal100   = Color(0xFFD6F4F8)
val Teal200   = Color(0xFFACE9F1)
val Teal300   = Color(0xFF7DDCE8)
val Teal400   = Color(0xFF4FC9DE)
val Teal500   = Color(0xFF2EB4C8)  // Medical info, transport
val Teal600   = Color(0xFF1E9AAE)
val Teal700   = Color(0xFF1C7F8E)
val Teal800   = Color(0xFF1D6671)
val Teal900   = Color(0xFF1B545B)

// ── SEMANTIC: Warm Crimson - Emergency Only ──
val Crimson50  = Color(0xFFFFF5F5)
val Crimson100 = Color(0xFFFFE8E8)
val Crimson200 = Color(0xFFFFD0D0)
val Crimson300 = Color(0xFFFFAAAA)
val Crimson400 = Color(0xFFFF7A7A)
val Crimson500 = Color(0xFFFF5A5A)  // SOS, critical
val Crimson600 = Color(0xFFE84545)
val Crimson700 = Color(0xFFC83232)
val Crimson800 = Color(0xFFA32828)
val Crimson900 = Color(0xFF852020)

// ── SEMANTIC: Heritage Blue - Info, Links ──
val Indigo50  = Color(0xFFEEF2FF)
val Indigo100 = Color(0xFFE0E7FF)
val Indigo200 = Color(0xFFC7D2FE)
val Indigo300 = Color(0xFFA5B4FC)
val Indigo400 = Color(0xFF818CF8)
val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)
val Indigo700 = Color(0xFF4338CA)
val Indigo800 = Color(0xFF3730A3)
val Indigo900 = Color(0xFF312E81)

// =============================================================================
// LIGHT THEME - Warm, bright, paper-like
// =============================================================================
val LightBackground       = Neutral050      // #FDFBF7 - Warm paper white
val LightSurface          = Neutral100      // #FAF6F0 - Slightly elevated
val LightSurfaceVariant   = Neutral200      // #F0ECE3 - Input fields, chips
val LightSurfaceHigh      = Color.White     // #FFFFFF - Modal, FAB
val LightOutline          = Neutral300      // #E0D9CC - Borders, dividers
val LightOutlineVariant   = Neutral400      // #C8BFAF - Subtle borders

val LightOnBackground     = Neutral900      // #2E2A23 - Primary text
val LightOnSurface        = Neutral800      // #4E483E - On cards
val LightOnSurfaceVariant = Neutral600      // #8A8172 - Secondary text
val LightInverseSurface   = Neutral900      // For modals on dark
val LightInverseOnSurface = Neutral050

val LightPrimary          = Terracotta600   // #D45A36 - Main actions
val LightOnPrimary        = Color.White
val LightPrimaryContainer = Terracotta100   // #FDEAE4 - Filled buttons
val LightOnPrimaryContainer = Terracotta900 // #732E20

val LightSecondary        = Teal600         // #1E9AAE - Secondary actions
val LightOnSecondary      = Color.White
val LightSecondaryContainer = Teal100       // #D6F4F8
val LightOnSecondaryContainer = Teal900     // #1B545B

val LightTertiary         = Amber700        // #C48A10 - Voice, accent
val LightOnTertiary       = Color.White
val LightTertiaryContainer = Amber100       // #FFF9E0
val LightOnTertiaryContainer = Amber900     // #855712

val LightError            = Crimson600      // #E84545
val LightOnError          = Color.White
val LightErrorContainer   = Crimson100      // #FFE8E8
val LightOnErrorContainer = Crimson900      // #852020

val LightSuccess          = Sage600         // #54A85A
val LightSuccessContainer = Sage100         // #E8FAED
val LightOnSuccessContainer = Sage900       // #305331

// Shadow & Scrim
val LightShadow           = Color(0x33000000)
val LightScrim            = Color(0x99000000)

// =============================================================================
// DARK THEME - Rich, warm, layered - NOT flat black
// =============================================================================
val DarkBackground        = Neutral950      // #1A1814 - Deep warm near-black
val DarkSurface           = Neutral900      // #2E2A23 - Base surface
val DarkSurfaceVariant    = Neutral800      // #4E483E - Elevated cards, inputs
val DarkSurfaceHigh       = Neutral800      // #4E483E - Modal, dialogs
val DarkOutline           = Neutral700      // #6D6558 - Visible borders
val DarkOutlineVariant    = Neutral600      // #8A8172 - Subtle dividers

val DarkOnBackground      = Neutral050      // #FAF6F0 - Primary text (warm white)
val DarkOnSurface         = Neutral050      // #FAF6F0 - On cards
val DarkOnSurfaceVariant  = Neutral400      // #C8BFAF - Secondary text
val DarkInverseSurface    = Neutral100      // For modals on light
val DarkInverseOnSurface  = Neutral900

val DarkPrimary           = Terracotta400   // #F48060 - Brighter for dark bg
val DarkOnPrimary         = Neutral950      // #1A1814
val DarkPrimaryContainer  = Terracotta800   // #963A28 - Filled buttons
val DarkOnPrimaryContainer = Terracotta50   // #FEF7F5

val DarkSecondary         = Teal400         // #4FC9DE
val DarkOnSecondary       = Neutral950
val DarkSecondaryContainer = Teal800        // #1D6671
val DarkOnSecondaryContainer = Teal50       // #F0FCFD

val DarkTertiary          = Amber400        // #FFD445
val DarkOnTertiary        = Neutral950
val DarkTertiaryContainer = Amber800        // #A36E10
val DarkOnTertiaryContainer = Amber50       // #FFFFFDF5

val DarkError             = Crimson400      // #FF7A7A
val DarkOnError           = Neutral950
val DarkErrorContainer    = Crimson800      // #A32828
val DarkOnErrorContainer  = Crimson50       // #FFF5F5

val DarkSuccess           = Sage400         // #86DA90
val DarkSuccessContainer  = Sage800         // #38693A
val DarkOnSuccessContainer = Sage50         // #F5FDF7

// Shadow & Scrim - warmer shadows
val DarkShadow            = Color(0x4C000000)
val DarkScrim             = Color(0xBF000000)

// =============================================================================
// INTERACTIVE STATE LAYERS (for Material 3 state layer opacity)
// =============================================================================
// These are applied as overlays on primary/secondary/tertiary containers

// =============================================================================
// GRADIENT TOKENS - For beautiful backgrounds, headers, voice visualizer
// =============================================================================
val GradientPrimaryStart   = Terracotta500  // #E86B44
val GradientPrimaryEnd     = Terracotta700  // #B8482E
val GradientWarmStart      = Amber500       // #FFC41A
val GradientWarmEnd        = Terracotta500  // #E86B44
val GradientCalmStart      = Teal500        // #2EB4C8
val GradientCalmEnd        = Sage500        // #6BC772
val GradientVoiceStart     = Amber400       // #FFD445
val GradientVoiceEnd       = Terracotta400  // #F48060
val GradientSurfaceStart   = Neutral100     // Light
val GradientSurfaceEnd     = Neutral200     // Light
val GradientDarkSurfaceStart = Neutral900   // Dark
val GradientDarkSurfaceEnd   = Neutral950   // Dark

// =============================================================================
// LEGACY ALIASES (for existing code compatibility)
// =============================================================================
val WarmCreamBackground    = LightBackground
val WarmCardSurface        = LightSurface
val WarmCardBorder         = LightOutline
val WarmSurfaceVariant     = LightSurfaceVariant
val TextHighContrast       = LightOnBackground
val TextMediumContrast     = LightOnSurfaceVariant
val TextSubtle             = Neutral500
val TerracottaPrimary      = LightPrimary
val TerracottaContainer    = LightPrimaryContainer
val OnTerracottaPrimary    = LightOnPrimary
val MicOrangeYellow        = LightTertiary
val MicOrangeGlow          = LightTertiaryContainer
val SosCrimson             = LightError
val SosCrimsonContainer    = LightErrorContainer
val OnSosCrimson           = LightOnError
val ForestGreen            = LightSuccess
val ForestGreenContainer   = LightSuccessContainer
val TealAccent             = LightSecondary
val CalmBlue               = Indigo500

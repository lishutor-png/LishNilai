package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// === SOOTHING MODERN PALETTE (Light & Dark) ===
// Primary: Deep Royal / Slate Indigo (Accessible, professional, calm)
val PrimaryLight = Color(0xFF1E40AF) // Deep Royal Blue
val PrimaryLightDark = Color(0xFF1E3A8A)
val PrimaryContainerLight = Color(0xFFEFF6FF) // Soft Sky/Ice Blue
val OnPrimaryContainerLight = Color(0xFF1E3A8A)

val PrimaryDark = Color(0xFF93C5FD) // Soft Luminous Blue
val PrimaryContainerDark = Color(0xFF1E3A8A)
val OnPrimaryContainerDark = Color(0xFFDBEAFE)

// Secondary: Refined Slate / Steel
val SecondaryLight = Color(0xFF475569) // Slate Steel
val SecondaryContainerLight = Color(0xFFF1F5F9)
val OnSecondaryContainerLight = Color(0xFF0F172A)

val SecondaryDark = Color(0xFFCBD5E1) // Soft Slate
val SecondaryContainerDark = Color(0xFF334155)
val OnSecondaryContainerDark = Color(0xFFF8FAFC)

// Tertiary: Warm Bronze / Slate Amber (Subtle Accents)
val TertiaryLight = Color(0xFFB45309)
val TertiaryContainerLight = Color(0xFFFEF3C7)
val OnTertiaryContainerLight = Color(0xFF78350F)

val TertiaryDark = Color(0xFFFCD34D)
val TertiaryContainerDark = Color(0xFF78350F)
val OnTertiaryContainerDark = Color(0xFFFEF3C7)

// Surfaces & Backgrounds - Light Mode
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceBackground = Color(0xFFF8FAFC) // Soothing canvas
val SurfaceCardLight = Color(0xFFFFFFFF)
val SurfaceCardMuted = Color(0xFFF8FAFC)
val SurfaceElevated = Color(0xFFF1F5F9)

// Surfaces & Backgrounds - Dark Mode (Rich OLED & Slate Canvas, easy on the eyes)
val SurfaceDark = Color(0xFF0F172A) // Rich Deep Slate Navy
val SurfaceBackgroundDark = Color(0xFF090D16) // Deep Dark Canvas
val SurfaceCardDark = Color(0xFF1E293B) // Elevated Slate Card
val SurfaceCardMutedDark = Color(0xFF162032)
val SurfaceElevatedDark = Color(0xFF273549) // Soothing Elevated Card

// High Contrast Text Colors
val OnSurfaceLight = Color(0xFF0F172A) // Rich Deep Slate/Black
val OnSurfaceVariantLight = Color(0xFF334155) // Slate
val TextMuted = Color(0xFF64748B) // Medium Slate
val OutlineLight = Color(0xFFE2E8F0) // Clean subtle border

val OnSurfaceDark = Color(0xFFF8FAFC) // Crisp White
val OnSurfaceVariantDark = Color(0xFFCBD5E1) // Soft Light Slate
val TextMutedDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF334155) // Clean dark border

// Semantic Status Colors (Used purely for Badges & Status Indicators, NOT solid button backgrounds)
val RedError = Color(0xFFDC2626)
val RedErrorContainer = Color(0xFFFEF2F2)
val RedErrorBorder = Color(0xFFFECACA)
val OnRedError = Color(0xFFFFFFFF)
val OnRedErrorContainer = Color(0xFF991B1B)

val GreenSuccess = Color(0xFF16A34A)
val GreenSuccessContainer = Color(0xFFECFDF5)
val OnGreenSuccessContainer = Color(0xFF065F46)

val YellowWarning = Color(0xFFD97706)
val YellowWarningContainer = Color(0xFFFEF3C7)

val PurpleRemedialContainer = Color(0xFFF5F3FF)
val PurpleRemedialText = Color(0xFF6D28D9)
val PurpleRemedialBorder = Color(0xFFDDD6FE)

// Aliases for compatibility
val TealPrimary = PrimaryLight
val TealPrimaryDark = PrimaryLightDark
val TealPrimaryContainer = PrimaryContainerLight
val OnTealPrimaryContainer = OnPrimaryContainerLight

val IndigoSecondary = SecondaryLight
val IndigoSecondaryContainer = SecondaryContainerLight
val OnIndigoSecondaryContainer = OnSecondaryContainerLight

val AmberTertiary = TertiaryLight
val AmberTertiaryContainer = TertiaryContainerLight
val OnAmberTertiaryContainer = OnTertiaryContainerLight

val TealPrimaryDarkTheme = PrimaryDark
val IndigoSecondaryDarkTheme = SecondaryDark

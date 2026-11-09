package id.xms.ohscaler.egg.core.designsystem

import androidx.compose.ui.graphics.Color

// ============================================================================
// Android 17 Inspired Frosted Glass & Slate-Coral Theme System
// Clean, tactile frosted glass with high-contrast Slate Blue and Coral accents.
// No purple tones; distinct card contours in both Dark and Light themes.
// ============================================================================

// --- Dark Theme: Deep Night Slate & Frosted Glass (Ice Blue & Coral) ---
val DarkBackground = Color(0xFF0C0E14)
val DarkSurface = Color(0xFF151821)
val DarkSurfaceContainerLow = Color(0xFF11131A)
val DarkSurfaceContainer = Color(0xFF1A1D27)
val DarkSurfaceContainerHigh = Color(0xFF222634)
val DarkSurfaceContainerHighest = Color(0xFF2C3142)

val DarkGlassSurface = Color(0xD4181B26)       // Android 17 diffuse matte slate (83% alpha, zero specular)
val DarkGlassBorder = Color(0x1AFFFFFF)        // Flat subtle 10% white outline (no specular highlight)

val DarkPrimary = Color(0xFFA8C7FA)            // Android 17 Pale Ice Blue
val DarkOnPrimary = Color(0xFF0F2F4C)
val DarkPrimaryContainer = Color(0xFF224468)
val DarkOnPrimaryContainer = Color(0xFFD7E6FD)

val DarkSecondary = Color(0xFFF27A72)          // Android 17 Emergency Coral / Salmon
val DarkOnSecondary = Color(0xFF49110F)
val DarkSecondaryContainer = Color(0xFF6E2421)
val DarkOnSecondaryContainer = Color(0xFFFFDAD6)

val DarkTertiary = Color(0xFF7DD3FC)           // Soft Sky Mint
val DarkBorder = Color(0xFF262B3A)
val DarkBorderLight = Color(0xFF3A4156)

val DarkTextPrimary = Color(0xFFF8FAFC)        // Crisp Ice White
val DarkTextSecondary = Color(0xFF94A3B8)      // Cool Slate
val DarkTextMuted = Color(0xFF64748B)          // Muted Slate

// --- Light Theme: Frosted Milk Glass & Cool Mist (Slate Blue & Coral) ---
val LightBackground = Color(0xFFEDF1F7)        // Cool Ice Mist (provides depth behind white frosted cards)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFE4E9F2)
val LightSurfaceContainer = Color(0xFFDAE2ED)
val LightSurfaceContainerHigh = Color(0xFFCFDAE7)
val LightSurfaceContainerHighest = Color(0xFFC3D1E0)

val LightGlassSurface = Color(0xE0FFFFFF)      // Android 17 diffuse milk glass (88% alpha, zero specular)
val LightGlassBorder = Color(0x1F334155)       // Flat subtle 12% slate outline (no specular highlight)

val LightPrimary = Color(0xFF355375)           // Android 17 Steel Slate Blue
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD8E5F6)
val LightOnPrimaryContainer = Color(0xFF091E33)

val LightSecondary = Color(0xFFD8493D)         // Android 17 Rich Coral Red
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFDAD6)
val LightOnSecondaryContainer = Color(0xFF410002)

val LightTertiary = Color(0xFF0284C7)          // Sky Blue
val LightBorder = Color(0xFFCBD5E1)            // Crisp Slate Border for high card visibility
val LightBorderLight = Color(0xFFE2E8F0)

val LightTextPrimary = Color(0xFF0F172A)        // Deep Charcoal Slate (WCAG AAA 15:1 contrast)
val LightTextSecondary = Color(0xFF334155)      // Crisp Charcoal Slate (9:1 contrast)
val LightTextMuted = Color(0xFF64748B)          // Medium Slate (4.8:1 contrast)

// --- Shared Status Colors ---
val EmeraldSuccess = Color(0xFF10B981)
val EmeraldGlow = Color(0xFF34D399)
val AmberWarning = Color(0xFFF59E0B)
val CoralError = Color(0xFFEF4444)

// --- Hero & Card Gradients ---
val DarkHeroGradient = listOf(Color(0xFF1B2333), Color(0xFF281E24), Color(0xFF131620))
val LightHeroGradient = listOf(Color(0xFFE2EAF5), Color(0xFFFDECEB), Color(0xFFE8EEF5))

// --- Ambient Backdrops for Frosted Glass Depth ---
val DarkAmbientBlob1 = Color(0x331E293B)
val DarkAmbientBlob2 = Color(0x2E3B202A)
val LightAmbientBlob1 = Color(0x59CBD5E1)
val LightAmbientBlob2 = Color(0x3DFED7D7)

// Legacy aliases for backward compatibility across modules
val ObsidianBlack = DarkBackground
val SurfaceDark = DarkSurface
val SurfaceContainerLow = DarkSurfaceContainerLow
val SurfaceVariantDark = DarkSurfaceContainer
val SurfaceContainerHigh = DarkSurfaceContainerHigh
val SurfaceContainerHighest = DarkSurfaceContainerHighest
val SurfaceElevatedDark = DarkSurfaceContainerHighest
val BorderDark = DarkBorder
val BorderLight = DarkBorderLight
val SpecularEdgeTop = Color(0x33FFFFFF)
val SpecularEdgeBottom = Color(0x0AFFFFFF)
val ElectricViolet = DarkPrimary
val ElectricVioletDark = DarkPrimaryContainer
val ElectricVioletLight = Color(0xFFD7E6FD)
val IndigoAccent = DarkPrimary
val CyanAccent = DarkTertiary
val CyanGlow = Color(0xFF7DD3FC)
val RoseAccent = DarkSecondary
val RoseGlow = Color(0xFFF27A72)
val TextPrimary = DarkTextPrimary
val TextSecondary = DarkTextSecondary
val TextMuted = DarkTextMuted
val GlassOverlay = DarkGlassSurface
val GlassBorder = DarkGlassBorder
val HandleGlow = Color(0xFFFFFFFF)

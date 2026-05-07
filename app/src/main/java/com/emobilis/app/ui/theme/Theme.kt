package com.emobilis.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Emobilis Brand Palette (matches the school banner image) ──────────────────
val EmobilisPrimary    = Color(0xFF6D1B6D)   // rich purple from banner
val EmobilisSecondary  = Color(0xFF3B0A2A)   // deep maroon
val EmobilisAccent     = Color(0xFFFFCC00)   // gold (sun / smiley)
val EmobilisPink       = Color(0xFFB5326F)   // mid-pink gradient stop
val EmobilisBackground = Color(0xFFF5F0FF)   // soft lavender tint
val EmobilisGreen      = Color(0xFF1B5E20)
val EmobilisRed        = Color(0xFFC62828)
val EmobilisOrange     = Color(0xFFE65100)

private val EmobilisColorScheme = lightColorScheme(
    primary          = EmobilisPrimary,
    secondary        = EmobilisSecondary,
    tertiary         = EmobilisAccent,
    background       = EmobilisBackground,
    surface          = Color.White,
    onPrimary        = Color.White,
    onSecondary      = Color.White,
    onBackground     = Color(0xFF1C1B1F),
    onSurface        = Color(0xFF1C1B1F),
    error            = EmobilisRed,
)

@Composable
fun EmobilisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EmobilisColorScheme,
        typography  = Typography(),
        content     = content
    )
}
